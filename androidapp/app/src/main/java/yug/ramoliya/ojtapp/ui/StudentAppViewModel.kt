package yug.ramoliya.ojtapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import yug.ramoliya.ojtapp.data.SampleIndicators
import yug.ramoliya.ojtapp.data.StudentRepository
import yug.ramoliya.ojtapp.data.model.StudentIndicatorResponse
import yug.ramoliya.ojtapp.data.model.UserResponse

class StudentAppViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(StudentAppViewModel::class.java)) {
                        return StudentAppViewModel(app) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val repo = StudentRepository(application)

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _profile = MutableStateFlow<UserResponse?>(null)
    val profile: StateFlow<UserResponse?> = _profile.asStateFlow()

    private val _healthText = MutableStateFlow<String?>(null)
    val healthText: StateFlow<String?> = _healthText.asStateFlow()

    private val _history = MutableStateFlow<List<StudentIndicatorResponse>>(emptyList())
    val history: StateFlow<List<StudentIndicatorResponse>> = _history.asStateFlow()

    private val _lastSubmit = MutableStateFlow<StudentIndicatorResponse?>(null)
    val lastSubmit: StateFlow<StudentIndicatorResponse?> = _lastSubmit.asStateFlow()

    val startRoute: String =
        if (repo.getStoredToken().isNullOrBlank()) "login" else "main"

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String) {
        _uiMessage.value = msg
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            try {
                repo.login(email.trim(), password)
                onSuccess()
            } catch (e: HttpException) {
                _uiMessage.value = e.response()?.errorBody()?.string() ?: e.message
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: e.toString()
            } finally {
                _busy.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            try {
                repo.register(name.trim(), email.trim(), password)
                onSuccess()
            } catch (e: HttpException) {
                _uiMessage.value = e.response()?.errorBody()?.string() ?: e.message
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: e.toString()
            } finally {
                _busy.value = false
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        repo.logout()
        _profile.value = null
        _history.value = emptyList()
        _lastSubmit.value = null
        onDone()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _busy.value = true
            try {
                _profile.value = repo.me()
            } catch (e: HttpException) {
                _uiMessage.value = e.response()?.errorBody()?.string() ?: e.message
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: e.toString()
            } finally {
                _busy.value = false
            }
        }
    }

    fun pingHealth() {
        viewModelScope.launch {
            _busy.value = true
            try {
                val h = repo.health()
                _healthText.value = "status=${h.status}"
            } catch (e: Exception) {
                _healthText.value = e.message
            } finally {
                _busy.value = false
            }
        }
    }

    fun refreshHistory(range: String = "weekly") {
        viewModelScope.launch {
            _busy.value = true
            try {
                _history.value = repo.history(range)
            } catch (e: HttpException) {
                _uiMessage.value = e.response()?.errorBody()?.string() ?: e.message
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: e.toString()
            } finally {
                _busy.value = false
            }
        }
    }

    fun submitIndicatorsJson(json: String) {
        viewModelScope.launch {
            _busy.value = true
            try {
                _lastSubmit.value = repo.submitIndicators(json.trim())
                _uiMessage.value = "Submitted. Risk: ${_lastSubmit.value?.riskLevel}"
            } catch (e: HttpException) {
                _uiMessage.value = e.response()?.errorBody()?.string() ?: e.message
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: e.toString()
            } finally {
                _busy.value = false
            }
        }
    }

    fun loadSampleJson(): String = SampleIndicators.asPrettyJson()
}
