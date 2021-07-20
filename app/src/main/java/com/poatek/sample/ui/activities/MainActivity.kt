package com.poatek.sample.ui.activities

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.poatek.sample.R
import com.poatek.sample.databinding.ActivityMainBinding
import com.poatek.sample.ui.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var navController: NavController

    override fun onCreateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun onStart() {
        super.onStart()
        navController = Navigation.findNavController(this, R.id.navHostFragment)
        NavigationUI.setupWithNavController(binding.toolbar, navController)
    }

}