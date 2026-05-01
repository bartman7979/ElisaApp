package com.bartman79.elisa.ui.counter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bartman79.elisa.R
import com.bartman79.elisa.data.local.database.AppDatabase
import com.bartman79.elisa.data.repository.MoodRepository
import com.bartman79.elisa.databinding.FragmentCounterBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.bartman79.elisa.utils.MoodColorCalculator
import android.view.animation.ScaleAnimation
import android.view.animation.Animation

//import com.google.firebase.analytics.ktx.analytics
//import com.google.firebase.ktx.Firebase

class CounterFragment : Fragment() {

    private var _binding: FragmentCounterBinding? = null
    private val binding get() = _binding!!

    private lateinit var moodRepository: MoodRepository
    private lateinit var viewModel: CounterViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCounterBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun animateClick(view: View) {
        val anim = ScaleAnimation(
            1.0f, 1.1f,
            1.0f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.duration = 100
        anim.repeatCount = 1
        anim.repeatMode = Animation.REVERSE
        view.startAnimation(anim)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация репозитория и ViewModel
        val database = AppDatabase.getInstance(requireContext())
        moodRepository = MoodRepository(database.moodEventDao())
        val factory = CounterViewModel.Factory(moodRepository)
        viewModel = ViewModelProvider(this, factory)[CounterViewModel::class.java]

        // Подписка на изменения баланса для обновления лампы
        lifecycleScope.launch {
            moodRepository.getTodayBalanceFlow().collectLatest { balance ->
                updateLampColor(balance)
            }
        }

        binding.btnPositive.setOnClickListener {
            //analytics.logEvent("mood_positive_tap") {
            //    param("value", moodValue.toString())
            //}
            animateClick(binding.btnPositive)
            viewModel.addPositiveEvent()
            Toast.makeText(requireContext(), "Позитив добавлен", Toast.LENGTH_SHORT).show()
        }

        binding.btnNegative.setOnClickListener {
            animateClick(binding.btnNegative)
            viewModel.addNegativeEvent()
            Toast.makeText(requireContext(), "Негатив добавлен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLampColor(balance: Int) {
        if (_binding == null) return
        val color = MoodColorCalculator.getColor(balance)
        binding.ivCounterLamp.setColorFilter(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}