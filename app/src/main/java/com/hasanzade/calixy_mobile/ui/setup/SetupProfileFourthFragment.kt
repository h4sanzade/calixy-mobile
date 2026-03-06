package com.hasanzade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hasanzade.calixy_mobile.AppModule
import com.hasanzade.calixy_mobile.R
import com.hasanzade.calixy_mobile.ui.setup.SetupProfileViewModel
import com.hasanzade.calixy_mobile.ui.setup.SetupProfileViewModelFactory

class SetupProfileFourth : Fragment() {

    private val selectedGoals = mutableSetOf<Goal>()
    private lateinit var adapter: GoalAdapter

    private val setupViewModel: SetupProfileViewModel by activityViewModels {
        SetupProfileViewModelFactory(AppModule.provideAuthRepository(requireContext()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup_profile_fourth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val greetingTv = view.findViewById<TextView>(R.id.greeting_tv)
        val goalsRecycler = view.findViewById<RecyclerView>(R.id.goals_recycler)
        val backButton = view.findViewById<View>(R.id.back_button)
        val nextButton = view.findViewById<View>(R.id.next_button)

        val stepProgress = view.findViewById<View>(R.id.view_step_progress_id)
        stepProgress.findViewById<View>(R.id.step1).setBackgroundColor(android.graphics.Color.WHITE)
        stepProgress.findViewById<View>(R.id.step2).setBackgroundColor(android.graphics.Color.WHITE)
        stepProgress.findViewById<View>(R.id.step3).setBackgroundColor(android.graphics.Color.WHITE)
        stepProgress.findViewById<View>(R.id.step4).setBackgroundColor(android.graphics.Color.WHITE)

        val firstName = setupViewModel.firstName
        greetingTv.text = if (firstName.isNotBlank())
            "Hey, $firstName 👋 Let's start\nwith your goals."
        else
            "Hey 👋 Let's start\nwith your goals."

        val goals = listOf(
            Goal.LOSE_WEIGHT to "Lose Weight",
            Goal.MAINTAIN_WEIGHT to "Maintain Weight",
            Goal.GAIN_WEIGHT to "Gain Weight",
            Goal.GAIN_MUSCLE to "Gain Muscle",
            Goal.MODIFY_DIET to "Modify My Diet",
            Goal.PLAN_MEALS to "Plan Meals",
            Goal.MANAGE_STRESS to "Manage Stress",
            Goal.STAY_ACTIVE to "Stay Active"
        )

        adapter = GoalAdapter(goals) { goal, isChecked ->
            if (isChecked) {
                if (selectedGoals.size >= 3) {
                    adapter.revertCheck(goal)
                    Toast.makeText(requireContext(), "You can select up to 3 goals", Toast.LENGTH_SHORT).show()
                } else {
                    selectedGoals.add(goal)
                }
            } else {
                selectedGoals.remove(goal)
            }
        }

        goalsRecycler.layoutManager = LinearLayoutManager(requireContext())
        goalsRecycler.adapter = adapter

        backButton.setOnClickListener { findNavController().navigateUp() }

        nextButton.setOnClickListener {
            if (selectedGoals.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Save goals to shared ViewModel, navigate to activity level screen
            setupViewModel.goals = selectedGoals.map { it.name }
            findNavController().navigate(R.id.action_setupProfileFourth_to_homeFragment)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SetupProfileFourth()
    }
}

class GoalAdapter(
    private val goals: List<Pair<Goal, String>>,
    private val onToggle: (Goal, Boolean) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalVH>() {

    private val checkedStates = mutableMapOf<Goal, Boolean>()

    fun revertCheck(goal: Goal) {
        checkedStates[goal] = false
        val index = goals.indexOfFirst { it.first == goal }
        if (index != -1) notifyItemChanged(index)
    }

    override fun getItemCount() = goals.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalVH(view)
    }

    override fun onBindViewHolder(holder: GoalVH, position: Int) {
        val (goal, label) = goals[position]
        val isChecked = checkedStates[goal] ?: false
        holder.bind(label, isChecked) { checked ->
            checkedStates[goal] = checked
            onToggle(goal, checked)
        }
    }

    class GoalVH(val view: View) : RecyclerView.ViewHolder(view) {
        private val labelTv: TextView = view.findViewById(R.id.goal_label)
        private val checkbox: android.widget.CheckBox = view.findViewById(R.id.goal_checkbox)
        private val card: View = view.findViewById(R.id.goal_card)

        fun bind(label: String, isChecked: Boolean, onToggle: (Boolean) -> Unit) {
            labelTv.text = label
            checkbox.isChecked = isChecked
            updateStyle(isChecked)
            view.setOnClickListener {
                val newState = !checkbox.isChecked
                checkbox.isChecked = newState
                updateStyle(newState)
                onToggle(newState)
            }
            checkbox.setOnCheckedChangeListener(null)
        }

        private fun updateStyle(checked: Boolean) {
            if (checked) {
                card.setBackgroundResource(R.drawable.goal_card_selected)
                labelTv.setTextColor(android.graphics.Color.WHITE)
            } else {
                card.setBackgroundResource(R.drawable.goal_card_default)
                labelTv.setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            }
        }
    }
}