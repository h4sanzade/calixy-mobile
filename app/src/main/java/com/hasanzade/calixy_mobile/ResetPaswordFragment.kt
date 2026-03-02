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
import com.hasanzade.calixy_mobile.databinding.FragmentResetPaswordBinding
import kotlinx.coroutines.launch

class ResetPaswordFragment : Fragment() {

    private var _binding: FragmentResetPaswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResetPasswordViewModel by viewModels {
        AuthViewModelFactory(
            FirebaseModule.provideAuthRepository(requireContext())
        )
    }

    private var emailArg: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPaswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetState()
        getArgumentsFromBundle()
        setupClickListeners()
        observeViewModel()
    }

    private fun getArgumentsFromBundle() {
        arguments?.let { bundle ->
            emailArg = bundle.getString("email", "")
        }
    }

    private fun setupClickListeners() {
        binding.resetPasswordButton.setOnClickListener {
            val newPassword = binding.newPasswordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            viewModel.resetPassword(emailArg, newPassword, confirmPassword)
        }
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.resetPasswordState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        binding.resetPasswordButton.isEnabled = false
                        binding.resetPasswordButton.text = "Resetting..."
                    }
                    is AuthResult.Success -> {
                        binding.resetPasswordButton.isEnabled = true
                        binding.resetPasswordButton.text = "Reset Password"
                        viewModel.resetState()
                        findNavController().navigate(R.id.action_resetPasswordFragment_to_dialogPasswordChangedFragment)
                    }
                    is AuthResult.Error -> {
                        binding.resetPasswordButton.isEnabled = true
                        binding.resetPasswordButton.text = "Reset Password"
                    }
                    else -> {
                        binding.resetPasswordButton.isEnabled = true
                        binding.resetPasswordButton.text = "Reset Password"
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.passwordValidation.collect { validation ->
                if (validation.newPasswordError != null) {
                    binding.newPasswordLayout.error = validation.newPasswordError
                    binding.newPasswordLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    binding.newPasswordLayout.error = null
                    binding.newPasswordLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray)
                }
                if (validation.confirmPasswordError != null) {
                    binding.confirmPasswordLayout.error = validation.confirmPasswordError
                    binding.confirmPasswordLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    binding.confirmPasswordLayout.error = null
                    binding.confirmPasswordLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}