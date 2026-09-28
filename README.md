# FindIT
A modern Lost and Found mobile application and RESTful API system built with Jetpack Compose, Kotlin, Laravel, and MySQL.

<p align="center">
  <img src="https://raw.githubusercontent.com/SltnBM/findit/main/FindIT/app/src/main/res/drawable/logo_findit.png" width="220" alt="FindIT Logo">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Laravel-12-FF2D20?logo=laravel&logoColor=white" alt="Laravel">
  <img src="https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Room_Database-4285F4?logo=sqlite&logoColor=white" alt="Room Database">
</p>

---

## Features
- Role-based authentication (User & Admin) powered by Laravel Sanctum
- Lost & found item discovery with real-time search and category filtering
- Detailed item view including photos, characteristics, discovery locations, and pickup points
- Claim verification workflow with evidence photo capture and status updates (`draft`, `published`, `closed`)
- Admin management dashboard with item CRUD, status publishing, and category management
- Activity log audit trail for administrative tracking
- Offline-first caching with Room Database encrypted via SQLCipher
- Security protections with root device detection and screenshot prevention (`FLAG_SECURE`)

---

## Technologies Used
- Android (SDK 36)
- Kotlin
- Jetpack Compose & Material 3
- Room Database & SQLCipher
- Jetpack DataStore Preferences
- Retrofit 2 & OkHttp
- Coil
- Laravel 12
- PHP 8.2+
- Laravel Sanctum
- MySQL

---

## How to Use

### 1. Clone the repository
```bash
git clone https://github.com/SltnBM/findit.git
cd findit
```

### 2. Setup Backend API (`findit-api`)
```bash
cd findit-api
```

Install backend dependencies:
```bash
composer install
```

Create the environment file:
```bash
cp .env.example .env
```

Generate the application key:
```bash
php artisan key:generate
```

Configure MySQL in `.env`:
```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=findit_db
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
```

Run migrations and seeders:
```bash
php artisan migrate --seed
```

Create storage link for item images:
```bash
php artisan storage:link
```

Start the development server:
```bash
php artisan serve --host=0.0.0.0 --port=8000
```

### 3. Setup Android Client (`FindIT`)
1. Open the `FindIT` project folder in **Android Studio**.
2. Sync the project with Gradle files.
3. Configure the base API URL in `app/src/main/java/com/sultan/findit/data/remote/RetrofitClient.kt`:
   - For Android Emulator: `http://10.0.2.2:8000/api/`
   - For Physical Device: `http://<YOUR_LOCAL_IP>:8000/api/`
4. Build and run the app on an Android device or emulator.

---

## Connect with Me
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Sultan%20Badra-blue?logo=linkedin\&logoColor=white\&style=flat-square)](https://www.linkedin.com/in/sultan-badra)

---

## License
This project is open-source and available under the MIT [LICENSE](./LICENSE).
