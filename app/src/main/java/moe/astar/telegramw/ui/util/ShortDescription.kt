package moe.astar.telegramw.ui.util

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi

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
    chat: TdApi.Chat,
    viewModel: ViewModel,
    client: TelegramClient,
    modifier: Modifier = Modifier
) {
    val altColor = Color(0xFF4588BE)

    val senderId = message.senderId
    val chatType = chat.type
    val myId = remember(viewModel) { client.me.value }

    val user = if (senderId is TdApi.MessageSenderUser) {
        if (senderId.userId == myId) {
            if (chatType is TdApi.ChatTypePrivate && chatType.userId == myId) null
            else "You"
        } else if (chat.type is TdApi.ChatTypeSupergroup || chat.type is TdApi.ChatTypeBasicGroup) {
            client.getUser(senderId.userId)?.firstName
        } else null
    } else null

    val username = if (senderId is TdApi.MessageSenderUser) {
        if (senderId.userId == myId) {
            "You"
        } else client.getUser(senderId.userId)?.let {
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