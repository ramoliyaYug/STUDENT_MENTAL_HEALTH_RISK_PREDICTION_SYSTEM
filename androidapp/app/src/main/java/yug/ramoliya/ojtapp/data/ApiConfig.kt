package yug.ramoliya.ojtapp.data

/**
 * Central place for the backend API base URL (Retrofit [retrofit2.Retrofit.baseUrl]).
 *
 * Must end with `/`. Paths in [yug.ramoliya.ojtapp.network.StudentApiService] are relative (no leading `/`).
 *
 * - **Android emulator** → host machine: `http://10.0.2.2:8000/api/v1/`
 * - **Physical device** → your PC’s LAN IP, e.g. `http://192.168.1.10:8000/api/v1/`
 * - **HTTPS production** → e.g. `https://api.example.com/api/v1/`
 */
object ApiConfig {
    const val BASE_URL: String = "http://13.205.252.252:8000/api/v1/"
}
