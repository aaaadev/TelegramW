package xyz.tolvanen.weargram.ui.message

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import xyz.tolvanen.weargram.ui.util.MenuItem
import xyz.tolvanen.weargram.ui.util.YesNoDialog

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
                viewModel.client.sendRequest(TdApi.SendMessage(
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
                )).filterIsInstance<TdApi.Message>().collect {
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

                item { DeleteItem(onClick = { showDeleteDialog.value = true }) }
                item { ReplyItem(onClick = { val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                    val remoteInputs: List<RemoteInput> = listOf(
                        RemoteInput.Builder("input").setLabel("Text message?").wearableExtender {
                            setEmojisAllowed(true)
                            setInputActionType(EditorInfo.IME_ACTION_SEND)
                        }.build()
                    )
                    RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                    launcher.launch(intent) }) }
            }

        }
    }


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
        title = "Reply",
        imageVector = Icons.Outlined.Reply,
        onClick = onClick
    )
}