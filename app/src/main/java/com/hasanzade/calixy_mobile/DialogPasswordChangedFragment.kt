package com.hasanzade.calixy_mobile

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
            isCancelable = false
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
        val parentFragment = parentFragmentManager.fragments.find { it is ResetPaswordFragment }
        parentFragment?.view?.alpha = 0.3f
    }

    private fun navigateToLogin() {
        lifecycleScope.launch {
            try {
                val parentFragment = parentFragmentManager.fragments.find { it is ResetPaswordFragment }
                parentFragment?.view?.alpha = 1.0f
                delay(200)
                findNavController().navigate(R.id.action_dialogPasswordChangedFragment_to_loginFragment)
            } catch (e: Exception) {
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
        val parentFragment = parentFragmentManager.fragments.find { it is ResetPaswordFragment }
        parentFragment?.view?.alpha = 1.0f
    }
}