package com.hasanzade.calixy_mobile

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class VerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _verificationState = MutableStateFlow<AuthResult?>(null)
    val verificationState: StateFlow<AuthResult?> = _verificationState

    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer

    private val _canResend = MutableStateFlow(true)
    val canResend: StateFlow<Boolean> = _canResend

    private var countDownTimer: CountDownTimer? = null

    fun updateOtpCode(code: String) {
        _otpCode.value = code
    }

    fun verifyOtp() {
        if (_otpCode.value.length == 6) {
            viewModelScope.launch {
                authRepository.checkEmailVerification().collect {
                    _verificationState.value = it
                }
            }
        } else {
            _verificationState.value = AuthResult.Error("Wrong")
        }
    }

    fun resendCode() {
        if (_canResend.value) {
            viewModelScope.launch {
                authRepository.resendEmailVerification().collect {
                    if (it is AuthResult.Success) {
                        startResendTimer()
                    }
                    _verificationState.value = it
                }
            }
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