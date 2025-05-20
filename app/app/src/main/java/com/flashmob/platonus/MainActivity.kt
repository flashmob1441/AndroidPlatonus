package com.flashmob.platonus

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.flashmob.platonus.data.model.UserRole
import com.flashmob.platonus.data.storage.AuthManager
import com.flashmob.platonus.databinding.ActivityMainBinding
import com.flashmob.platonus.ui.auth.LoginActivity
import com.flashmob.platonus.ui.grades.GradesFragment
import com.flashmob.platonus.ui.home.HomeFragment
import com.flashmob.platonus.ui.mystudents.MyStudentsFragment
import com.flashmob.platonus.ui.schedule.ScheduleFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    HomeFragment.HomeNavigationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userRole: UserRole
    private lateinit var userId: String
    private var userCourse: Int? = null

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_USER_ROLE = "extra_user_role"
        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_USER_EMAIL = "extra_user_email"
        const val EXTRA_USER_COURSE = "extra_user_course"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra(EXTRA_USER_ID) ?: run { logout(); return }
        val roleName = intent.getStringExtra(EXTRA_USER_ROLE)
        userRole = UserRole.valueOf(roleName ?: UserRole.STUDENT.name)
        userCourse = intent.getIntExtra(EXTRA_USER_COURSE, 0).takeIf { it != 0 }

        val userName = intent.getStringExtra(EXTRA_USER_NAME)
        val userEmail = intent.getStringExtra(EXTRA_USER_EMAIL)

        setupToolbar()
        setupNavigationDrawer(userName, userEmail)
        setupRoleSpecificUI()
        supportFragmentManager.addOnBackStackChangedListener(backStackListener)

        if (savedInstanceState == null) {
            navigateToFragment(
                HomeFragment.newInstance(userName ?: "Пользователь", userRole, userId),
                "Главная",
                R.id.nav_home
            )
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (supportFragmentManager.backStackEntryCount > 1) {
                        supportFragmentManager.popBackStack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        when (currentFragment()) {
            is HomeFragment -> binding.navView.setCheckedItem(R.id.nav_home)
            is ScheduleFragment -> binding.navView.setCheckedItem(R.id.nav_schedule)
            is GradesFragment -> binding.navView.setCheckedItem(R.id.nav_grades)
            is MyStudentsFragment -> binding.navView.setCheckedItem(R.id.nav_my_students)
        }
        updateToolbarTitleFromFragment()
    }

    private fun currentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(R.id.fragment_container)

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupNavigationDrawer(userName: String?, userEmail: String?) {
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        val headerView = binding.navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.textViewUserName).text = userName ?: "N/A"
        headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail ?: "N/A"
    }

    private fun setupRoleSpecificUI() {
        val menu = binding.navView.menu
        menu.findItem(R.id.nav_my_students).isVisible = userRole == UserRole.TEACHER
        menu.findItem(R.id.nav_grades).isVisible = userRole == UserRole.STUDENT
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navigateToFragment(
                HomeFragment.newInstance(
                    intent.getStringExtra(EXTRA_USER_NAME) ?: "Пользователь",
                    userRole,
                    userId
                ), "Главная", R.id.nav_home
            )

            R.id.nav_schedule -> navigateToFragment(
                ScheduleFragment.newInstance(userId, userRole),
                "Расписание",
                R.id.nav_schedule
            )

            R.id.nav_grades -> if (userRole == UserRole.STUDENT) navigateToFragment(
                GradesFragment.newInstance(userId, userRole, userCourse),
                "Оценки",
                R.id.nav_grades
            )

            R.id.nav_my_students -> if (userRole == UserRole.TEACHER) navigateToFragment(
                MyStudentsFragment.newInstance(userId),
                "Мои студенты",
                R.id.nav_my_students
            )

            R.id.nav_logout -> showLogoutConfirmationDialog()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun navigateToFragment(fragment: Fragment, title: String, menuId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(title)
            .commit()
        binding.navView.setCheckedItem(menuId)
        supportActionBar?.title = title
    }

    private fun updateToolbarTitleFromFragment() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            val title =
                supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name
            supportActionBar?.title = title
        } else {
            supportActionBar?.title = getString(R.string.app_name)
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { _, _ -> logout() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun logout() {
        AuthManager(this).clear()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    override fun openScheduleFromHome() {
        navigateToFragment(
            ScheduleFragment.newInstance(userId, userRole),
            "Расписание",
            R.id.nav_schedule
        )
    }

    override fun openGradesFromHome() {
        if (userRole == UserRole.STUDENT)
            navigateToFragment(
                GradesFragment.newInstance(userId, userRole, userCourse),
                "Оценки",
                R.id.nav_grades
            )
    }

    override fun openStudentsFromHome() {
        if (userRole == UserRole.TEACHER) {
            navigateToFragment(
                MyStudentsFragment.newInstance(userId),
                "Мои студенты",
                R.id.nav_my_students
            )
        }
    }
}