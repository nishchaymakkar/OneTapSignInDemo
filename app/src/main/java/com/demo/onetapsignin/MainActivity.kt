package com.demo.onetapsignin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.demo.onetapsignin.ui.theme.OneTapSignInDemoTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OneTapSignInDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color= MaterialTheme.colorScheme.background
                ) {
                    GoogleSignInScreen()
                }
            }
        }
    }
}

@Composable
fun GoogleSignInScreen(){
    val context = LocalContext.current

    val credentialManager = remember { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()
    var signInStatus by remember { mutableStateOf("Signed Out") }
    var isLoading by remember { mutableStateOf(false) }
    val clientId = BuildConfig.CLIENT_ID
    fun launchSignIn() {
        coroutineScope.launch {
            signInStatus = "Launching Google Sign-In..."
            isLoading = true
            try {
                val request = GetCredentialRequest(
                    listOf(
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(clientId)
                            .build()
                    )
                )

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )


                val credential = result.credential as? GoogleIdTokenCredential
                val idToken = credential?.idToken

                if (idToken != null) {
                    signInStatus = "Got IdToken, verifying with server..."
                    val serverResponse = withContext(Dispatchers.IO) {
                        verifyTokenOnBackend(idToken)
                    }
                    signInStatus = serverResponse
                } else {
                    signInStatus = "No IdToken"
                    Log.e("CredentialManager", "No IdToken!")
                }

            } catch (e: Exception) {
                signInStatus = "Sign-in failed: ${e.localizedMessage}"
                Log.e("CredentialManager", "Sign-in error", e)
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    signInStatus = "Initializing..."
                    launchSignIn()
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !isLoading,
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isLoading){
                    CircularProgressIndicator()
                } else {
                    Text("Sign in with Google")
                }

            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = signInStatus)

        }
    }
}

private fun verifyTokenOnBackend(idToken: String): String {
    val backendUrl = BuildConfig.ENDPOINT
    val client = OkHttpClient()

    return try {
        val jsonObject = JSONObject()
        jsonObject.put("token", idToken)
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body.string()
            "Server response: $responseBody"
        } else {
            Log.d("BackendVerify", "Server verification failed: ${response.message}")
            "Server verification failed: ${response.code} ${response.message}"
        }
    } catch (e: IOException) {
        Log.e("BackendVerify", "Network error", e)
        "Network error: Could not connect to backend. Is the server running?"
    } catch (e: Exception) {
        Log.e("BackendVerify", "An unexpected error occurred", e)
        "An unexpected error occurred: ${e.localizedMessage}"
    }
}