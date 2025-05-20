package com.flashmob.platonus.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.flashmob.platonus.R
import com.flashmob.platonus.data.model.UserRole
import com.flashmob.platonus.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    interface HomeNavigationListener {
        fun openScheduleFromHome()
        fun openGradesFromHome()
        fun openStudentsFromHome()
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userName: String
    private lateinit var userRole: UserRole
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userName = requireArguments().getString(ARG_USER_NAME) ?: ""
        userRole = UserRole.valueOf(requireArguments().getString(ARG_USER_ROLE)!!)
        userId = requireArguments().getString(ARG_USER_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ru"))
        binding.textCurrentDate.text = dateFormat.format(Calendar.getInstance().time)
        binding.textGreeting.text = getString(R.string.home_greeting, userName)
        binding.textRole.text = getString(
            R.string.home_role,
            if (userRole == UserRole.STUDENT) getString(R.string.role_student) else getString(R.string.role_teacher)
        )

        binding.cardSchedule.setOnClickListener {
            (activity as? HomeNavigationListener)?.openScheduleFromHome()
        }
        binding.cardGrades.setOnClickListener {
            (activity as? HomeNavigationListener)?.openGradesFromHome()
        }
        binding.cardMyStudents.setOnClickListener {
            (activity as? HomeNavigationListener)?.openStudentsFromHome()
        }

        binding.cardGrades.visibility =
            if (userRole == UserRole.STUDENT) View.VISIBLE else View.GONE
        binding.cardMyStudents.visibility =
            if (userRole == UserRole.TEACHER) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_USER_NAME = "arg_user_name"
        private const val ARG_USER_ROLE = "arg_user_role"
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(name: String, role: UserRole, id: String) = HomeFragment().apply {
            arguments = bundleOf(
                ARG_USER_NAME to name,
                ARG_USER_ROLE to role.name,
                ARG_USER_ID to id
            )
        }
    }
}