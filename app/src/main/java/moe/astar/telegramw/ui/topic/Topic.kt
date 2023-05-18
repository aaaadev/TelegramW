package moe.astar.telegramw.ui.topic

import androidx.compose.foundation.Image
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
import kotlinx.coroutines.launch
import moe.astar.telegramw.R
import moe.astar.telegramw.Screen
import moe.astar.telegramw.ui.util.MenuItem
import moe.astar.telegramw.ui.util.TopicMessageStatusIcon
import org.drinkless.tdlib.TdApi
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TopicScreen(chatId: Long, navController: NavController, viewModel: TopicViewModel) {
    viewModel.initialize(chatId)
    TopicScaffold(chatId, navController, viewModel)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopicScaffold(chatId: Long, navController: NavController, viewModel: TopicViewModel) {
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val topics by viewModel.topicProvider.threadIds.collectAsState()
    val topicData by viewModel.topicProvider.threadData.collectAsState()

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
            item {
                viewModel.getChat(chatId)?.also { chat ->
                    (chat.type as? TdApi.ChatTypeSupergroup)?.also {
                        MenuItem(
                            modifier = Modifier.padding(bottom = 10.dp),
                            title = "Forum Info",
                            iconPainter = painterResource(id = R.drawable.baseline_info_24),
                            onClick = {
                                navController.navigate(
                                    Screen.Info.buildRoute(
                                        "channel",
                                        it.supergroupId
                                    )
                                )
                            })
                    }
                }
            }
            items(topics) { topic ->
                topicData[topic]?.let {
                    val info = viewModel.getTopicInfo(chatId, it.lastMessage!!.id).collectAsState(
                        initial = null
                    )
                    if (info.value == null) {
                        ChatItem(
                            it,
                            onClick = {
                                navController.navigate(
                                    Screen.Chat.buildRoute(
                                        chatId,
                                        it.info.messageThreadId
                                    )
                                )
                            },
                            viewModel
                        )
                    } else {
                        ChatItem(
                            it,
                            onClick = {
                                navController.navigate(
                                    Screen.Chat.buildRoute(
                                        chatId,
                                        info.value!!.messageThreadId
                                    )

                                )
                            },
                            viewModel
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ShortText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF888888),
    user: String? = null
) {

    Text(
        text = buildAnnotatedString {
            append(user?.let { "$it: " } ?: "")

            withStyle(style = SpanStyle(color = color)) {
                append(text)
            }

        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.caption2,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
fun ShortDescription(
    message: TdApi.Message,
    topic: TdApi.ForumTopic,
    viewModel: TopicViewModel,
    modifier: Modifier = Modifier
) {
    val altColor = Color(0xFF4588BE)

    val senderId = message.senderId
    val myId = remember(viewModel) { viewModel.client.me.value }

    val user = if (senderId is TdApi.MessageSenderUser) {
        viewModel.client.getUser(senderId.userId)?.firstName
    } else null

    val username = if (senderId is TdApi.MessageSenderUser) {
        if (senderId.userId == myId) {
            "You"
        } else viewModel.client.getUser(senderId.userId)?.let {
            it.firstName + " " + it.lastName
        }
    } else null

    when (val content = message.content) {
        is TdApi.MessageText -> ShortText(content.text.text, modifier, user = user)
        is TdApi.MessagePhoto -> ShortText("Photo", modifier, color = altColor, user = user)
        is TdApi.MessageAudio -> ShortText("Audio", modifier, color = altColor, user = user)
        is TdApi.MessageVoiceNote -> ShortText(
            "Voice note",
            modifier,
            color = altColor,
            user = user
        )
        is TdApi.MessageVideo -> ShortText("Video", modifier, color = altColor, user = user)
        is TdApi.MessageVideoNote -> ShortText(
            "Video note",
            modifier,
            color = altColor,
            user = user
        )
        is TdApi.MessageCall -> ShortText("Call", modifier, color = altColor, user = user)
        is TdApi.MessageAnimation -> ShortText("GIF", modifier, color = altColor, user = user)
        is TdApi.MessageAnimatedEmoji -> ShortText(
            content.emoji,
            modifier,
            color = altColor,
            user = user
        )
        is TdApi.MessageLocation -> ShortText("Location", modifier, color = altColor, user = user)
        is TdApi.MessageContact -> ShortText("Contact", modifier, color = altColor, user = user)
        is TdApi.MessageDocument -> ShortText("Document", modifier, color = altColor, user = user)
        is TdApi.MessagePoll -> ShortText("Poll", modifier, color = altColor, user = user)
        is TdApi.MessageSticker -> ShortText(
            content.sticker.emoji + " Sticker",
            modifier,
            color = altColor, user = user
        )

        is TdApi.MessageBasicGroupChatCreate -> ShortText(
            "$username created the group",
            color = altColor
        )
        is TdApi.MessageChatAddMembers -> ShortText("$username added members", color = altColor)
        is TdApi.MessageChatChangePhoto -> ShortText(
            "$username changed group photo",
            color = altColor
        )
        is TdApi.MessageChatJoinByLink -> ShortText(
            "Member joined via an invite link",
            color = altColor
        )
        is TdApi.MessageChatJoinByRequest -> ShortText("Member joined by request", color = altColor)
        is TdApi.MessageChatSetTheme -> ShortText("Chat theme was set", color = altColor)
        is TdApi.MessageChatUpgradeFrom -> ShortText(
            "Supergroup was created from group",
            color = altColor
        )
        is TdApi.MessageChatUpgradeTo -> ShortText(
            "Supergroup was created from group",
            color = altColor
        )
        is TdApi.MessageContactRegistered -> ShortText(
            "$username joined Telegram",
            color = altColor
        )
        is TdApi.MessageCustomServiceAction -> ShortText(content.text, color = altColor)
        is TdApi.MessageDice -> ShortText("${content.emoji} Dice", color = altColor, user = user)
        is TdApi.MessageExpiredPhoto -> ShortText("Expired Photo", color = altColor, user = user)
        is TdApi.MessageExpiredVideo -> ShortText("Expired Video", color = altColor, user = user)
        is TdApi.MessageGame -> ShortText("Game", color = altColor)
        is TdApi.MessageGameScore -> ShortText("Game Score", color = altColor)
        is TdApi.MessageInviteVideoChatParticipants -> ShortText(
            "Invite to group call",
            color = altColor
        )
        is TdApi.MessageInvoice -> ShortText("Invoice", color = altColor)
        //is TdApi.MessagePassportDataReceived -> ShortText("", color = altColor)
        is TdApi.MessagePassportDataSent -> ShortText("Passport data sent", color = altColor)
        is TdApi.MessagePaymentSuccessful -> ShortText("Payment Successful", color = altColor)
        //is TdApi.MessagePaymentSuccessfulBot -> ShortText("", color = altColor)
        is TdApi.MessagePinMessage -> ShortText("Message was pinned", color = altColor)
        is TdApi.MessageProximityAlertTriggered -> ShortText("Proximity alert", color = altColor)
        is TdApi.MessageScreenshotTaken -> ShortText("Screenshot was taken", color = altColor)
        is TdApi.MessageSupergroupChatCreate -> ShortText(
            "Supergroup was created",
            color = altColor
        )
        is TdApi.MessageVenue -> ShortText("Venue", color = altColor)
        is TdApi.MessageVideoChatEnded -> ShortText("Video chat ended", color = altColor)
        is TdApi.MessageVideoChatScheduled -> ShortText("Video chat scheduled", color = altColor)
        is TdApi.MessageVideoChatStarted -> ShortText("Video chat started", color = altColor)
        is TdApi.MessageWebsiteConnected -> ShortText("Website connected", color = altColor)
        else -> ShortText("Unsupported message", modifier, color = altColor, user = user)
    }


}

@Composable
fun ChatItem(topic: TdApi.ForumTopic, onClick: () -> Unit = {}, viewModel: TopicViewModel) {
    Card(
        onClick = onClick,
        backgroundPainter = ColorPainter(MaterialTheme.colors.background),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            val emojis: State<TdApi.Stickers?> =
                viewModel.getCustomEmoji(listOf(topic.info.icon.customEmojiId)).collectAsState(
                    initial = null
                )
            emojis.value?.also { emojisValue ->
                emojisValue.stickers?.also { stickers ->
                    if (stickers.isNotEmpty()) {
                        stickers[0]?.also { emoji ->
                            viewModel.fetchPhoto(emoji.thumbnail!!.file)
                                .collectAsState(null).value?.also { img ->
                                    Image(img, null, Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(7.dp))
                                }
                        }
                    }
                }
            }
            // Chat name
            Text(
                text = topic.info.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f)
            )

            // Time of last message
            DateTime(topic.lastMessage)

        }
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            // Last message content
            topic.lastMessage?.also {
                ShortDescription(it, topic, viewModel, modifier = Modifier.weight(1f))
            }

            // Status indicators
            Row(
                modifier = Modifier.padding(start = 2.dp)
            ) {
                topic.lastMessage?.also { message ->
                    TopicMessageStatusIcon(
                        message, topic, modifier = Modifier
                            .size(20.dp)
                            .padding(top = 4.dp)
                    )
                }

                if (topic.unreadMentionCount > 0) {
                    UnreadDot(text = "@", contentModifier = Modifier.padding(bottom = 2.dp))
                }

                if (topic.unreadCount - topic.unreadMentionCount > 0) {
                    UnreadDot(
                        text = if (topic.unreadCount < 100) topic.unreadCount.toString() else "99+"
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