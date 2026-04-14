package yug.ramoliya.ojtapp.data

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() }

    fun setToken(token: String?) {
        prefs.edit().apply {
            if (token.isNullOrBlank()) remove(KEY_TOKEN)
            else putString(KEY_TOKEN, token)
        }.apply()
    }

    companion object {
        private const val PREFS = "smh_student_auth"
        private const val KEY_TOKEN = "access_token"
    }
}
