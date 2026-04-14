package yug.ramoliya.ojtapp.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import yug.ramoliya.ojtapp.data.ApiConfig
import yug.ramoliya.ojtapp.data.TokenStore
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var service: StudentApiService? = null

    private val lock = Any()

    fun studentApi(context: Context): StudentApiService {
        service?.let { return it }
        synchronized(lock) {
            service?.let { return it }
            val tokenStore = TokenStore(context.applicationContext)
            val gson: Gson = GsonBuilder()
                .setLenient()
                .create()

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(tokenStore))
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val created = retrofit.create(StudentApiService::class.java)
            service = created
            return created
        }
    }

    /** Call after logout if you need a fresh client with cleared interceptors state (token is read per request). */
    fun reset() {
        synchronized(lock) {
            service = null
        }
    }
}
