package moe.astar.telegramw.ui.home

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import moe.astar.telegramw.client.Authenticator
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class ChatSelectViewModel @Inject constructor(
    private val authenticator: Authenticator,
    val client: TelegramClient,
    val chatProvider: ChatProvider
) : ViewModel() {
    init {
        viewModelScope.launch {
            chatProvider.loadChats()
        }
    }

    fun forwardMessageAsync(messageId: Long, chatId: Long, fromChatId: Long) {
        viewModelScope.launch {
            client.sendUnscopedRequest(
                TdApi.ForwardMessages(
                    chatId,
                    0,
                    fromChatId,
                    longArrayOf(messageId),
                    TdApi.MessageSendOptions(),
                    false,
                    false,
                    false
                )
            )
        }
    }

    fun fetchPhoto(photo: TdApi.File): Flow<ImageBitmap?> {
        return client.getFilePath(photo).map {
            it?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            }
        }
    }
}