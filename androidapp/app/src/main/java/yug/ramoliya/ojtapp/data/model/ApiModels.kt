package yug.ramoliya.ojtapp.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer",
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "student",
)

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("created_at") val createdAt: String,
)

data class HealthResponse(
    val status: String? = null,
)

data class StudentIndicatorRequest(
    val indicators: JsonObject,
    @SerializedName("model_name") val modelName: String? = null,
)

data class StudentIndicatorResponse(
    val id: Int,
    @SerializedName("student_id") val studentId: Int,
    val indicators: JsonObject? = null,
    @SerializedName("risk_level") val riskLevel: String,
    val probability: Double? = null,
    @SerializedName("anxiety_score") val anxietyScore: Double? = null,
    @SerializedName("stress_score") val stressScore: Double? = null,
    @SerializedName("depression_score") val depressionScore: Double? = null,
    @SerializedName("anxiety_label") val anxietyLabel: String? = null,
    @SerializedName("stress_label") val stressLabel: String? = null,
    @SerializedName("depression_label") val depressionLabel: String? = null,
    val explainability: List<ShapValue>? = null,
    @SerializedName("model_used") val modelUsed: String,
    @SerializedName("created_at") val createdAt: String,
)

data class ShapValue(
    val feature: String,
    @SerializedName("shap_value") val shapValue: Double,
    @SerializedName("abs_shap_value") val absShapValue: Double,
)
