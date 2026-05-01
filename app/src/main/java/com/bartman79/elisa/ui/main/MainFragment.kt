package com.bartman79.elisa.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bartman79.elisa.R
import com.bartman79.elisa.data.local.database.AppDatabase
import com.bartman79.elisa.data.repository.MoodRepository
import com.bartman79.elisa.databinding.FragmentMainBinding
import com.bartman79.elisa.utils.ImageManager
import com.bartman79.elisa.utils.MoodColorCalculator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.bartman79.elisa.MoodWidgetProvider

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var imageManager: ImageManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация репозитория и ViewModel
        val database = AppDatabase.getInstance(requireContext())
        val moodRepository = MoodRepository(database.moodEventDao())
        val factory = MainViewModel.Factory(moodRepository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        // Инициализация менеджера изображений
        imageManager = ImageManager(requireContext())

        // Навигация
        binding.ivDiary.setOnClickListener {
            findNavController().navigate(R.id.diaryListFragment)
        }
        binding.ivCard.setOnClickListener {
            findNavController().navigate(R.id.encouragementsFragment)
        }
        binding.ivCalendar.setOnClickListener {
            findNavController().navigate(R.id.calendarFragment)
        }
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
        binding.ivLamp.setOnClickListener {
            findNavController().navigate(R.id.counterFragment)
        }
        binding.ivChat.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }
        binding.ivAbout.setOnClickListener {
            findNavController().navigate(R.id.aboutFragment)
        }

        // Наблюдение за сменой фона (подписка остаётся здесь)
        lifecycleScope.launch {
            imageManager.currentImage.collect { imageRes ->
                binding.ivBackground.setImageResource(imageRes)
            }
        }

        // Наблюдение за балансом для цвета лампы
        lifecycleScope.launch {
            viewModel.balance.collectLatest { balance ->
                updateLampColor(balance)
                saveBalanceForWidget(balance)
            }
        }
    }

    private fun saveBalanceForWidget(balance: Int) {
        try {
            val prefs = requireContext().getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("balance", balance).apply()

            val appWidgetManager = AppWidgetManager.getInstance(requireContext())
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(requireContext(), MoodWidgetProvider::class.java)
            )
            for (id in appWidgetIds) {
                MoodWidgetProvider.updateAppWidget(requireContext(), appWidgetManager, id)
            }
        } catch (e: Exception) {
            Log.e("MainFragment", "Error updating widget", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем картинку при каждом возвращении на экран
        lifecycleScope.launch {
            imageManager.updateImage()
        }
    }

    private fun updateLampColor(balance: Int) {
        if (_binding == null) return
        val color = MoodColorCalculator.getColor(balance)
        binding.ivLamp.setColorFilter(color)
        binding.tvBalance.text = if (balance > 0) "+$balance" else balance.toString()


        // Анимация лёгкого увеличения
        val scaleAnim = ScaleAnimation(
            1.0f, 1.05f,
            1.0f, 1.05f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnim.duration = 200
        scaleAnim.repeatMode = Animation.REVERSE
        scaleAnim.repeatCount = 1
        binding.ivLamp.startAnimation(scaleAnim)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //override fun onResume() {
    //    super.onResume()
    //    analytics.setCurrentScreen(requireActivity(), "MainScreen", null)
    //}
}