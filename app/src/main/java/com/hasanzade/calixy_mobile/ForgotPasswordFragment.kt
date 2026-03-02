package com.hasanzade.calixy_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hasanzade.calixy_mobile.databinding.FragmentForgotPasswordBinding
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            AppModule.provideAuthRepository(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetAuthState()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.resetLinkButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            viewModel.sendPasswordReset(email)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.backToLoginText.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        binding.resetLinkButton.isEnabled = false
                        binding.resetLinkButton.text = "Sending..."
                        clearEmailError()
                    }
                    is AuthResult.Success -> {
                        binding.resetLinkButton.isEnabled = true
                        binding.resetLinkButton.text = "Send Reset Link"
                        clearEmailError()

                        val email = binding.emailEditText.text.toString().trim()
                        val bundle = Bundle().apply {
                            putString("email", email)
                            putBoolean("isFromSignUp", false) // forgot password flow
                        }
                        // ✅ Əvvəlcə OTP ekranına yönləndir
                        findNavController().navigate(
                            R.id.action_forgotPasswordFragment_to_verificationFragment, bundle
                        )
                        viewModel.resetAuthState()
                    }
                    is AuthResult.Error -> {
                        binding.resetLinkButton.isEnabled = true
                        binding.resetLinkButton.text = "Send Reset Link"
                        showEmailError(result.message)
                    }
                    else -> {
                        binding.resetLinkButton.isEnabled = true
                        binding.resetLinkButton.text = "Send Reset Link"
                    }
                }
            }
        }
    }

    private fun showEmailError(message: String) {
        binding.emailInputLayout.error = message
        binding.emailInputLayout.boxStrokeColor =
            ContextCompat.getColor(requireContext(), R.color.red)
    }

    private fun clearEmailError() {
        binding.emailInputLayout.error = null
        binding.emailInputLayout.boxStrokeColor =
            ContextCompat.getColor(requireContext(), R.color.gray)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}