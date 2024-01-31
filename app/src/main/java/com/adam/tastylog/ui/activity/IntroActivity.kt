package com.adam.tastylog.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.adam.tastylog.R
import com.adam.tastylog.databinding.IntroMainBinding
import com.adam.tastylog.ui.fragment.AppIntro

class IntroActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var binding: IntroMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = IntroMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val hasCompletedIntro = prefs.getBoolean("hasCompletedIntro", false)

        if (!hasCompletedIntro) {
            // 인트로 화면 표시
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, AppIntro())
                .commit()
        } else {
            // 메인 화면으로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        val navHostFragment = supportFragmentManager.findFragmentById(binding.container.navHostFragmentContainer.id) as NavHostFragment? ?: return
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration.Builder(R.id.appIntro).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.appIntro -> {
                    // Remove FLAG_LAYOUT_NO_LIMITS to allow the button area to be taken into account
                    window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    binding.toolbar.visibility = View.GONE
                }
                else -> {
                    // Add FLAG_LAYOUT_NO_LIMITS to expand the content under the button area
                    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    binding.toolbar.visibility = View.VISIBLE
                }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}