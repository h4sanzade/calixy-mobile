package com.hasanzade.calixy_mobile

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class VerificationViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _verificationState = MutableStateFlow<AuthResult?>(null)
    val verificationState: StateFlow<AuthResult?> = _verificationState

    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer

    private val _canResend = MutableStateFlow(false)
    val canResend: StateFlow<Boolean> = _canResend

    private var generatedOtp: String = ""
    private var countDownTimer: CountDownTimer? = null

    // EmailJS məlumatları
    private val SERVICE_ID = "service_27622nu"
    private val TEMPLATE_ID = "template_6pael3j"
    private val PUBLIC_KEY = "JcdB7ShaRn9j4uuK-"

    fun sendOtpEmail(email: String) {
        generatedOtp = (100000..999999).random().toString()
        viewModelScope.launch {
            _verificationState.value = AuthResult.Loading
            try {
                sendEmailViaEmailJS(email, generatedOtp)
                _verificationState.value = null
                startResendTimer()
            } catch (e: Exception) {
                _verificationState.value = AuthResult.Error("Email göndərilmədi")
            }
        }
    }

    private suspend fun sendEmailViaEmailJS(email: String, otp: String) {
        withContext(Dispatchers.IO) {
            val jsonBody = JSONObject().apply {
                put("service_id", SERVICE_ID)
                put("template_id", TEMPLATE_ID)
                put("user_id", PUBLIC_KEY)
                put("template_params", JSONObject().apply {
                    put("to_email", email)
                    put("otp_code", otp)
                })
            }

            val client = OkHttpClient()
            val body = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(body)
                .build()
            client.newCall(request).execute()
        }
    }

    fun updateOtpCode(code: String) {
        _otpCode.value = code
    }

    fun verifyOtp() {
        when {
            _otpCode.value.length != 6 -> {
                _verificationState.value = AuthResult.Error("Wrong")
            }
            _otpCode.value == generatedOtp -> {
                _verificationState.value = AuthResult.Success
            }
            else -> {
                _verificationState.value = AuthResult.Error("Wrong")
            }
        }
    }

    fun resendCode(email: String) {
        if (_canResend.value) {
            sendOtpEmail(email)
        }
    }

    private fun startResendTimer() {
        _canResend.value = false
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _resendTimer.value = (millisUntilFinished / 1000).toInt()
            }
            override fun onFinish() {
                _resendTimer.value = 0
                _canResend.value = true
            }
        }.start()
    }

    fun resetVerificationState() {
        _verificationState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}