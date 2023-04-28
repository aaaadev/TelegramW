package xyz.tolvanen.weargram.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.TdApi.Chat
import org.drinkless.tdlib.TdApi.ChatTypeBasicGroup
import org.drinkless.tdlib.TdApi.ChatTypeSupergroup
import org.drinkless.tdlib.TdApi.ForumTopics
import xyz.tolvanen.weargram.R
import xyz.tolvanen.weargram.Screen
import xyz.tolvanen.weargram.ui.util.MessageStatusIcon
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import xyz.tolvanen.weargram.ui.util.ShortDescription

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {

    val homeState by viewModel.homeState

    when (homeState) {
        HomeState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        HomeState.Login -> {
            navController.navigate(Screen.Login.route) {
                launchSingleTop = true
            }
        }
        HomeState.Ready -> {
            HomeScaffold(navController, viewModel)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScaffold(navController: NavController, viewModel: HomeViewModel) {
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val chats by viewModel.chatProvider.chatIds.collectAsState()
    val chatData by viewModel.chatProvider.chatData.collectAsState()
    val forums by viewModel.chatProvider.threads.collectAsState()
    //val forumData by viewModel.chatProvider.threadData.collectAsState()

    Log.d("HomeScaffold", "chats: " + chats.size.toString())
    Log.d("HomeScaffold", "chatData: " + chatData.size.toString())

    Scaffold(
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

            item {
                CompactButton(
                    onClick = { navController.navigate(Screen.MainMenu.route) },
                    modifier = Modifier.padding(6.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                ) {
                    Icon(
                        painterResource(id = R.drawable.baseline_menu_24),
                        contentDescription = null,
                    )

                }
            }
            items(chats) { chatId ->
                chatData[chatId]?.let { chat ->
                    ChatItem(
                        chat,
                        onClick = {
                            if (!forums.contains(chatId)) {
                                navController.navigate(Screen.Chat.buildRoute(chatId, Long.MAX_VALUE))
                            } else {
                                navController.navigate(Screen.Topic.buildRoute(chatId))
                            }
                                  },
                        viewModel
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit = {}, viewModel: HomeViewModel) {
    Card(
        onClick = onClick,
        backgroundPainter = ColorPainter(MaterialTheme.colors.surface),
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
                ShortDescription(it, chat, viewModel, viewModel.client, modifier = Modifier.weight(1f))
            }

            // Status indicators
            Row(
                modifier = Modifier.padding(start = 2.dp)
            ) {
                chat.lastMessage?.also {message ->
                    MessageStatusIcon(message, chat, modifier = Modifier
                        .size(20.dp)
                        .padding(top = 4.dp))
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

@Composable
fun DateTime(message: TdApi.Message?) {
    val locale = LocalContext.current.resources.configuration.locales[0]

    val text = remember(message) {
        message?.date?.let {
            val date = Date(it.toLong() * 1000)
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            val lastWeek = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }
            val lastYear = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }

            if (date.after(yesterday.time)) {
                DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
            } else if (date.after(lastWeek.time)) {
                SimpleDateFormat("EEE", locale).format(date)
            } else if (date.after(lastYear.time)) {
                SimpleDateFormat("dd MMM", locale).format(date)
            } else {
                DateFormat.getDateInstance(DateFormat.SHORT).format(date)
            }
        }
    }
    Text(
        text ?: "",
        modifier = Modifier.padding(start = 2.dp),
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun UnreadDot(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    text: String = ""
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(start = 2.dp, top = 2.dp)
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(MaterialTheme.colors.primaryVariant, CircleShape)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.caption3,
            modifier = contentModifier.align(Alignment.Center)
        )
    }

}

sealed class HomeState {
    object Loading : HomeState()
    object Login : HomeState()
    object Ready : HomeState()
}
