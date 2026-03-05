package com.hasanzade.calixy_mobile.ui.auth.verification

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hasanzade.calixy_mobile.AppModule
import com.hasanzade.calixy_mobile.domain.model.AuthResult
import com.hasanzade.calixy_mobile.R
import com.hasanzade.calixy_mobile.databinding.FragmentLoginVerificationBinding
import com.hasanzade.calixy_mobile.ui.auth.login.AuthViewModelFactory
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
        // ✅ repeatOnLifecycle istifadə et - fragment detach olduqda collect dayansın
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.verificationState.collect { result ->
                    when (result) {
                        is AuthResult.Loading -> {
                            binding.verifyButton.isEnabled = false
                            binding.verifyButton.text = "Verifying..."
                        }
                        is AuthResult.Success -> {
                            binding.verifyButton.isEnabled = true
                            binding.verifyButton.text = "Verify"
                            viewModel.resetVerificationState()
                            // ✅ Fragment hələ attached-dirmi yoxla
                            if (isAdded && _binding != null) {
                                navigateOnSuccess()
                            }
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
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resendTimer.collect { seconds ->
                    if (_binding == null) return@collect
                    if (seconds > 0) {
                        binding.timerText.visibility = View.VISIBLE
                        binding.timerText.text = "00:${String.format("%02d", seconds)}"
                    } else {
                        binding.timerText.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canResend.collect { canResend ->
                    if (_binding == null) return@collect
                    binding.resendCodeText.isEnabled = canResend
                    binding.resendCodeText.alpha = if (canResend) 1.0f else 0.5f
                }
            }
        }
    }

    private fun navigateOnSuccess() {
        try {
            val navController = findNavController()
            if (isFromSignUpArg) {
                // ✅ Sign-up flow: login-ə get
                navController.navigate(R.id.action_verificationFragment_to_loginFragment)
            } else {
                // ✅ Forgot password flow: OTP kodunu bundle ilə reset password-a göndər
                val code = viewModel.otpCode.value
                val bundle = Bundle().apply {
                    putString("email", emailArg)
                    putString("code", code)
                }
                navController.navigate(
                    R.id.action_verificationFragment_to_resetPasswordFragment, bundle
                )
            }
        } catch (e: Exception) {
            // Navigation artıq baş vermişsə və ya fragment detach olubsa ignore et
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