package com.gabriel.cal

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gabriel.cal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Obtén el SharedViewModel a nivel de la Activity para compartirlo con los fragments
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-carga los datos desde Firestore al iniciar la app
        sharedViewModel.loadSelectedDates()

        // Configuración del NavHostFragment de forma programática
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                as? NavHostFragment ?: NavHostFragment.create(R.navigation.mobile_navigation).also {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, it)
                .setPrimaryNavigationFragment(it)
                .commitNow()
        }
        val navController = navHostFragment.navController

        val navView = binding.navView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,   // Si ya renombraste dashboard a calendar, actualiza también aquí.
                R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
