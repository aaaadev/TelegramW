 package xyz.tolvanen.weargram.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.codelab.android.datastore.UserPreferences
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import xyz.tolvanen.weargram.NotificationService
import xyz.tolvanen.weargram.R
import xyz.tolvanen.weargram.Screen
import xyz.tolvanen.weargram.theme.WeargramTheme
import xyz.tolvanen.weargram.ui.chat.ChatScreen
import xyz.tolvanen.weargram.ui.home.HomeScreen
import xyz.tolvanen.weargram.ui.info.InfoScreen
import xyz.tolvanen.weargram.ui.login.LoginScreen
import xyz.tolvanen.weargram.ui.message.MessageMenuScreen
import xyz.tolvanen.weargram.ui.settings.*
import xyz.tolvanen.weargram.ui.topic.TopicScreen
import xyz.tolvanen.weargram.ui.util.MapScreen
import xyz.tolvanen.weargram.ui.util.MapView
import xyz.tolvanen.weargram.ui.util.VideoView

@Composable
fun App(enableNotification: () -> Unit, disableNotification: () -> Unit) {
    val userRepository = UserPreferencesRepository(LocalContext.current)
    val settings = userRepository.flow.collectAsState(initial = UserPreferences.getDefaultInstance())

    if (!settings.value.notificationEnabled) {
        disableNotification()
    } else {
        enableNotification()
    }

    WeargramTheme {
        val navController = rememberSwipeDismissableNavController()
        MainNavHost(navController)
    }
}

@Composable
private fun MainNavHost(navController: NavHostController) {
    SwipeDismissableNavHost(navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(navController, hiltViewModel(it))
        }

        composable(Screen.MainMenu.route) {
            MainMenuScreen(navController, hiltViewModel(it))
        }

        composable(Screen.Login.route) {
            LoginScreen(hiltViewModel(it)) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(Screen.Chat.route) {
            Screen.Chat.getChatId(it)?.also { chatId ->
                Screen.Chat.getThreadId(it)?.also { threadId ->
                        ChatScreen(
                            navController = navController,
                            chatId = chatId,
                            threadId = if (threadId == Long.MAX_VALUE) {
                                null
                            } else {
                                threadId
                            },
                            viewModel = hiltViewModel(it)
                        )
                }
            }
        }

        composable(Screen.ChatMenu.route) {
            Screen.ChatMenu.getChatId(it)?.also { chatId ->
                ChatMenuScreen(
                    navController = navController,
                    chatId = chatId,
                    viewModel = hiltViewModel(it)
                )
            }
        }

        composable(Screen.MessageMenu.route) {
            Screen.MessageMenu.getChatId(it)?.also { chatId ->
                Screen.MessageMenu.getMessageId(it)?.also { messageId ->
                    MessageMenuScreen(
                        navController = navController,
                        chatId = chatId,
                        messageId = messageId,
                        viewModel = hiltViewModel(it)
                    )
                }
            }
        }

        composable(Screen.Info.route) {
            Screen.Info.getType(it)?.also { type ->
                Screen.Info.getId(it)?.also { id ->
                    InfoScreen(
                        navController = navController,
                        type = type,
                        id = id,
                        viewModel = hiltViewModel(it)
                    )
                }
            }
        }

        composable(Screen.Video.route) {
            val path = Screen.Video.getPath(it)
            VideoView(videoUri = path)
        }

        composable(Screen.Map.route) {
            val coordinates = Screen.Map.getCoordinates(it)
            MapScreen(coordinates.first, coordinates.second)
        }

        composable(Screen.Topic.route) {
            Screen.Topic.getChatId(it)?.also { chat ->
                TopicScreen(chatId = chat, navController = navController, viewModel = hiltViewModel(it))
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = hiltViewModel(it))
        }
    }
}
