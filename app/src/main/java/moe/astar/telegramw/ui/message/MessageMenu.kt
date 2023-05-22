package moe.astar.telegramw.ui.message

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import moe.astar.telegramw.Screen
import moe.astar.telegramw.ui.util.MenuItem
import moe.astar.telegramw.ui.util.YesNoDialog
import org.drinkless.tdlib.TdApi

@Composable
fun MessageMenuScreen(
    navController: NavController,
    chatId: Long,
    messageId: Long,
    viewModel: MessageMenuViewModel
) {

    val message = viewModel.getMessage(chatId, messageId).collectAsState(initial = null)

    message.value?.also {
        MessageMenuScaffold(chatId, it, navController, viewModel)
    }

}

@Composable
fun MessageMenuScaffold(
    chatId: Long,
    message: TdApi.Message,
    navController: NavController,
    viewModel: MessageMenuViewModel
) {
    val scope = rememberCoroutineScope()

    val replyMessage = { text: String ->
        scope.launch {
            val result = CompletableDeferred<TdApi.Message>()
            scope.launch {
                viewModel.client.sendRequest(
                    TdApi.SendMessage(
                        chatId,
                        message.messageThreadId,
                        message.id,
                        TdApi.MessageSendOptions(),
                        null,
                        TdApi.InputMessageText(
                            TdApi.FormattedText(
                                text, emptyArray()
                            ),
                            false,
                            false,
                        )
                    )
                ).filterIsInstance<TdApi.Message>().collect {
                    result.complete(it)
                }
            }
            result.await()
        }
    }
    val editMessage = { text: String ->
        scope.launch {
            val result = CompletableDeferred<TdApi.Message>()
            scope.launch {
                viewModel.client.sendRequest(
                    TdApi.EditMessageText(
                        chatId,
                        message.id,
                        null,
                        TdApi.InputMessageText(
                            TdApi.FormattedText(
                                text, emptyArray()
                            ),
                            false,
                            false,
                        )
                    )
                ).filterIsInstance<TdApi.Message>().collect {
                    result.complete(it)
                }
            }
            result.await()
        }
    }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)
                val activityInput: CharSequence? = results.getCharSequence("input")
                replyMessage(activityInput.toString())
            }
        }
    val editLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)
                val activityInput: CharSequence? = results.getCharSequence("input")
                editMessage(activityInput.toString())
            }
        }
    if (showDeleteDialog.value) {
        YesNoDialog(text = "Delete message?",
            onYes = {
                viewModel.deleteMessage(chatId, message.id)
                navController.popBackStack()
            },
            onNo = { showDeleteDialog.value = false }
        )
    } else {
        Scaffold(
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        ) {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (((message.senderId as? TdApi.MessageSenderUser)?.userId
                        ?: 0) == (viewModel.getMe()?.id ?: 0)
                ) {
                    item { DeleteItem(onClick = { showDeleteDialog.value = true }) }
                    when (val msgType = message.content) {
                        is TdApi.MessageText -> {
                            item {
                                EditItem(onClick = {
                                    val intent: Intent =
                                        RemoteInputIntentHelper.createActionRemoteInputIntent()
                                    val remoteInputs: List<RemoteInput> = listOf(
                                        RemoteInput.Builder("input").setLabel("Text message?")
                                            .wearableExtender {
                                                setEmojisAllowed(true)
                                                setInputActionType(EditorInfo.IME_ACTION_DONE)
                                            }.build()
                                    )
                                    RemoteInputIntentHelper.putRemoteInputsExtra(
                                        intent,
                                        remoteInputs
                                    )
                                    editLauncher.launch(intent)
                                })
                            }
                        }
                    }
                }
                item {
                    ReactionItem(onClick = {
                        navController.navigate(
                            Screen.SelectReaction.buildRoute(
                                chatId,
                                message.id
                            )
                        )
                    })
                }
                if (message.canBeForwarded) {
                    item {
                        ForwardItem(
                            onClick = {
                                navController.currentDestination?.id?.also {
                                    navController.navigate(
                                        Screen.ChatSelect.buildRoute(
                                            message.id,
                                            message.chatId,
                                            it
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
                item {
                    Text("Reply message")
                }
                item {
                    ReplyItem(onClick = {
                        val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                        val remoteInputs: List<RemoteInput> = listOf(
                            RemoteInput.Builder("input").setLabel("Text message?")
                                .wearableExtender {
                                    setEmojisAllowed(true)
                                    setInputActionType(EditorInfo.IME_ACTION_SEND)
                                }.build()
                        )
                        RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                        launcher.launch(intent)
                    })
                }
                item {
                    ReplyStickerItem {
                        navController.navigate(Screen.SelectStickers.buildRoute(message.id, chatId))
                    }
                }
                /*message.interactionInfo?.also { interactionInfo ->
                    item {
                        Text("Reactions")
                    }
                    for (i in 0 until interactionInfo.reactions.size) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                when (val reactionType =
                                    interactionInfo.reactions[i].type) {
                                    is TdApi.ReactionTypeEmoji -> {
                                        val emoji =
                                            viewModel.getAnimatedEmoji(reactionType.emoji)
                                                .collectAsState(
                                                    initial = null
                                                )
                                        emoji.value?.also { emojiValue ->
                                            emojiValue.sticker?.also {
                                                EmojiImage(
                                                    photo = it.thumbnail!!.file,
                                                    viewModel = viewModel,
                                                    onClick = {
                                                        viewModel.removeMessageReaction(
                                                            chatId,
                                                            message.id,
                                                            reactionType
                                                        )
                                                    }
                                                )
                                                Text(
                                                    interactionInfo.reactions[i].totalCount.toString(),
                                                    style = MaterialTheme.typography.title1
                                                )
                                            }
                                        }
                                    }
                                    is TdApi.ReactionTypeCustomEmoji -> {
                                        val emoji =
                                            viewModel.getCustomEmoji(listOf(reactionType.customEmojiId))
                                                .collectAsState(
                                                    initial = null
                                                )
                                        emoji.value?.also { emojiValue ->
                                            emojiValue.stickers?.also { stickers ->
                                                stickers[0]?.also {
                                                    EmojiImage(
                                                        photo = it.thumbnail!!.file,
                                                        viewModel = viewModel,
                                                        onClick = {
                                                            viewModel.removeMessageReaction(
                                                                chatId,
                                                                message.id,
                                                                reactionType
                                                            )
                                                        }
                                                    )
                                                    Text(
                                                        interactionInfo.reactions[i].totalCount.toString(),
                                                        style = MaterialTheme.typography.title1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }*/
            }
        }
    }
}

@Composable
fun ReactionItem(onClick: () -> Unit) {
    MenuItem(
        title = "Reaction",
        imageVector = Icons.Outlined.Favorite,
        onClick = onClick
    )
}

@Composable
fun EditItem(onClick: () -> Unit) {
    MenuItem(
        title = "Edit",
        imageVector = Icons.Outlined.Edit,
        onClick = onClick
    )
}

@Composable
fun DeleteItem(onClick: () -> Unit) {
    MenuItem(
        title = "Delete",
        imageVector = Icons.Outlined.Delete,
        onClick = onClick
    )
}

@Composable
fun ReplyItem(onClick: () -> Unit) {
    MenuItem(
        title = "Text",
        imageVector = Icons.Outlined.Reply,
        onClick = onClick
    )
}

@Composable
fun ReplyStickerItem(onClick: () -> Unit) {
    MenuItem(
        title = "Sticker",
        imageVector = Icons.Outlined.EmojiEmotions,
        onClick = onClick
    )
}

@Composable
fun ForwardItem(onClick: () -> Unit) {
    MenuItem(
        title = "Forward",
        imageVector = Icons.Outlined.Forward,
        onClick = onClick
    )
}