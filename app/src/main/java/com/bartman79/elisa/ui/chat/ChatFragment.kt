package com.bartman79.elisa.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bartman79.elisa.R
import com.bartman79.elisa.data.local.database.AppDatabase
import com.bartman79.elisa.data.repository.CycleRepository
import com.bartman79.elisa.data.repository.MoodRepository
import com.bartman79.elisa.databinding.FragmentChatBinding
import com.bartman79.elisa.utils.PersonalityManager
import com.bartman79.elisa.utils.PersonalityType
import com.bartman79.elisa.utils.UserDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import kotlinx.coroutines.flow.first
import org.json.JSONArray

class ChatFragment : Fragment() {

    private lateinit var userDataManager: UserDataManager
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var personalityManager: PersonalityManager
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private lateinit var moodRepository: MoodRepository
    private lateinit var cycleRepository: CycleRepository

    private val WORKER_URL = "https://eliza-ai-worker.sergeylando87.workers.dev"

    // Сохраняем ссылку на слушатель клавиатуры, чтобы удалить его в onDestroyView
    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация менеджеров и репозиториев
        personalityManager = PersonalityManager(requireContext())
        userDataManager = UserDataManager(requireContext())
        val database = AppDatabase.getInstance(requireContext())
        moodRepository = MoodRepository(database.moodEventDao())
        cycleRepository = CycleRepository(database.cycleDayDao())
        adapter = ChatAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = adapter
        addMessage(getString(R.string.chat_welcome), false)

        // Слушатель клавиатуры
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            // Фрагмент мог быть уничтожен – проверяем binding
            if (_binding == null) return@OnGlobalLayoutListener
            val rect = android.graphics.Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) { // клавиатура открыта
                binding.bottomPanel.setPadding(0, 0, 0, keypadHeight)
                // Адаптер уже инициализирован
                if (::adapter.isInitialized) {
                    binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
                }
            } else {
                binding.bottomPanel.setPadding(0, 0, 0, 0)
            }
        }
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        // Кнопка "Назад" в шапке
        binding.ivBack.setOnClickListener {
            requireView().findNavController().popBackStack()
        }

        // Системная кнопка "Назад"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireView().findNavController().popBackStack()
            }
        })

        // Отступ под системную навигацию
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            binding.bottomPanel.setPadding(0, 0, 0, navigationBarHeight)
            insets
        }

        // Отправка сообщения
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                binding.etMessage.text.clear()
            }
        }
    }

    private fun sendMessage(userMessage: String) {
        addMessage(userMessage, true)

        lifecycleScope.launch {
            val contextData = buildContext()
            val history = messages.takeLast(10).map { msg ->
                mapOf(
                    "role" to if (msg.isUser) "user" else "assistant",
                    "content" to msg.text
                )
            }
            val historyArray = JSONArray()
            history.forEach { item ->
                val obj = JSONObject()
                obj.put("role", item["role"])
                obj.put("content", item["content"])
                historyArray.put(obj)
            }
            val json = JSONObject().apply {
                put("name", contextData.name)
                put("mood", contextData.mood)
                put("cyclePhase", contextData.cyclePhase)
                put("personality", contextData.personality)
                put("message", userMessage)
                put("history", historyArray)
                put("isBirthday", contextData.isBirthday)
            }

            val request = Request.Builder()
                .url(WORKER_URL)
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            withContext(Dispatchers.IO) {
                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d("ChatFragment", "Response: $responseBody")
                        val reply = JSONObject(responseBody).optString("reply", "...")
                        withContext(Dispatchers.Main) {
                            addMessage(reply, false)
                        }
                    } else {
                        val errorBody = response.body?.string()
                        Log.e("ChatFragment", "HTTP error ${response.code}: $errorBody")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Ошибка сервера (${response.code})", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: IOException) {
                    Log.e("ChatFragment", "Network error", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Сетевая ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun buildContext(): ContextData {
        val name = userDataManager.userName.first().ifEmpty { "Дорогая" }
        val mood = getTodayMood()
        val cyclePhase = getCyclePhase()
        val personality = personalityManager.currentPersonality.first()
        val personalityStr = when (personality) {
            PersonalityType.FRIEND -> "friend"
            PersonalityType.SISTER -> "sister"
            PersonalityType.COACH -> "coach"
        }
        val isBirthday = userDataManager.isBirthdayToday()
        return ContextData(name, mood, cyclePhase, personalityStr, isBirthday)
    }

    private suspend fun getTodayMood(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        return moodRepository.getTodayBalance(startOfDay, endOfDay)
    }

    private suspend fun getCyclePhase(): String {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val markedDays = cycleRepository.getDaysForMonth(startOfMonth, endOfMonth)
        val todayMarked = markedDays.any { it.date == today && it.type == 1 }

        val predictions = cycleRepository.calculatePredictions(startOfMonth, endOfMonth)
        val isOvulation = predictions.ovulation.contains(today)
        val isFertile = predictions.fertile.contains(today)

        return when {
            todayMarked -> "menstruation"
            isOvulation -> "ovulation"
            isFertile -> "fertile"
            else -> "normal"
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(Message(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.smoothScrollToPosition(messages.size - 1)
    }

    override fun onDestroyView() {
        // Удаляем слушатель клавиатуры, чтобы избежать вызовов после уничтожения
        if (_binding != null) {
            binding.root.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
        super.onDestroyView()
        _binding = null
    }

    data class ContextData(
        val name: String,
        val mood: Int,
        val cyclePhase: String,
        val personality: String,
        val isBirthday: Boolean
    )
}