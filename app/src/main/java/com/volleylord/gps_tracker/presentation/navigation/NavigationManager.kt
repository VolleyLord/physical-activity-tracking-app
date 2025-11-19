package com.volleylord.gps_tracker.presentation.navigation

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized navigation event bus to keep navigation out of UI/business layers.
 * Screens send intents to this manager; the NavHost collects commands and performs navigation.
 */
@Singleton
class NavigationManager @Inject constructor() {

    private val _commands = MutableSharedFlow<NavigationCommand>(
        extraBufferCapacity = 1
    )
    val commands: SharedFlow<NavigationCommand> = _commands.asSharedFlow()

    fun navigateTo(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
        _commands.tryEmit(NavigationCommand.NavigateTo(route, builder))
    }

    fun navigateUp() {
        _commands.tryEmit(NavigationCommand.NavigateUp)
    }
}

sealed interface NavigationCommand {
    data class NavigateTo(
        val route: String,
        val builder: NavOptionsBuilder.() -> Unit = {}
    ) : NavigationCommand

    data object NavigateUp : NavigationCommand
}

