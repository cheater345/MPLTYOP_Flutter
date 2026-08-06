package com.aicoding.assistant.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aicoding.assistant.data.local.SettingsStore
import com.aicoding.assistant.data.local.ThemeMode
import com.aicoding.assistant.ui.screens.chat.ChatScreen
import com.aicoding.assistant.ui.screens.home.HomeScreen
import com.aicoding.assistant.ui.screens.settings.SettingsScreen
import com.aicoding.assistant.ui.screens.providers.ProvidersScreen
import com.aicoding.assistant.ui.screens.providers.ProviderEditScreen
import com.aicoding.assistant.ui.screens.providers.KeysScreen
import com.aicoding.assistant.ui.screens.prompts.PromptsScreen
import com.aicoding.assistant.ui.screens.prompts.PromptEditScreen
import com.aicoding.assistant.ui.theme.AiCodingTheme
import dagger.hilt.android.qualifiers.ApplicationContext

object Routes {
    const val HOME = "home"
    const val CHAT = "chat/{chatId}"
    const val SETTINGS = "settings"
    const val PROVIDERS = "providers"
    const val PROVIDER_EDIT = "providers/edit?id={id}"
    const val KEYS = "providers/keys/{providerId}"
    const val PROMPTS = "prompts"
    const val PROMPT_EDIT = "prompts/edit?id={id}"

    fun chat(chatId: Long) = "chat/$chatId"
    fun providerEdit(id: Long = 0) = "providers/edit?id=$id"
    fun keys(providerId: Long) = "providers/keys/$providerId"
    fun promptEdit(id: Long = 0) = "prompts/edit?id=$id"
}

@Composable
fun AiApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings by SettingsStore.flow(context).collectAsState(initial = com.aicoding.assistant.data.local.AppSettings())
    AiCodingTheme(
        themeMode = ThemeMode.valueOf(settings.themeMode),
        dynamicColor = settings.dynamicColor,
    ) {
        NavHost(navController = navController, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenChat = { navController.navigate(Routes.chat(it)) },
                    onNewChat = { id ->
                        navController.navigate(Routes.chat(id)) {
                            launchSingleTop = true
                        }
                    },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onPrompts = { navController.navigate(Routes.PROMPTS) },
                )
            }
            composable(
                Routes.CHAT,
                arguments = listOf(navArgument("chatId") { type = NavType.LongType })
            ) { entry ->
                val chatId = entry.arguments?.getLong("chatId") ?: return@composable
                ChatScreen(
                    chatId = chatId,
                    onBack = { navController.popBackStack() },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onProviders = { navController.navigate(Routes.PROVIDERS) },
                    onPrompts = { navController.navigate(Routes.PROMPTS) },
                )
            }
            composable(Routes.PROVIDERS) {
                ProvidersScreen(
                    onBack = { navController.popBackStack() },
                    onAdd = { navController.navigate(Routes.providerEdit()) },
                    onEdit = { navController.navigate(Routes.providerEdit(it)) },
                    onKeys = { navController.navigate(Routes.keys(it)) },
                )
            }
            composable(
                Routes.PROVIDER_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                ProviderEditScreen(
                    providerId = id,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.KEYS,
                arguments = listOf(navArgument("providerId") { type = NavType.LongType })
            ) { entry ->
                val providerId = entry.arguments?.getLong("providerId") ?: return@composable
                KeysScreen(
                    providerId = providerId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.PROMPTS) {
                PromptsScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.promptEdit(id)) },
                    onUse = { _ -> },
                )
            }
            composable(
                Routes.PROMPT_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                PromptEditScreen(promptId = id, onBack = { navController.popBackStack() })
            }
        }
    }
}