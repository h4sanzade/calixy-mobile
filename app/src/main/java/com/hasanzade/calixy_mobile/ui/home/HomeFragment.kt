package com.hasanzade.calixy_mobile.ui.home

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hasanzade.calixy_mobile.AppModule
import com.hasanzade.calixy_mobile.R
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(AppModule.provideAuthRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val welcomeText = TextView(requireContext()).apply {
            text = "Welcome Home!"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val logoutButton = Button(requireContext()).apply {
            text = "Log Out"
            setOnClickListener {
                lifecycleScope.launch {
                    viewModel.logout()
                    findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                }
            }
        }

        layout.addView(welcomeText)
        layout.addView(logoutButton)
        return layout
    }
}