package com.hasanzade.calcueai

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.hasanzade.calixy_mobile.R
import com.hasanzade.calixy_mobile.ResetPaswordFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DialogPasswordChangedFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_password_changed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
        setupClickListener(view)
        makeBackgroundBlur()
    }

    private fun setupDialog() {
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            // Make dialog not cancelable
            isCancelable = false

            // Set dialog to appear above everything
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
        }
    }

    private fun setupClickListener(view: View) {
        view.findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            navigateToLogin()
        }
    }

    private fun makeBackgroundBlur() {
        // Make the background fragment appear blurred/dimmed
        val parentFragment = parentFragmentManager.fragments.find {
            it is ResetPaswordFragment
        }
        parentFragment?.view?.alpha = 0.3f
    }

    private fun navigateToLogin() {
        lifecycleScope.launch {
            try {
                // Restore background fragment alpha
                val parentFragment = parentFragmentManager.fragments.find {
                    it is ResetPaswordFragment
                }
                parentFragment?.view?.alpha = 1.0f

                // Small delay for better UX
                delay(200)

                // Navigate to login and clear back stack
                findNavController().navigate(
                    R.id.action_dialogPasswordChangedFragment_to_loginFragment
                )
            } catch (e: Exception) {
                // Fallback navigation
                findNavController().popBackStack(R.id.loginFragment, false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Restore background fragment alpha when dialog is destroyed
        val parentFragment = parentFragmentManager.fragments.find {
            it is ResetPaswordFragment
        }
        parentFragment?.view?.alpha = 1.0f
    }
}