package com.bartman79.elisa.ui.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bartman79.elisa.R
import com.bartman79.elisa.data.local.database.AppDatabase
import com.bartman79.elisa.data.repository.DiaryRepository
import com.bartman79.elisa.databinding.FragmentDiaryDetailBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class DiaryDetailFragment : Fragment() {

    private var _binding: FragmentDiaryDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DiaryDetailViewModel
    private lateinit var photoAdapter: PhotoDetailAdapter
    private lateinit var repository: DiaryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val entryId = arguments?.getLong("entryId") ?: -1L
        Log.d("DiaryDetail", "Received entryId = $entryId")
        if (entryId == -1L) {
            Toast.makeText(requireContext(), "Ошибка: запись не найдена", Toast.LENGTH_SHORT).show()
            requireView().findNavController().popBackStack()
            return
        }

        val database = AppDatabase.getInstance(requireContext())
        repository = DiaryRepository(database.diaryEntryDao(), database.photoDao())

        val factory = DiaryDetailViewModel.Factory(repository, entryId)
        viewModel = ViewModelProvider(this, factory)[DiaryDetailViewModel::class.java]

        photoAdapter = PhotoDetailAdapter()
        binding.rvPhotos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPhotos.adapter = photoAdapter

        var isFirst = true
        lifecycleScope.launch {
            viewModel.entry.collect { entry ->
                Log.d("DiaryDetail", "collect called, entry = $entry, isFirst = $isFirst")
                if (isFirst) {
                    isFirst = false
                    if (entry == null) return@collect
                }
                if (entry != null) {
                    binding.tvDate.text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        .format(Date(entry.date))
                    binding.tvContent.text = entry.content

                    val photos = repository.getPhotosForEntry(entry.id).first()
                    Log.d("DiaryDetail", "Photos count: ${photos.size}")
                    photoAdapter.submitList(photos)
                    binding.rvPhotos.visibility = if (photos.isEmpty()) View.GONE else View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "Запись не найдена", Toast.LENGTH_SHORT).show()
                    requireView().findNavController().popBackStack()
                }
            }
        }

        binding.btnEdit.setOnClickListener {
            val bundle = Bundle().apply { putLong("entryId", entryId) }
            requireView().findNavController().navigate(R.id.diaryEditFragment, bundle)
        }

        binding.btnDelete.setOnClickListener {
            lifecycleScope.launch {
                viewModel.deleteEntry()
                Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
                requireView().findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}