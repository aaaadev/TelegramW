package moe.astar.telegramw.ui.topic

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.TelegramClient
import moe.astar.telegramw.client.TopicProvider
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class TopicSelectViewModel @Inject constructor(
    val client: TelegramClient,
    val chatProvider: ChatProvider,
    val topicProvider: TopicProvider,
) : ViewModel() {
    fun initialize(chatId: Long) {
        val topicsValue = this.getTopics(chatId)
        topicsValue?.let {
            topicProvider.initialize(chatId, it)
        }
    }

    fun getCustomEmoji(customEmojiId: List<Long>): Flow<TdApi.Stickers> {
        return client.sendRequest(TdApi.GetCustomEmojiStickers(customEmojiId.toLongArray()))
            .filterIsInstance()
    }

    fun fetchPhoto(photo: TdApi.File): Flow<ImageBitmap?> {
        return client.getFilePath(photo).map {
            it?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            }
        }
    }

    fun getTopics(chatId: Long): TdApi.ForumTopics? {
        return chatProvider.threadData.value[chatId]
    }

    fun getChat(chatId: Long): TdApi.Chat? {
        return chatProvider.getChat(chatId)
    }

    fun getTopicInfo(chatId: Long, threadId: Long): Flow<TdApi.MessageThreadInfo> {
        return client.sendRequest(TdApi.GetMessageThread(chatId, threadId)).filterIsInstance()
    }

    fun forwardMessageAsync(messageId: Long, chatId: Long, fromChatId: Long, messageThreadId: Long) {
        viewModelScope.launch {
            client.sendUnscopedRequest(TdApi.ForwardMessages(chatId, messageThreadId, fromChatId, longArrayOf(messageId), TdApi.MessageSendOptions(), false, false, false))
        }
    }
}