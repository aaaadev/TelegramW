package moe.astar.telegramw.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class ChatMenuViewModel @Inject constructor(
    private val client: TelegramClient,
    private val chatProvider: ChatProvider,
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
