package com.hasanzade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.hasanzade.calixy_mobile.AppModule
import com.hasanzade.calixy_mobile.R
import com.hasanzade.calixy_mobile.ui.setup.SetupProfileViewModel
import com.hasanzade.calixy_mobile.ui.setup.SetupProfileViewModelFactory

class SetupProfileSecond : Fragment() {

    private var selectedHeight = 164
    private val minHeight = 100
    private val maxHeight = 220

    private val setupViewModel: SetupProfileViewModel by activityViewModels {
        SetupProfileViewModelFactory(AppModule.provideAuthRepository(requireContext()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup_profile_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val heightLabel = view.findViewById<TextView>(R.id.height_label)
        val rulerRecycler = view.findViewById<RecyclerView>(R.id.ruler_recycler)
        val backButton = view.findViewById<View>(R.id.back_button)
        val nextButton = view.findViewById<View>(R.id.next_button)

        heightLabel.text = "${selectedHeight}cm"

        val stepProgress = view.findViewById<View>(R.id.view_step_progress_id)
        stepProgress.findViewById<View>(R.id.step1).setBackgroundColor(android.graphics.Color.WHITE)
        stepProgress.findViewById<View>(R.id.step2).setBackgroundColor(android.graphics.Color.WHITE)

        val adapter = RulerAdapter(minHeight, maxHeight)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rulerRecycler.layoutManager = layoutManager
        rulerRecycler.adapter = adapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rulerRecycler)

        rulerRecycler.post {
            val itemWidthPx = dpToPx(28)
            val recyclerCenter = rulerRecycler.width / 2
            val targetPosition = (selectedHeight - minHeight) + RulerAdapter.PADDING_COUNT
            val offset = recyclerCenter - itemWidthPx / 2
            layoutManager.scrollToPositionWithOffset(targetPosition, offset)
        }

        rulerRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapView = snapHelper.findSnapView(layoutManager) ?: return
                    val position = layoutManager.getPosition(snapView)
                    val dataIndex = position - RulerAdapter.PADDING_COUNT
                    if (dataIndex < 0 || dataIndex > (maxHeight - minHeight)) return
                    selectedHeight = minHeight + dataIndex
                    heightLabel.text = "${selectedHeight}cm"
                }
            }
        })

        backButton.setOnClickListener { findNavController().navigateUp() }

        nextButton.setOnClickListener {
            setupViewModel.height = selectedHeight.toDouble()
            findNavController().navigate(R.id.action_setupProfileSecond_to_homeFragment)
        }
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    companion object {
        @JvmStatic
        fun newInstance() = SetupProfileSecond()
    }
}

class RulerAdapter(private val min: Int, private val max: Int) : RecyclerView.Adapter<RulerAdapter.TickVH>() {

    companion object { const val PADDING_COUNT = 8 }

    override fun getItemCount() = (max - min + 1) + PADDING_COUNT * 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TickVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ruler_tick, parent, false)
        return TickVH(view)
    }

    override fun onBindViewHolder(holder: TickVH, position: Int) {
        val dataIndex = position - PADDING_COUNT
        if (dataIndex < 0 || dataIndex > (max - min)) holder.bind(-1) else holder.bind(min + dataIndex)
    }

    class TickVH(val view: View) : RecyclerView.ViewHolder(view) {
        private val tickView: View = view.findViewById(R.id.tick_line)
        private val labelView: TextView = view.findViewById(R.id.tick_label)

        fun bind(value: Int) {
            if (value < 0) { tickView.visibility = View.INVISIBLE; labelView.visibility = View.INVISIBLE; return }
            tickView.visibility = View.VISIBLE
            val isMajor = value % 10 == 0
            val isMid = value % 5 == 0 && !isMajor
            val d = view.resources.displayMetrics.density
            when {
                isMajor -> { tickView.layoutParams.height = (52 * d).toInt(); tickView.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF")); labelView.visibility = View.VISIBLE; labelView.text = "$value"; labelView.setTextColor(android.graphics.Color.parseColor("#888888")); labelView.textSize = 11f }
                isMid -> { tickView.layoutParams.height = (32 * d).toInt(); tickView.setBackgroundColor(android.graphics.Color.parseColor("#666666")); labelView.visibility = View.INVISIBLE }
                else -> { tickView.layoutParams.height = (18 * d).toInt(); tickView.setBackgroundColor(android.graphics.Color.parseColor("#404040")); labelView.visibility = View.INVISIBLE }
            }
            tickView.requestLayout()
        }
    }
}