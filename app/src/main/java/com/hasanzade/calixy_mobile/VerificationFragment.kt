package com.hasanzade.calixy_mobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hasanzade.calixy_mobile.databinding.FragmentLoginVerificationBinding
import kotlinx.coroutines.launch

class VerificationFragment : Fragment() {

    private var _binding: FragmentLoginVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VerificationViewModel by viewModels {
        AuthViewModelFactory(
            AppModule.provideAuthRepository(requireContext())
        )
    }

    private lateinit var otpInputs: Array<EditText>
    private var emailArg: String = ""
    private var isFromSignUpArg: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getArgumentsFromBundle()
        initOtpInputs()
        setupOtpInputs()
        setupClickListeners()
        observeViewModel()
        updateEmailDisplay()

        // API-yə request atmırıq, backend register/forgot-password zamanı
        // kodu artıq email-ə göndərib, sadəcə timer-i başladırıq
        viewModel.startVerification(emailArg)
    }

    private fun getArgumentsFromBundle() {
        arguments?.let { bundle ->
            emailArg = bundle.getString("email", "")
            isFromSignUpArg = bundle.getBoolean("isFromSignUp", false)
        }
    }

    private fun initOtpInputs() {
        otpInputs = arrayOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
            binding.otp5,
            binding.otp6
        )
    }

    private fun setupOtpInputs() {
        otpInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Növbəti inputa keç
                    if (s?.length == 1 && index < otpInputs.size - 1) {
                        otpInputs[index + 1].requestFocus()
                    }
                    otpInputs.forEach { it.setBackgroundResource(R.drawable.otp_box_normal) }
                    updateOtpCode()
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_DEL && editText.text.isEmpty() && index > 0) {
                    otpInputs[index - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun updateOtpCode() {
        val code = otpInputs.joinToString("") { it.text.toString() }
        viewModel.updateOtpCode(code)
    }

    private fun setupClickListeners() {
        binding.verifyButton.setOnClickListener {
            viewModel.verifyOtp(isFromSignUpArg)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.resendCodeText.setOnClickListener {
            viewModel.resendCode(emailArg)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.verificationState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        binding.verifyButton.isEnabled = false
                        binding.verifyButton.text = "Verifying..."
                    }
                    is AuthResult.Success -> {
                        binding.verifyButton.isEnabled = true
                        binding.verifyButton.text = "Verify"
                        navigateOnSuccess()
                        viewModel.resetVerificationState()
                    }
                    is AuthResult.Error -> {
                        binding.verifyButton.isEnabled = true
                        binding.verifyButton.text = "Verify"
                        if (result.message == "Wrong") {
                            otpInputs.forEach { it.setBackgroundResource(R.drawable.otp_box_error) }
                        }
                    }
                    else -> {
                        binding.verifyButton.isEnabled = true
                        binding.verifyButton.text = "Verify"
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.resendTimer.collect { seconds ->
                if (seconds > 0) {
                    binding.timerText.visibility = View.VISIBLE
                    binding.timerText.text = "00:${String.format("%02d", seconds)}"
                } else {
                    binding.timerText.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.canResend.collect { canResend ->
                binding.resendCodeText.isEnabled = canResend
                binding.resendCodeText.alpha = if (canResend) 1.0f else 0.5f
            }
        }
    }

    private fun navigateOnSuccess() {
        if (isFromSignUpArg) {
            // Register flow → login ekranına
            findNavController().navigate(
                R.id.action_verificationFragment_to_loginFragment
            )
        } else {
            // Forgot password flow → reset password ekranına
            // OTP kodunu da bundle-a əlavə edirik (ResetPassword üçün lazımdır)
            val bundle = Bundle().apply {
                putString("email", emailArg)
                putString("code", viewModel.otpCode.value)
            }
            findNavController().navigate(
                R.id.action_verificationFragment_to_resetPasswordFragment, bundle
            )
        }
    }

    private fun updateEmailDisplay() {
        binding.emailDisplay.text = "Enter the code we've sent to\n$emailArg"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}