package yug.ramoliya.ojtapp.data

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import yug.ramoliya.ojtapp.data.model.RegisterRequest
import yug.ramoliya.ojtapp.data.model.StudentIndicatorRequest
import yug.ramoliya.ojtapp.data.model.StudentIndicatorResponse
import yug.ramoliya.ojtapp.data.model.TokenResponse
import yug.ramoliya.ojtapp.data.model.UserResponse
import yug.ramoliya.ojtapp.network.RetrofitClient

class StudentRepository(context: Context) {
    private val app = context.applicationContext
    private val tokenStore = TokenStore(app)
    private val api get() = RetrofitClient.studentApi(app)

    fun getStoredToken(): String? = tokenStore.getToken()

    suspend fun register(name: String, email: String, password: String): TokenResponse {
        tokenStore.setToken(null)
        val token = api.register(RegisterRequest(name, email, password, role = "student"))
        tokenStore.setToken(token.accessToken)
        return token
    }

    suspend fun login(email: String, password: String): TokenResponse {
        tokenStore.setToken(null)
        val token = api.login(username = email, password = password)
        tokenStore.setToken(token.accessToken)
        return token
    }

    fun logout() {
        tokenStore.setToken(null)
        RetrofitClient.reset()
    }

    suspend fun me(): UserResponse = api.getMe()

    suspend fun health() = api.health()

    suspend fun submitIndicators(indicatorsJson: String, modelName: String? = null): StudentIndicatorResponse {
        val obj = JsonParser.parseString(indicatorsJson).asJsonObject
        return api.submitIndicators(StudentIndicatorRequest(indicators = obj, modelName = modelName))
    }

    suspend fun submitIndicatorsObject(indicators: JsonObject, modelName: String? = null): StudentIndicatorResponse {
        return api.submitIndicators(StudentIndicatorRequest(indicators = indicators, modelName = modelName))
    }

    suspend fun history(range: String = "weekly"): List<StudentIndicatorResponse> = api.getHistory(range)
}
