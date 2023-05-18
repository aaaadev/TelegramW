package moe.astar.telegramw.ui.home

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Confirmation
import androidx.wear.compose.material.dialog.Dialog
import kotlinx.coroutines.launch
import moe.astar.telegramw.Screen
import moe.astar.telegramw.ui.util.MessageStatusIcon
import moe.astar.telegramw.ui.util.ShortDescription
import org.drinkless.tdlib.TdApi

@Composable
fun ChatSelectScreen(
    navController: NavController,
    viewModel: ChatSelectViewModel,
    messageId: Long,
    fromChatId: Long,
    destId: Int
) {
    ChatSelectScaffold(navController, viewModel, messageId, fromChatId, destId)
}

@Composable
fun ChatSelectScaffold(
    navController: NavController,
    viewModel: ChatSelectViewModel,
    messageId: Long,
    fromChatId: Long,
    destId: Int
) {
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val chats by viewModel.chatProvider.chatIds.collectAsState()
    val chatData by viewModel.chatProvider.chatData.collectAsState()
    val forums by viewModel.chatProvider.threads.collectAsState()
    var showSent by remember {
        mutableStateOf(false)
    }
    //val forumData by viewModel.chatProvider.threadData.collectAsState()

    Scaffold(
        timeText = {
            TimeText()
        },
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState,
                modifier = Modifier
            )
        },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        listState.animateScrollBy(it.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable()
                .wrapContentHeight(),
        ) {
            items(chats) { chatId ->
                chatData[chatId]?.let { chat ->
                    ChatItem(
                        chat,
                        onClick = {
                            if (!forums.contains(chatId)) {
                                viewModel.forwardMessageAsync(messageId, chatId, fromChatId)
                                showSent = true
                            } else {
                                navController.navigate(
                                    Screen.TopicSelect.buildRoute(
                                        chatId,
                                        messageId,
                                        fromChatId,
                                        destId
                                    )
                                )
                            }
                        },
                        viewModel
                    )
                }
            }
        }

        Dialog(showDialog = showSent, onDismissRequest = {
            navController.popBackStack(destId, false, false)
            showSent = false
        }) {
            Confirmation(
                onTimeout = {
                    navController.popBackStack(destId, false, false)
                    showSent = false
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Sent message",
                        modifier = Modifier.size(48.dp)
                    )
                },
            ) {
                Text(
                    text = "Sent",
                    textAlign = TextAlign.Center
                )
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

    }
}

@Composable
fun ChatItem(chat: TdApi.Chat, onClick: () -> Unit = {}, viewModel: ChatSelectViewModel) {
    Card(
        onClick = onClick,
        backgroundPainter = ColorPainter(MaterialTheme.colors.background),
    ) {

        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            // Chat name
            Text(
                text = chat.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f)
            )

            // Time of last message
            DateTime(chat.lastMessage)

        }
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            // Last message content
            chat.lastMessage?.also {
                ShortDescription(
                    it,
                    chat,
                    viewModel,
                    viewModel.client,
                    modifier = Modifier.weight(1f)
                )
            }

            // Status indicators
            Row(
                modifier = Modifier.padding(start = 2.dp)
            ) {
                chat.lastMessage?.also { message ->
                    MessageStatusIcon(
                        message, chat, modifier = Modifier
                            .size(20.dp)
                            .padding(top = 4.dp)
                    )
                }

                if (chat.unreadMentionCount > 0) {
                    UnreadDot(text = "@", contentModifier = Modifier.padding(bottom = 2.dp))
                }

                if (chat.unreadCount - chat.unreadMentionCount > 0) {
                    UnreadDot(
                        text = if (chat.unreadCount < 100) chat.unreadCount.toString() else "99+"
                    )
                }
            }

        }
    }
}