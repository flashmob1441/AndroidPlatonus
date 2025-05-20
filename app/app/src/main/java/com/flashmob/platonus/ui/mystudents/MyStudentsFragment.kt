package com.flashmob.platonus.ui.mystudents

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
import com.flashmob.platonus.data.model.Group
import com.flashmob.platonus.data.model.User
import com.flashmob.platonus.data.repository.GradesRepository
import com.flashmob.platonus.data.repository.ScheduleRepository
import com.flashmob.platonus.data.repository.TeacherRepository
import com.flashmob.platonus.databinding.FragmentMyStudentsBinding
import com.flashmob.platonus.util.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class MyStudentsFragment : Fragment(), StudentsAdapter.OnGradeClickListener {

    private var _binding: FragmentMyStudentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyStudentsViewModel by viewModels {
        ViewModelFactory { MyStudentsViewModel(TeacherRepository(), GradesRepository(), ScheduleRepository()) }
    }

    private lateinit var teacherId: String
    private lateinit var adapter: StudentsAdapter
    private lateinit var groups: List<Group>
    private lateinit var subjects: List<SubjectOption>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teacherId = requireArguments().getString(ARG_TEACHER_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = StudentsAdapter(this)
        binding.recyclerStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStudents.adapter = adapter

        binding.swipeRefresh.setColorSchemeColors(
            resources.getColor(R.color.platonus_red_primary, null)
        )
        binding.swipeRefresh.setOnRefreshListener { loadEverything() }

        loadEverything()
    }

    private fun loadEverything() = viewLifecycleOwner.lifecycleScope.launch {
        binding.swipeRefresh.isRefreshing = true
        val year = Calendar.getInstance().get(Calendar.YEAR)

        subjects = viewModel.fetchTeacherSubjects(teacherId, year)
        groups = viewModel.fetchGroups()
        if (groups.isEmpty()) {
            snack("Нет групп") ; binding.swipeRefresh.isRefreshing = false ; return@launch
        }
        val titles = groups.map { it.name }
        binding.spinnerSubject.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, titles)
        binding.spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(p0: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                loadStudents(groups[pos].id)
            }
        }
        binding.spinnerSubject.setSelection(0)
        binding.swipeRefresh.isRefreshing = false
    }

    private fun loadStudents(groupId: String) = viewLifecycleOwner.lifecycleScope.launch {
        when (val s = viewModel.fetchStudents(groupId)) {
            is StudentsUiState.Success -> adapter.submitList(s.students)
            is StudentsUiState.Error   -> snack(s.message)
        }
    }

    override fun onGradeClick(user: User) {
        if (subjects.isEmpty()) { snack("Нет предметов"); return }
        var idx = 0
        val input = layoutInflater.inflate(R.layout.dialog_edit_grade, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_grade_title, user.name))
            .setSingleChoiceItems(subjects.map { it.name }.toTypedArray(), 0) { _, i -> idx = i }
            .setView(input)
            .setPositiveButton("Ок") { _, _ ->
                val score = input.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editScore)
                    .text.toString().toIntOrNull()
                if (score == null) { snack("Неверное значение"); return@setPositiveButton }
                val sub = subjects[idx]
                submitGrade(user, sub.id, sub.name, score)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun submitGrade(user: User, subjectId: String, subjectName: String, score: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val state = viewModel.submitGrade(teacherId, user.id, subjectId, subjectName, score)) {
                is SubmitUiState.Success -> {
                    Snackbar.make(binding.root, "Оценка сохранена", Snackbar.LENGTH_SHORT).show()
                    val currentGroupId = groups.getOrNull(binding.spinnerSubject.selectedItemPosition)?.id
                    if (currentGroupId != null) loadStudents(currentGroupId)
                }
                is SubmitUiState.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun snack(msg: String) = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TEACHER_ID = "arg_teacher_id"
        fun newInstance(teacherId: String) = MyStudentsFragment().apply {
            arguments = bundleOf(ARG_TEACHER_ID to teacherId)
        }
    }
}