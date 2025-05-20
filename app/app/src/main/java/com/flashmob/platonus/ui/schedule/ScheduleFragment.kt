package com.flashmob.platonus.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.flashmob.platonus.R
import com.flashmob.platonus.data.model.UserRole
import com.flashmob.platonus.data.repository.ScheduleRepository
import com.flashmob.platonus.databinding.FragmentScheduleBinding
import com.flashmob.platonus.util.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScheduleViewModel by viewModels {
        ViewModelFactory { ScheduleViewModel(ScheduleRepository()) }
    }

    private lateinit var userId: String
    private lateinit var userRole: UserRole
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = requireArguments().getString(ARG_USER_ID) ?: ""
        userRole = UserRole.valueOf(requireArguments().getString(ARG_USER_ROLE)!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ScheduleAdapter()
        binding.recyclerSchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSchedule.adapter = adapter

        binding.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.platonus_red_primary, null))
        binding.swipeRefresh.setOnRefreshListener { loadSchedule() }

        loadSchedule()
    }

    private fun loadSchedule() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        viewLifecycleOwner.lifecycleScope.launch {
            binding.swipeRefresh.isRefreshing = true
            when (val result = viewModel.getSchedule(userId, userRole, year)) {
                is ScheduleUiState.Success -> adapter.submitList(result.items)
                is ScheduleUiState.Error -> Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
            }
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_USER_ID = "arg_user_id"
        private const val ARG_USER_ROLE = "arg_user_role"

        fun newInstance(userId: String, role: UserRole) = ScheduleFragment().apply {
            arguments = bundleOf(
                ARG_USER_ID to userId,
                ARG_USER_ROLE to role.name
            )
        }
    }
}