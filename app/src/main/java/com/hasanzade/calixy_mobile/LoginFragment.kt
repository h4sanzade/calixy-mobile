package com.hasanzade.calixy_mobile

import android.app.Activity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.hasanzade.calixy_mobile.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            AppModule.provideAuthRepository(requireContext())
        )
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken)
            } else {
                Toast.makeText(requireContext(), "Google token alınmadı", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Google giriş uğursuz: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGoogleSignIn()
        viewModel.resetAuthState()
        setupSignUpText()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupSignUpText() {
        val fullText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(fullText)
        val orangeColor = ContextCompat.getColor(requireContext(), R.color.orange)
        val startIndex = fullText.indexOf("Sign Up")
        val endIndex = startIndex + "Sign Up".length
        spannableString.setSpan(
            ForegroundColorSpan(orangeColor),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.signUpText.text = spannableString
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            viewModel.signIn(email, password)
        }

        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpDefaultFragment)
        }

        binding.googleButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        binding.loginButton.isEnabled = false
                        binding.loginButton.text = "Logging in..."
                    }
                    is AuthResult.Success -> {
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Log In"
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        viewModel.resetAuthState()
                    }
                    is AuthResult.Error -> {
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Log In"
                    }
                    else -> {
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Log In"
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.loginValidation.collect { validation ->
                if (validation.emailError != null) {
                    binding.emailInputLayout.error = validation.emailError
                    binding.emailInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    binding.emailInputLayout.error = null
                    binding.emailInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.gray)
                }
                if (validation.passwordError != null) {
                    binding.passwordInputLayout.error = validation.passwordError
                    binding.passwordInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    binding.passwordInputLayout.error = null
                    binding.passwordInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.gray)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.googleAuthState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        binding.googleButton.isEnabled = false
                    }
                    is AuthResult.Success -> {
                        binding.googleButton.isEnabled = true
                        viewModel.resetGoogleAuthState()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                    is AuthResult.Error -> {
                        binding.googleButton.isEnabled = true
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetGoogleAuthState()
                    }
                    else -> {
                        binding.googleButton.isEnabled = true
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