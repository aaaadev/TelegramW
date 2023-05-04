package moe.astar.telegramw.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
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
            client.sendUnscopedRequest(TdApi.ForwardMessages(chatId, 0, fromChatId, longArrayOf(messageId), TdApi.MessageSendOptions(), false, false, false))
        }
    }
}