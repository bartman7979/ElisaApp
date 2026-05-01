package com.bartman79.elisa.ui.diary

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bartman79.elisa.R
import com.bartman79.elisa.data.local.database.AppDatabase
import com.bartman79.elisa.data.repository.DiaryRepository
import com.bartman79.elisa.databinding.FragmentDiaryEditBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DiaryEditFragment : Fragment() {

    private var _binding: FragmentDiaryEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DiaryEditViewModel
    private var entryId: Long = -1L
    private lateinit var photoAdapter: PhotoPreviewAdapter
    private val photoPaths = mutableListOf<String>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data
            imageUri?.let { uri ->
                saveImageToInternalStorage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        entryId = arguments?.getLong("entryId", -1L) ?: -1L
        Log.d("DiaryEdit", "Started with entryId: $entryId")

        val database = AppDatabase.getInstance(requireContext())
        val repository = DiaryRepository(database.diaryEntryDao(), database.photoDao())
        val factory = DiaryEditViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, factory)[DiaryEditViewModel::class.java]

        photoAdapter = PhotoPreviewAdapter(photoPaths) { position ->
            photoPaths.removeAt(position)
            photoAdapter.notifyDataSetChanged()
            if (photoPaths.isEmpty()) binding.rvPhotos.visibility = View.GONE
        }
        binding.rvPhotos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPhotos.adapter = photoAdapter

        if (entryId > 0) {
            lifecycleScope.launch {
                val entry = repository.getEntryById(entryId)
                binding.etContent.setText(entry?.content ?: "")

                val existingPhotos = repository.getPhotosForEntry(entryId).first()
                val paths = existingPhotos.map { it.filePath }
                photoPaths.clear()
                photoPaths.addAll(paths)
                photoAdapter.notifyDataSetChanged()
                if (paths.isNotEmpty()) binding.rvPhotos.visibility = View.VISIBLE
            }
        }

        binding.btnSave.setOnClickListener {
            val content = binding.etContent.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.setPhotoPaths(photoPaths.toList())
                if (entryId <= 0) {
                    lifecycleScope.launch {
                        val newId = viewModel.saveEntry(content)
                        Toast.makeText(requireContext(), "Сохранено ${photoPaths.size} фото", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                } else {
                    lifecycleScope.launch {
                        viewModel.updateEntry(entryId, content)
                        Toast.makeText(requireContext(), "Запись обновлена", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Введите текст", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val imagesDir = File(requireContext().filesDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val filename = "img_${System.currentTimeMillis()}.jpg"
            val outFile = File(imagesDir, filename)
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            }

            photoPaths.add(outFile.absolutePath)
            photoAdapter.notifyDataSetChanged()
            if (binding.rvPhotos.visibility != View.VISIBLE) {
                binding.rvPhotos.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e("DiaryEdit", "Ошибка сохранения фото", e)
            Toast.makeText(requireContext(), "Не удалось сохранить фото", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class PhotoPreviewAdapter(
        private val paths: List<String>,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<PhotoPreviewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo_preview, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(paths[position])
            holder.itemView.setOnClickListener {
                onDelete(position)
            }
        }

        override fun getItemCount() = paths.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivThumb: ImageView = itemView.findViewById(R.id.ivThumb)

            fun bind(path: String) {
                ivThumb.load(File(path)) {
                    crossfade(true)
                    placeholder(R.drawable.ic_photo_placeholder)
                    error(R.drawable.ic_photo_error)
                }
            }
        }
    }
}