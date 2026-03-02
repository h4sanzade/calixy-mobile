package com.hasanzade.calixy_mobile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private val viewModel: SplashViewModel by viewModels {
        SplashViewModelFactory(
            AppModule.provideUserPreferences(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startAnimation(view)
        observeNavigation()
    }

    private fun startAnimation(view: View) {
        val calCueText = view.findViewById<View>(R.id.CalCueId)
        val aiText = view.findViewById<View>(R.id.ai_text)
        val sloganText = view.findViewById<View>(R.id.slogan_text)

        calCueText.alpha = 0f
        aiText.alpha = 0f
        sloganText.alpha = 0f
        sloganText.translationY = 100f

        val fadeInCalCue = ObjectAnimator.ofFloat(calCueText, "alpha", 0f, 1f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fadeInAi = ObjectAnimator.ofFloat(aiText, "alpha", 0f, 1f).apply {
            duration = 800
            startDelay = 500
            interpolator = AccelerateDecelerateInterpolator()
        }
        val slideUpSlogan = ObjectAnimator.ofFloat(sloganText, "translationY", 100f, 0f).apply {
            duration = 800
            startDelay = 1000
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fadeInSlogan = ObjectAnimator.ofFloat(sloganText, "alpha", 0f, 1f).apply {
            duration = 800
            startDelay = 1000
            interpolator = AccelerateDecelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(fadeInCalCue, fadeInAi, slideUpSlogan, fadeInSlogan)
            start()
        }
    }

    private fun observeNavigation() {
        lifecycleScope.launch {
            viewModel.navigationState.collect { state ->
                when (state) {
                    is SplashNavigationState.NavigateToOnboarding ->
                        findNavController().navigate(R.id.action_splashScreenFragment_to_firstOnBoardingFragment)
                    is SplashNavigationState.NavigateToLogin ->
                        findNavController().navigate(R.id.action_splashScreenFragment_to_loginFragment)
                    is SplashNavigationState.NavigateToMain ->
                        findNavController().navigate(R.id.action_splashScreenFragment_to_firstOnBoardingFragment)
                    else -> {}
                }
            }
        }
    }
}