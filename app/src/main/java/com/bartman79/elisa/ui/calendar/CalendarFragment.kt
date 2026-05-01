package com.bartman79.elisa.ui.calendar

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bartman79.elisa.data.local.database.AppDatabase
import com.bartman79.elisa.data.repository.CycleRepository
import com.bartman79.elisa.databinding.FragmentCalendarBinding
import kotlinx.coroutines.launch

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CalendarViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getInstance(requireContext())
        val repository = CycleRepository(database.cycleDayDao())
        val factory = CalendarViewModel.Factory(requireContext(), repository)
        viewModel = ViewModelProvider(this, factory)[CalendarViewModel::class.java]

        // Настройка бесконечного свайпа
        val startPos = 500
        binding.calendarPager.adapter = ScreenSlidePagerAdapter(this)
        binding.calendarPager.setCurrentItem(startPos, false)

        // Анимация затухания при свайпе
        binding.calendarPager.setPageTransformer { page, position ->
            page.alpha = 0.3f + (1 - Math.abs(position)) * 0.7f
        }

        binding.calendarPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.updateMonthTitle(position - startPos)
            }
        })

        // Подписки на заголовок и прогноз
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthTitle.collect { binding.tvMonthYear.text = it }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.prediction.collect { binding.tvPrediction.text = it }
        }

        // Кнопки
        binding.ivPrevMonth.setOnClickListener { binding.calendarPager.currentItem -= 1 }
        binding.ivNextMonth.setOnClickListener { binding.calendarPager.currentItem += 1 }
    }

    // Вложенный адаптер для страниц ViewPager
    private inner class ScreenSlidePagerAdapter(f: Fragment) : FragmentStateAdapter(f) {
        override fun getItemCount(): Int = 1000
        override fun createFragment(position: Int): Fragment = MonthPageFragment.newInstance(position - 500)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}