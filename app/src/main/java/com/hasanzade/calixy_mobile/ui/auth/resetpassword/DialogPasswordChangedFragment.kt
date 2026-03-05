package com.hasanzade.calixy_mobile.ui.auth.resetpassword

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.hasanzade.calixy_mobile.R
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
    }

    private fun setupDialog() {
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        isCancelable = false
    }

    private fun setupClickListener(view: View) {
        view.findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        lifecycleScope.launch {
            try {
                dismiss()
                delay(100)
                findNavController().navigate(R.id.action_dialogPasswordChangedFragment_to_loginFragment)
            } catch (e: Exception) {
                try {
                    findNavController().popBackStack(R.id.loginFragment, false)
                } catch (ex: Exception) {
                    // ignore
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}