package com.flashmob.platonus.ui.grades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.flashmob.platonus.R
import com.flashmob.platonus.data.model.Grade
import com.flashmob.platonus.data.model.UserRole
import com.flashmob.platonus.data.repository.GradesRepository
import com.flashmob.platonus.databinding.FragmentGradesBinding
import com.flashmob.platonus.util.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.recyclerview.widget.RecyclerView
import com.flashmob.platonus.databinding.DialogGradeHistoryBinding

class GradesFragment : Fragment() {

    private var _binding: FragmentGradesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GradesViewModel by viewModels {
        ViewModelFactory {
            GradesViewModel(GradesRepository())
        }
    }

    private lateinit var userId: String
    private lateinit var role: UserRole
    private var currentCourse: Int = 1
    private lateinit var adapter: GradesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = requireArguments().getString(ARG_USER_ID) ?: ""
        role = UserRole.valueOf(requireArguments().getString(ARG_USER_ROLE)!!)
        currentCourse = requireArguments().getInt(ARG_COURSE, 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGradesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = GradesAdapter { grade -> showHistoryDialog(grade) }
        binding.recyclerGrades.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerGrades.adapter = adapter

        binding.swipeRefresh.setColorSchemeColors(
            requireContext().getColor(R.color.platonus_red_primary)
        )
        binding.swipeRefresh.setOnRefreshListener { updateGrades() }

        initSpinners()
        updateGrades()
    }

    private fun initSpinners() {
        val courseTitles = (1..currentCourse).map {
            getString(R.string.grade_course, it)
        }
        binding.spinnerCourse.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            courseTitles
        )
        binding.spinnerCourse.setSelection(currentCourse - 1)

        val periodTitles = listOf("Период 1", "Период 2")
        binding.spinnerPeriod.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            periodTitles
        )

        val month = Calendar.getInstance().get(Calendar.MONTH)
        val currentPeriod = if (month < Calendar.FEBRUARY) 1 else 2
        binding.spinnerPeriod.setSelection(currentPeriod - 1)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) = updateGrades()
        }
        binding.spinnerCourse.onItemSelectedListener = listener
        binding.spinnerPeriod.onItemSelectedListener = listener
    }


    private fun updateGrades() {
        val selectedCourse = binding.spinnerCourse.selectedItemPosition + 1
        val selectedPeriod = binding.spinnerPeriod.selectedItemPosition + 1
        loadGrades(selectedCourse, selectedPeriod)
    }

    private fun loadGrades(course: Int, period: Int) {
        val now = Calendar.getInstance().get(Calendar.YEAR)
        val year = now - (currentCourse - course)

        viewLifecycleOwner.lifecycleScope.launch {
            binding.swipeRefresh.isRefreshing = true
            when (val rs = viewModel.fetchGrades(userId, year, period, course)) {
                is GradesUiState.Success -> adapter.submitList(rs.grades)
                is GradesUiState.Error   -> { adapter.submitList(emptyList()); snack(rs.message) }
            }
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun showHistoryDialog(finalGrade: Grade) {
        val period = binding.spinnerPeriod.selectedItemPosition + 1
        val course = binding.spinnerCourse.selectedItemPosition + 1
        val year = Calendar.getInstance().get(Calendar.YEAR) - (currentCourse - course)

        viewLifecycleOwner.lifecycleScope.launch {
            when (val rs = viewModel.fetchHistory(userId, finalGrade.subjectId, year, period)) {
                is GradesUiState.Success -> {
                    val dialogBinding =
                        DialogGradeHistoryBinding.inflate(layoutInflater)

                    dialogBinding.textGradeHistoryTitle.text = finalGrade.subjectName
                    dialogBinding.textGradeHistoryInfo.text =
                        "Преподаватель: ${finalGrade.teacherName}\nИтоговая оценка: ${finalGrade.score}"

                    val histAdapter = GradeHistoryAdapter()
                    dialogBinding.recyclerGradeHistory.apply {
                        layoutManager =
                            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                        adapter = histAdapter
                    }
                    histAdapter.submitList(rs.grades)

                    com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setView(dialogBinding.root)
                        .setPositiveButton("OK", null)
                        .show()
                }

                is GradesUiState.Error -> snack(rs.message)
            }
        }
    }

    private fun snack(msg: String) = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_USER_ID = "arg_user_id"
        private const val ARG_USER_ROLE = "arg_user_role"
        private const val ARG_COURSE = "arg_course"

        fun newInstance(userId: String, role: UserRole, course: Int?) =
            GradesFragment().apply {
                arguments = bundleOf(
                    ARG_USER_ID to userId,
                    ARG_USER_ROLE to role.name,
                    ARG_COURSE to (course ?: 1)
                )
            }
    }
}