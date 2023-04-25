package xyz.tolvanen.weargram.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.drinkless.tdlib.TdApi
import xyz.tolvanen.weargram.client.ChatProvider
import xyz.tolvanen.weargram.client.TelegramClient
import javax.inject.Inject

@HiltViewModel
class ChatMenuViewModel @Inject constructor(
    private val client: TelegramClient,
    private val chatProvider: ChatProvider
) : ViewModel() {
    fun getChat(chatId: Long): TdApi.Chat? {
        return chatProvider.getChat(chatId)
    }

    fun deleteChat(chatId: Long) {
        client.sendUnscopedRequest(TdApi.DeleteChat(chatId))
    }

    fun leaveChat(chatId: Long) {
        client.sendUnscopedRequest(TdApi.LeaveChat(chatId))
    }

}
