package com.bartman79.elisa.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartman79.elisa.data.local.database.AppDatabase
import com.bartman79.elisa.data.repository.CycleRepository
import kotlinx.coroutines.launch

class MonthPageFragment : Fragment() {

    private lateinit var viewModel: CalendarViewModel
    private lateinit var calendarAdapter: CalendarAdapter
    private var monthOffset: Int = 0

    companion object {
        fun newInstance(offset: Int): MonthPageFragment {
            val fragment = MonthPageFragment()
            val args = Bundle()
            args.putInt("offset", offset)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        monthOffset = arguments?.getInt("offset") ?: 0

        // Инициализация ViewModel
        val dao = AppDatabase.getInstance(requireContext()).cycleDayDao()
        val repository = CycleRepository(dao)
        val factory = CalendarViewModel.Factory(requireContext(), repository)
        viewModel = ViewModelProvider(this, factory)[CalendarViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        calendarAdapter = CalendarAdapter { day ->
            viewModel.toggleDay(day.date, monthOffset)
        }

        val recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = GridLayoutManager(requireContext(), 7)
            isNestedScrollingEnabled = false
            adapter = calendarAdapter
        }
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.daysState.collect { allDays ->
                val daysForThisMonth = allDays[monthOffset]
                if (daysForThisMonth != null) {
                    calendarAdapter.submitList(daysForThisMonth)
                }
            }
        }

        viewModel.loadDaysForMonth(monthOffset)
    }
}