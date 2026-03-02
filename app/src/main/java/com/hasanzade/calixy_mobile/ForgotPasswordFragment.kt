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
            FirebaseModule.provideAuthRepository(requireContext())
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
                    }
                    is AuthResult.Success -> {
                        binding.resetLinkButton.isEnabled = true
                        binding.resetLinkButton.text = "Send Reset Link"

                        val email = binding.emailEditText.text.toString().trim()
                        val bundle = Bundle().apply {
                            putString("email", email)
                            putBoolean("isFromSignUp", false)
                        }
                        viewModel.resetAuthState()
                        findNavController().navigate(R.id.action_forgotPasswordFragment_to_verificationFragment, bundle)
                    }
                    is AuthResult.Error -> {
                        binding.resetLinkButton.isEnabled = true
                        binding.resetLinkButton.text = "Send Reset Link"

                        if (result.message.contains("email")) {
                            binding.emailInputLayout.error = result.message
                            binding.emailInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                        }
                    }
                    else -> {
                        binding.resetLinkButton.isEnabled = true
                        binding.resetLinkButton.text = "Send Reset Link"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}