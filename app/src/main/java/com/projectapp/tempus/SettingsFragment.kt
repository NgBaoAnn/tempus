package com.projectapp.tempus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.projectapp.tempus.databinding.FragmentSettingsBinding
import android.util.Log // cho debug


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun logout() {
        // 1. XÓA TOKEN
        // 2. MỞ LOGIN ACTIVITY
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)

        // 3. ĐÓNG MAIN ACTIVITY
        requireActivity().finish()
    }

}
