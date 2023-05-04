package moe.astar.telegramw.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import kotlinx.coroutines.launch
import moe.astar.telegramw.R
import moe.astar.telegramw.Screen
import moe.astar.telegramw.ui.info.PlaceholderInfoImage
import moe.astar.telegramw.ui.util.MessageStatusIcon
import moe.astar.telegramw.ui.util.ShortDescription
import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.TdApi.Chat
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScaffold(navController: NavController, viewModel: HomeViewModel) {
    val focusRequester = remember { FocusRequester() }
    val listState = rememberScalingLazyListState()
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
        val maxPages = 2
        var finalValue by remember { mutableStateOf(0) }
        var state = rememberPagerState(0)

        val animatedSelectedPage by animateFloatAsState(
            targetValue = state.currentPage.toFloat(),
        ) {
            finalValue = it.toInt()
        }

        val pageIndicatorState: PageIndicatorState = remember {
            object : PageIndicatorState {
                override val pageOffset: Float
                    get() = animatedSelectedPage - finalValue
                override val selectedPage: Int
                    get() = finalValue
                override val pageCount: Int
                    get() = maxPages
            }
        }
        val shape = if (LocalConfiguration.current.isScreenRound) CircleShape else null
        HorizontalPager(
            pageCount = maxPages,
            state = state,
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize().run {
                    if (shape != null) {
                        clip(shape)
                    } else {
                        this
                    }
                }
            ) {
                when (page) {
                    0 -> {
                        ChatPage(listState, focusRequester, navController, viewModel)
                    }
                    1 -> {
                        ContactsPage(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            HorizontalPageIndicator(
                pageIndicatorState = pageIndicatorState,
            )
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun ContactsPage(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val focusRequester = remember { FocusRequester() }
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val contacts = remember { viewModel.getContact() }.collectAsState(initial = null)
    contacts.value?.also { contactsValue ->
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
                Text(
                    "Contacts",
                    style = MaterialTheme.typography.title2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(contactsValue.userIds.toList()) {
                UserItem(userId = it, viewModel = viewModel, navController = navController)
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun UserItem(userId: Long, viewModel: HomeViewModel, navController: NavController) {
    val imageSize = 30.dp
    viewModel.getUser(userId)?.also { user ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate(Screen.Info.buildRoute("user", user.id))
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(20.dp))
                user.profilePhoto?.also {
                    InfoImage(it.small, it.minithumbnail, viewModel, imageSize = imageSize)
                } ?: run {
                    PlaceholderInfoImage(
                        painterResource(R.drawable.baseline_person_24),
                        imageSize = imageSize
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                Text(user.let { it.firstName + " " + it.lastName })

            }

        }

    }

}

@Composable
fun InfoImage(
    photo: TdApi.File,
    thumbnail: TdApi.Minithumbnail?,
    viewModel: HomeViewModel,
    imageSize: Dp = 120.dp
) {

    val thumbnailBitmap = remember {
        thumbnail?.let {
            val data = thumbnail.data
            val aspectRatio = thumbnail.width.toFloat() / thumbnail.height.toFloat()
            val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
            Bitmap.createScaledBitmap(bmp, 400, (400 / aspectRatio).toInt(), true).asImageBitmap()
        }
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
    ) {

        val imageModifier = Modifier
            .clip(CircleShape)
            .align(Alignment.Center)
            .size(imageSize)

        viewModel.fetchPhoto(photo).collectAsState(null).value?.also {
            Image(it, null, modifier = imageModifier)
        } ?: run {
            thumbnailBitmap?.also {
                Image(it, null, modifier = imageModifier)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

}

@Composable
fun ChatPage(
    listState: ScalingLazyListState,
    focusRequester: FocusRequester,
    navController: NavController,
    viewModel: HomeViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val chats by viewModel.chatProvider.chatIds.collectAsState()
    val chatData by viewModel.chatProvider.chatData.collectAsState()
    val forums by viewModel.chatProvider.threads.collectAsState()

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
            Text(
                "Chats",
                style = MaterialTheme.typography.title2,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate(
                            Screen.Info.buildRoute(
                                "user",
                                viewModel.getMe()!!.id
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primaryVariant,
                        contentColor = MaterialTheme.colors.onSurface
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = null,
                    )
                }
                Button(
                    onClick = { navController.navigate(Screen.Settings.buildRoute()) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primaryVariant,
                        contentColor = MaterialTheme.colors.onSurface
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.outline_settings_24),
                        contentDescription = null,
                    )
                }
            }
        }
        items(chats) { chatId ->
            chatData[chatId]?.let { chat ->
                ChatItem(
                    chat,
                    onClick = {
                        if (!forums.contains(chatId)) {
                            navController.navigate(
                                Screen.Chat.buildRoute(
                                    chatId,
                                    Long.MAX_VALUE
                                )
                            )
                        } else {
                            navController.navigate(Screen.Topic.buildRoute(chatId))
                        }
                    },
                    viewModel
                )
            }
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
