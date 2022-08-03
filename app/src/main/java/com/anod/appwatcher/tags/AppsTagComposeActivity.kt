package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.prefs
import com.google.android.material.color.DynamicColors
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.addMultiWindowFlags
import kotlinx.coroutines.launch

class AppsTagComposeActivity : AppCompatActivity() {
    val viewModel: AppsTagViewModel by viewModels()

    val theme: Theme
        get() = Theme(this, viewModel.prefs)

    private val themeRes: Int
        get() = if (themeColors.statusBarColor.isLight)
            theme.themeLightActionBar
        else
            theme.themeDarkActionBar

    private val themeColors: CustomThemeColors
        get() = CustomThemeColors(viewModel.viewState.tag.color, theme.colors.navigationBarColor)

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = ApplicationContext(this)
        AppCompatDelegate.setDefaultNightMode(app.appCompatNightMode)
        super.onCreate(savedInstanceState)
        setTheme(this.themeRes)
        DynamicColors.applyToActivityIfAvailable(this)

        lifecycleScope.launch {
            viewModel.viewActions.collect { onViewAction(it) }
        }

        setContent {
            AppTheme(
                    customPrimaryColor = Color(viewModel.viewState.tag.color)
            ) {
                AppsTagScreen(tag = viewModel.viewState.tag, sortIndex = viewModel.viewState.sortId, onEvent = { viewModel.handleEvent(it) })
            }
        }
    }

    private fun onViewAction(action: AppsTagScreenAction) {
        when (action) {
            AppsTagScreenAction.OnBackPressed -> onBackPressed()
        }
    }

    companion object {
        fun createTagIntent(tag: Tag, context: Context) = Intent(context, AppsTagComposeActivity::class.java).apply {
            putExtra(AppsTagViewModel.EXTRA_TAG, tag)
            addMultiWindowFlags(context)
        }
    }
}