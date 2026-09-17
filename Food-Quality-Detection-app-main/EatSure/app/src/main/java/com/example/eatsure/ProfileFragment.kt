package com.example.eatsure

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.eatsure.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe user data
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserEmail.text = user.email ?: "No email"
            } else {
                // User not logged in, navigate to login
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }

        // Observe full name
        viewModel.fullName.observe(viewLifecycleOwner) { fullName ->
            binding.tvUserName.text = fullName ?: "Anonymous"
        }


        // Handle settings clicks
        binding.llEditProfile.setOnClickListener {
            // TODO: Navigate to Edit Profile Fragment or show dialog
            Snackbar.make(binding.root, "Edit Profile clicked", Snackbar.LENGTH_SHORT).show()
        }

        binding.llChangePassword.setOnClickListener {
            // TODO: Navigate to Change Password Fragment or show dialog
            Snackbar.make(binding.root, "Change Password clicked", Snackbar.LENGTH_SHORT).show()
        }

        binding.llLogout.setOnClickListener {
            viewModel.logout()
        }

        // Observe logout success
        viewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}