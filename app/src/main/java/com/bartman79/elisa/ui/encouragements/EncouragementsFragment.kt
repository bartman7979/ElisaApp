package com.bartman79.elisa.ui.encouragements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bartman79.elisa.R
import com.bartman79.elisa.databinding.FragmentEncouragementsBinding
import com.bartman79.elisa.utils.PersonalityManager
import com.bartman79.elisa.utils.PersonalityType
import kotlinx.coroutines.launch
import kotlin.random.Random

class EncouragementsFragment : Fragment() {

    private var _binding: FragmentEncouragementsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EncouragementsViewModel
    private lateinit var personalityManager: PersonalityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEncouragementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        personalityManager = PersonalityManager(requireContext())

        // Получаем массивы фраз по умолчанию (пока нет данных)
        val defaultPhrases = resources.getStringArray(R.array.encouragements_friend)
        val factory = EncouragementsViewModel.Factory(defaultPhrases)
        viewModel = ViewModelProvider(this, factory)[EncouragementsViewModel::class.java]

        binding.tvEncouragement.text = viewModel.getCurrentPhrase()

        binding.btnNext.setOnClickListener {
            viewModel.nextPhrase()
            binding.tvEncouragement.text = viewModel.getCurrentPhrase()
        }

        // Подписываемся на изменения личности и обновляем массив во ViewModel
        lifecycleScope.launch {
            personalityManager.currentPersonality.collect { type ->
                val phrases = when (type) {
                    PersonalityType.FRIEND -> resources.getStringArray(R.array.encouragements_friend)
                    PersonalityType.SISTER -> resources.getStringArray(R.array.encouragements_sister)
                    PersonalityType.COACH -> resources.getStringArray(R.array.encouragements_coach)
                }
                viewModel.updatePhrases(phrases)
                // Показываем новую фразу (можно случайную, можно сохранить текущую, но лучше обновить)
                binding.tvEncouragement.text = viewModel.getCurrentPhrase()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}