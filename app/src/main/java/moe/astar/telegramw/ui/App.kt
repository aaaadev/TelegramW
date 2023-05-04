package moe.astar.telegramw.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import moe.astar.telegramw.Screen
import moe.astar.telegramw.UserPreferences
import moe.astar.telegramw.theme.WeargramTheme
import moe.astar.telegramw.ui.about.AboutScreen
import moe.astar.telegramw.ui.chat.ChatScreen
import moe.astar.telegramw.ui.home.ChatSelectScreen
import moe.astar.telegramw.ui.home.HomeScreen
import moe.astar.telegramw.ui.info.InfoScreen
import moe.astar.telegramw.ui.login.LoginScreen
import moe.astar.telegramw.ui.message.MessageMenuScreen
import moe.astar.telegramw.ui.message.SelectReactionScreen
import moe.astar.telegramw.ui.settings.SettingsScreen
import moe.astar.telegramw.ui.settings.UserPreferencesRepository
import moe.astar.telegramw.ui.topic.TopicScreen
import moe.astar.telegramw.ui.topic.TopicSelectScreen
import moe.astar.telegramw.ui.util.MapScreen
import moe.astar.telegramw.ui.util.VideoView

@Composable
fun App(enableNotification: () -> Unit, disableNotification: () -> Unit) {
    val userRepository = UserPreferencesRepository(LocalContext.current)
    val settings =
        userRepository.flow.collectAsState(initial = UserPreferences.getDefaultInstance())

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
                TopicScreen(
                    chatId = chat,
                    navController = navController,
                    viewModel = hiltViewModel(it)
                )
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = hiltViewModel(it), navController = navController)
        }

        composable(Screen.SelectReaction.route) {
            Screen.MessageMenu.getChatId(it)?.also { chatId ->
                Screen.MessageMenu.getMessageId(it)?.also { messageId ->
                    SelectReactionScreen(
                        navController = navController,
                        chatId = chatId,
                        messageId = messageId,
                        viewModel = hiltViewModel(it)
                    )
                }
            }
        }

        composable(Screen.About.route) {
            AboutScreen(navController, viewModel = hiltViewModel(it))
        }

        composable(Screen.TopicSelect.route) {
            Screen.TopicSelect.getChatId(it)?.also { chatId ->
                Screen.TopicSelect.getMessageId(it)?.also { messageId ->
                    Screen.TopicSelect.getFromChatId(it)?.also { fromChatId ->
                        Screen.TopicSelect.getDestId(it)?.also { destId ->
                            TopicSelectScreen(
                                chatId = chatId,
                                navController = navController,
                                viewModel = hiltViewModel(it),
                                messageId = messageId,
                                fromChatId = fromChatId,
                                destId = destId
                            )
                        }
                    }
                }
            }
        }

        composable(Screen.ChatSelect.route) {
            Screen.ChatSelect.getDestId(it)?.also { destId ->
                Screen.ChatSelect.getMessageId(it)?.also { messageId ->
                    Screen.ChatSelect.getFromChatId(it)?.also { fromChatId ->
                        ChatSelectScreen(
                            navController = navController,
                            viewModel = hiltViewModel(it),
                            messageId = messageId,
                            fromChatId = fromChatId,
                            destId = destId
                        )
                    }
                }
            }
        }
    }
}
