package com.hasanzade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.hasanzade.calixy_mobile.AppModule
import com.hasanzade.calixy_mobile.R
import com.hasanzade.calixy_mobile.domain.model.AuthResult
import com.hasanzade.calixy_mobile.ui.setup.SetupProfileViewModel
import com.hasanzade.calixy_mobile.ui.setup.SetupProfileViewModelFactory
import kotlinx.coroutines.launch

data class ActivityOption(
    val apiValue: String,
    val title: String,
    val description: String
)

class SetupProfileFifth : Fragment() {

    private var selectedOption: ActivityOption? = null
    private lateinit var optionAdapter: ActivityAdapter

    private val setupViewModel: SetupProfileViewModel by activityViewModels {
        SetupProfileViewModelFactory(AppModule.provideAuthRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_setup_profile_fifth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.activity_recycler)
        val backTop = view.findViewById<View>(R.id.back_button)
        val nextButton = view.findViewById<MaterialButton>(R.id.next_button)

        val stepProgress = view.findViewById<View>(R.id.view_step_progress_id)
        stepProgress.findViewById<View>(R.id.step1).setBackgroundColor(android.graphics.Color.WHITE)
        stepProgress.findViewById<View>(R.id.step2).setBackgroundColor(android.graphics.Color.WHITE)
        stepProgress.findViewById<View>(R.id.step3).setBackgroundColor(android.graphics.Color.WHITE)
        stepProgress.findViewById<View>(R.id.step4).setBackgroundColor(android.graphics.Color.WHITE)
        stepProgress.findViewById<View>(R.id.step5).setBackgroundColor(android.graphics.Color.WHITE)

        val options = listOf(
            ActivityOption(
                apiValue = "SEDENTARY",
                title = "Mostly Sedentary",
                description = "Most of the day sitting — office work, studying, desk job."
            ),
            ActivityOption(
                apiValue = "LIGHT",
                title = "Lightly Active",
                description = "Some time standing or walking during the day."
            ),
            ActivityOption(
                apiValue = "MODERATE",
                title = "Moderately Active",
                description = "Frequent movement or physical tasks throughout the day."
            ),
            ActivityOption(
                apiValue = "ACTIVE",
                title = "Highly Active",
                description = "Your daily routine involves intense or physically demanding work."
            ),
            ActivityOption(
                apiValue = "VERY_ACTIVE",
                title = "Very Active",
                description = "Heavy physical workload or labor most of the day."
            )
        )

        optionAdapter = ActivityAdapter(options) { option ->
            selectedOption = option
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = optionAdapter
        recycler.itemAnimator = null

        backTop.setOnClickListener { findNavController().navigateUp() }

        nextButton.setOnClickListener {
            val option = selectedOption
            if (option == null) {
                Toast.makeText(requireContext(), "Please select your activity level", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setupViewModel.activityLevel = option.apiValue
            setupViewModel.submitProfile()
        }

        observeSetupState(nextButton)
    }

    private fun observeSetupState(nextButton: MaterialButton) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                setupViewModel.setupState.collect { result ->
                    when (result) {
                        is AuthResult.Loading -> {
                            nextButton.isEnabled = false
                            nextButton.text = "Saving..."
                        }
                        is AuthResult.Success -> {
                            nextButton.isEnabled = true
                            nextButton.text = "Next"
                            setupViewModel.resetState()
                            if (isAdded) {
                                findNavController().navigate(R.id.action_setupProfileFifth_to_homeFragment)
                            }
                        }
                        is AuthResult.Error -> {
                            nextButton.isEnabled = true
                            nextButton.text = "Next"
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            nextButton.isEnabled = true
                            nextButton.text = "Next"
                        }
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SetupProfileFifth()
    }
}

class ActivityAdapter(
    private val options: List<ActivityOption>,
    private val onSelect: (ActivityOption) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityVH>() {

    private var selectedIndex = -1

    override fun getItemCount() = options.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_option, parent, false)
        return ActivityVH(view)
    }

    override fun onBindViewHolder(holder: ActivityVH, position: Int) {
        holder.bind(options[position], selectedIndex == position) {
            val prev = selectedIndex
            selectedIndex = position
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(position)
            onSelect(options[position])
        }
    }

    class ActivityVH(val view: View) : RecyclerView.ViewHolder(view) {
        private val titleTv: TextView = view.findViewById(R.id.activity_title)
        private val descTv: TextView = view.findViewById(R.id.activity_desc)
        private val radioView: View = view.findViewById(R.id.activity_radio)
        private val card: View = view.findViewById(R.id.activity_card)

        fun bind(option: ActivityOption, isSelected: Boolean, onClick: () -> Unit) {
            titleTv.text = option.title
            descTv.text = option.description

            if (isSelected) {
                card.setBackgroundResource(R.drawable.goal_card_selected)
                titleTv.setTextColor(android.graphics.Color.WHITE)
                descTv.setTextColor(android.graphics.Color.parseColor("#BBBBBB"))
                radioView.setBackgroundResource(R.drawable.radio_selected)
            } else {
                card.setBackgroundResource(R.drawable.goal_card_default)
                titleTv.setTextColor(android.graphics.Color.WHITE)
                descTv.setTextColor(android.graphics.Color.parseColor("#888888"))
                radioView.setBackgroundResource(R.drawable.radio_default)
            }

            view.setOnClickListener { onClick() }
        }
    }
}