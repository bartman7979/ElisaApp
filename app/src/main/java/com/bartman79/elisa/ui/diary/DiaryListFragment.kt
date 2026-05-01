package com.bartman79.elisa.ui.diary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bartman79.elisa.R
import com.bartman79.elisa.data.local.database.AppDatabase
import com.bartman79.elisa.data.repository.DiaryRepository
import com.bartman79.elisa.databinding.FragmentDiaryListBinding
import kotlinx.coroutines.launch

class DiaryListFragment : Fragment() {

    private var _binding: FragmentDiaryListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DiaryListViewModel
    private lateinit var adapter: DiaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация RecyclerView
        adapter = DiaryAdapter { entry ->
            Log.d("DiaryList", "Item clicked, id=${entry.id}, navigating to detail")
            val bundle = Bundle().apply { putLong("entryId", entry.id) }
            requireView().findNavController().navigate(R.id.diaryDetailFragment, bundle)
        }
        binding.rvEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntries.adapter = adapter

        // Инициализация репозитория и ViewModel
        val database = AppDatabase.getInstance(requireContext())
        val repository = DiaryRepository(database.diaryEntryDao(), database.photoDao())
        val factory = DiaryListViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, factory)[DiaryListViewModel::class.java]

        // Наблюдение за списком записей
        lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                Log.d("DiaryList", "entries updated: size = ${entries.size}")
                for (entry in entries) {
                    Log.d("DiaryList", "entry: id=${entry.id}, content=${entry.content.take(20)}")
                }
                adapter.submitList(entries)
            }
        }

        // Кнопка добавления новой записи
        binding.fabAdd.setOnClickListener {
            // Для новой записи передаём entryId = -1 (или не передаём, но в EditFragment по умолчанию -1)
            // Можно просто перейти без аргументов
            requireView().findNavController().navigate(R.id.diaryEditFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}