package com.bartman79.elisa.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bartman79.elisa.R
import com.bartman79.elisa.databinding.FragmentProfileBinding
import com.bartman79.elisa.utils.PersonalityManager
import com.bartman79.elisa.utils.PersonalityType
import com.bartman79.elisa.utils.UserDataManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var personalityManager: PersonalityManager
    private lateinit var userDataManager: UserDataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        personalityManager = PersonalityManager(requireContext())
        userDataManager = UserDataManager(requireContext())

        // Загружаем имя и день рождения
        lifecycleScope.launch {
            userDataManager.userName.collect { name ->
                binding.etName.setText(name)
            }
        }
        lifecycleScope.launch {
            userDataManager.userBirthday.collect { birthday ->
                binding.etBirthday.setText(birthday)
            }
        }

        // Загружаем личность
        lifecycleScope.launch {
            personalityManager.currentPersonality.collect { type ->
                when (type) {
                    PersonalityType.FRIEND -> binding.radioFriend.isChecked = true
                    PersonalityType.SISTER -> binding.radioSister.isChecked = true
                    PersonalityType.COACH -> binding.radioCoach.isChecked = true
                }
            }
        }

        // Загружаем состояние уведомлений
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        binding.switchNotifications?.isChecked = prefs.getBoolean("notifications_enabled", true)

        // Обработка изменения переключателя уведомлений
        binding.switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        // Настройка Spinner для выбора языка
        val languages = arrayOf("Русский", "English", "Deutsch", "Français", "中文")
        val languageCodes = arrayOf("ru", "en", "de", "fr", "zh")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage?.adapter = adapter

        val sharedPrefLang = requireContext().getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val currentLang = sharedPrefLang.getString("app_lang", "ru") ?: "ru"
        val currentIndex = languageCodes.indexOf(currentLang).takeIf { it >= 0 } ?: 0
        binding.spinnerLanguage?.setSelection(currentIndex)

        binding.spinnerLanguage?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLangCode = languageCodes[position]
                if (selectedLangCode != currentLang) {
                    sharedPrefLang.edit().putString("app_lang", selectedLangCode).apply()
                    requireActivity().recreate()
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val birthday = binding.etBirthday.text.toString().trim()

            val selected = when (binding.radioGroupPersonality.checkedRadioButtonId) {
                R.id.radioFriend -> PersonalityType.FRIEND
                R.id.radioSister -> PersonalityType.SISTER
                R.id.radioCoach -> PersonalityType.COACH
                else -> PersonalityType.FRIEND
            }

            lifecycleScope.launch {
                personalityManager.setPersonality(selected)
                userDataManager.saveUserData(name, birthday)
                prefs.edit().putBoolean("notifications_enabled", binding.switchNotifications?.isChecked ?: true).apply()
                Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}