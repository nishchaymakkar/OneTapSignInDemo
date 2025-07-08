# OneTapSignInDemo

A demo Android application demonstrating **Google One Tap Sign-In** integrated with a **Spring Boot backend** for secure user authentication using ID tokens.

![License](https://img.shields.io/github/license/nishchaymakkar/OneTapSignInDemo)

---

## 🚀 Features

* Google One Tap Sign-In in Android using the latest `play-services-auth` library
* Token verification and authentication handling in Spring Boot backend
* Separation of concerns with Clean Architecture principles
* Secure communication using JWT (in future scope)
* MVVM architecture in Android frontend
* Kotlin + Jetpack Compose UI

---

## 📂 Project Structure

### Android Frontend (`app/`)

* Kotlin + Jetpack Compose
* Google One Tap Sign-In
* Retrofit + Ktor Client for API calls
* Dependency Injection via Koin
* Datastore for local persistence

### Backend (`backend/`)

* Spring Boot (Java)
* GoogleIdTokenVerifier for token validation
* REST API endpoints for handling auth
* Planned JWT generation and user management (future scope)

---

## 📦 Getting Started

### Prerequisites

* Android Studio Giraffe or later
* JDK 17+
* Spring Boot 3.2+
* Google Cloud OAuth 2.0 credentials (Web + Android)

### Clone the repo

```bash
git clone https://github.com/nishchaymakkar/OneTapSignInDemo.git
```
