package com.hasanzade.calixy_mobile

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    private var currentEmail: String = ""
    private var countDownTimer: CountDownTimer? = null

    // Register sonrası verification başlayanda çağırılır
    fun startVerification(email: String) {
        currentEmail = email
        startResendTimer()
    }

    // Forgot password sonrası da eyni ekran istifadə olunur
    fun sendOtpEmail(email: String) {
        currentEmail = email
        startResendTimer()
    }

    fun updateOtpCode(code: String) {
        _otpCode.value = code
    }

    fun verifyOtp(isFromSignUp: Boolean) {
        if (_otpCode.value.length != 6) {
            _verificationState.value = AuthResult.Error("Wrong")
            return
        }
        viewModelScope.launch {
            authRepository.verifyEmail(currentEmail, _otpCode.value).collect {
                _verificationState.value = it
            }
        }
    }

    fun resendCode(email: String) {
        if (!_canResend.value) return
        currentEmail = email
        viewModelScope.launch {
            authRepository.resendVerification(email).collect { result ->
                if (result is AuthResult.Success) {
                    startResendTimer()
                } else if (result is AuthResult.Error) {
                    _verificationState.value = result
                }
            }
        }
    }

    private fun startResendTimer() {
        _canResend.value = false
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60_000, 1_000) {
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