package com.bartman79.elisa.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bartman79.elisa.R
import com.bartman79.elisa.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvVersion.text = getString(R.string.about_version)
        binding.tvDescription.text = android.text.Html.fromHtml(
            getString(R.string.about_description),
            android.text.Html.FROM_HTML_MODE_LEGACY
        )
        binding.tvCopyright.text = getString(R.string.about_copyright)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}