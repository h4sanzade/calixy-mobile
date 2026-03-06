package com.hasanzade

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.hasanzade.calixy_mobile.AppModule
import com.hasanzade.calixy_mobile.R
import com.hasanzade.calixy_mobile.databinding.FragmentSetupProfileFirstBinding
import com.hasanzade.calixy_mobile.ui.setup.SetupProfileViewModel
import com.hasanzade.calixy_mobile.ui.setup.SetupProfileViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SetupProfileFirstFragment : Fragment() {

    private var _binding: FragmentSetupProfileFirstBinding? = null
    private val binding get() = _binding!!

    private val setupViewModel: SetupProfileViewModel by activityViewModels {
        SetupProfileViewModelFactory(AppModule.provideAuthRepository(requireContext()))
    }

    private val genderOptions = listOf("Male", "Female", "Prefer not to say")
    private val genderApiValues = listOf("MALE", "FEMALE", "OTHER")
    private var selectedGenderIndex: Int = -1
    private var selectedDateMillis: Long? = null
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { openCropDialog(it) }
    }

    private fun openCropDialog(uri: Uri) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val cropView = com.canhub.cropper.CropImageView(requireContext())

        cropView.apply {
            setImageUriAsync(uri)
            setAspectRatio(1, 1)
            setFixedAspectRatio(true)
            cropShape = com.canhub.cropper.CropImageView.CropShape.OVAL
            guidelines = com.canhub.cropper.CropImageView.Guidelines.ON
            setBackgroundColor(android.graphics.Color.parseColor("#171717"))
        }

        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#171717"))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        val toolbar = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
            layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 120)
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(24, 0, 24, 0)
        }

        val cancelBtn = android.widget.TextView(requireContext()).apply {
            text = "Cancel"
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { dialog.dismiss() }
        }

        val titleTv = android.widget.TextView(requireContext()).apply {
            text = "Crop Photo"
            textSize = 17f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
        }

        val doneBtn = android.widget.TextView(requireContext()).apply {
            text = "Done"
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
            gravity = android.view.Gravity.END
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                val croppedBitmap = cropView.getCroppedImage(512, 512)
                if (croppedBitmap != null) {
                    saveCroppedImage(croppedBitmap)
                    dialog.dismiss()
                }
            }
        }

        toolbar.addView(cancelBtn)
        toolbar.addView(titleTv)
        toolbar.addView(doneBtn)
        cropView.layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        layout.addView(toolbar)
        layout.addView(cropView)
        dialog.setContentView(layout)
        dialog.show()
    }

    private fun saveCroppedImage(bitmap: Bitmap) {
        lifecycleScope.launch {
            val ovalBitmap = makeOvalBitmap(bitmap)
            val base64 = withContext(Dispatchers.IO) {
                try {
                    val stream = ByteArrayOutputStream()
                    ovalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                } catch (e: Exception) { "" }
            }
            setupViewModel.profileImageBase64 = base64
            binding.userProfileImage.setImageBitmap(ovalBitmap)
            binding.addProfilePictInfo.visibility = View.GONE
        }
    }

    private fun makeOvalBitmap(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        canvas.drawOval(android.graphics.RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        val centeredSrc = Bitmap.createBitmap(src, (src.width - size) / 2, (src.height - size) / 2, size, size)
        canvas.drawBitmap(centeredSrc, 0f, 0f, paint)
        return output
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSetupProfileFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGenderDropdown()
        setupDatePicker()
        setupClickListeners()
    }

    private fun setupGenderDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.genderAutoComplete.setAdapter(adapter)
        binding.genderAutoComplete.setOnItemClickListener { _, _, position, _ ->
            selectedGenderIndex = position
            binding.genderInputLayout.error = null
        }
    }

    private fun setupDatePicker() {
        binding.dobCard.setOnClickListener { showDatePicker() }
        binding.calendarLayout.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .setEnd(Calendar.getInstance().timeInMillis)
            .build()

        val openAt = selectedDateMillis ?: Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000); set(Calendar.MONTH, Calendar.JANUARY); set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth")
            .setSelection(openAt)
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            selectedDateMillis = millis
            binding.dobText.text = displayFormat.format(millis)
            binding.dobText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        picker.show(parentFragmentManager, "DOB_PICKER")
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_setupProfileFirstFragment_to_loginFragment)
        }

        binding.userProfileImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.nextButton.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            setupViewModel.firstName = binding.firstNameEditText.text.toString().trim()
            setupViewModel.lastName = binding.lastNameEditText.text.toString().trim()
            setupViewModel.gender = genderApiValues[selectedGenderIndex]
            setupViewModel.dateOfBirth = apiDateFormat.format(selectedDateMillis!!)

            findNavController().navigate(R.id.action_setupProfileFirstFragment_to_homeFragment)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val firstName = binding.firstNameEditText.text.toString().trim()
        val lastName = binding.lastNameEditText.text.toString().trim()

        if (firstName.isEmpty()) {
            binding.firstNameEt.error = "First name is required"
            isValid = false
        } else {
            binding.firstNameEt.error = null
        }

        if (lastName.isEmpty()) {
            binding.lastNameEt.error = "Last name is required"
            isValid = false
        } else {
            binding.lastNameEt.error = null
        }

        if (selectedGenderIndex == -1) {
            binding.genderInputLayout.error = "Please select your gender"
            isValid = false
        } else {
            binding.genderInputLayout.error = null
        }

        if (selectedDateMillis == null) {
            binding.dobText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            binding.dobText.text = "Please select date of birth"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = SetupProfileFirstFragment()
    }
}