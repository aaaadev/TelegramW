package moe.astar.telegramw.ui.topic

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import moe.astar.telegramw.ui.util.TopicMessageStatusIcon
import org.drinkless.tdlib.TdApi

@Composable
fun TopicSelectScreen(
    chatId: Long,
    navController: NavController,
    viewModel: TopicSelectViewModel,
    messageId: Long,
    fromChatId: Long,
    destId: Int
) {
    viewModel.initialize(chatId)
    TopicSelectScaffold(chatId, navController, viewModel, messageId, fromChatId, destId)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopicSelectScaffold(
    chatId: Long,
    navController: NavController,
    viewModel: TopicSelectViewModel,
    messageId: Long,
    fromChatId: Long,
    destId: Int
) {
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val topics by viewModel.topicProvider.threadIds.collectAsState()
    val topicData by viewModel.topicProvider.threadData.collectAsState()
    var showSent by remember {
        mutableStateOf(false)
    }

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
            items(topics) { topic ->
                topicData[topic]?.let {
                    val info = viewModel.getTopicInfo(chatId, it.lastMessage!!.id).collectAsState(
                        initial = null
                    )
                    if (info.value == null) {
                        ChatItem(
                            it,
                            onClick = {
                                viewModel.forwardMessageAsync(
                                    messageId,
                                    chatId,
                                    fromChatId,
                                    it.info.messageThreadId
                                )
                                showSent = true
                            },
                            viewModel
                        )
                    } else {
                        ChatItem(
                            it,
                            onClick = {
                                viewModel.forwardMessageAsync(
                                    messageId,
                                    chatId,
                                    fromChatId,
                                    it.info.messageThreadId
                                )
                                showSent = true
                            },
                            viewModel
                        )
                    }
                }
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
}

@Composable
fun ChatItem(topic: TdApi.ForumTopic, onClick: () -> Unit = {}, viewModel: TopicSelectViewModel) {
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
                                    Image(img, null,
                                        Modifier
                                            .size(20.dp)
                                            .padding(end = 7.dp))
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
fun ShortDescription(
    message: TdApi.Message,
    topic: TdApi.ForumTopic,
    viewModel: TopicSelectViewModel,
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