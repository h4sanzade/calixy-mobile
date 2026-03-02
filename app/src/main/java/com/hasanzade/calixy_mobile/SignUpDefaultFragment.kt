package com.hasanzade.calixy_mobile

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hasanzade.calcueai.databinding.FragmentSignUpDefaultBinding
import com.hasanzade.calixy_mobile.databinding.FragmentSignUpDefaultBinding
import kotlinx.coroutines.launch

class SignUpDefaultFragment : Fragment() {

    private var _binding: FragmentSignUpDefaultBinding? = null
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
        _binding = FragmentSignUpDefaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.resetAuthState()
        setupLoginText()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupLoginText() {
        val fullText = "Already have an account? Log in"
        val spannableString = SpannableString(fullText)
        val orangeColor = ContextCompat.getColor(requireContext(), R.color.orange)

        val startIndex = fullText.indexOf("Log in")
        val endIndex = startIndex + "Log in".length

        spannableString.setSpan(
            ForegroundColorSpan(orangeColor),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.loginText.text = spannableString
    }

    private fun setupClickListeners() {
        binding.signUpButton.setOnClickListener {
            val fullName = binding.fullNameEditText.text.toString().trim()
            val email = binding.emailSignupEditText.text.toString().trim()
            val password = binding.passwordSignupEditText.text.toString().trim()
            viewModel.signUp(email, password, fullName)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.loginText.setOnClickListener {
            findNavController().navigate(R.id.action_signUpDefaultFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        binding.signUpButton.isEnabled = false
                        binding.signUpButton.text = "Creating Account..."
                    }
                    is AuthResult.Success -> {
                        binding.signUpButton.isEnabled = true
                        binding.signUpButton.text = "Sign Up"

                        val email = binding.emailSignupEditText.text.toString().trim()
                        val bundle = Bundle().apply {
                            putString("email", email)
                            putBoolean("isFromSignUp", true)
                        }
                        viewModel.resetAuthState()
                        findNavController().navigate(R.id.action_signUpDefaultFragment_to_verificationFragment, bundle)
                    }
                    is AuthResult.Error -> {
                        binding.signUpButton.isEnabled = true
                        binding.signUpButton.text = "Sign Up"
                    }
                    else -> {
                        binding.signUpButton.isEnabled = true
                        binding.signUpButton.text = "Sign Up"
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.signUpValidation.collect { validation ->
                if (validation.nameError != null) {
                    binding.fullNameLayout.error = validation.nameError
                    binding.fullNameLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    binding.fullNameLayout.error = null
                    binding.fullNameLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray)
                }

                if (validation.emailError != null) {
                    binding.emailSignupLayout.error = validation.emailError
                    binding.emailSignupLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    binding.emailSignupLayout.error = null
                    binding.emailSignupLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray)
                }

                if (validation.passwordError != null) {
                    binding.passwordSignupLayout.error = validation.passwordError
                    binding.passwordSignupLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    binding.passwordSignupLayout.error = null
                    binding.passwordSignupLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}