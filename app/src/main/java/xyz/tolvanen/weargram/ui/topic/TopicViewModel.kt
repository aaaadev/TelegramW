package xyz.tolvanen.weargram.ui.topic

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import xyz.tolvanen.weargram.client.ChatProvider
import xyz.tolvanen.weargram.client.MessageProvider
import xyz.tolvanen.weargram.client.TelegramClient
import xyz.tolvanen.weargram.client.TopicProvider
import javax.inject.Inject
import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.TdApi.ForumTopics

@HiltViewModel
class TopicViewModel  @Inject constructor(
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
        return client.sendRequest(TdApi.GetCustomEmojiStickers(customEmojiId.toLongArray())).filterIsInstance()
    }

    fun fetchPhoto(photo: TdApi.File): Flow<ImageBitmap?> {
        return client.getFilePath(photo).map {
            it?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            }
        }
    }

    fun getTopics(chatId: Long): ForumTopics? {
        return chatProvider.threadData.value[chatId]
    }
    fun getTopicInfo(chatId: Long, threadId: Long): Flow<TdApi.MessageThreadInfo> {
        return client.sendRequest(TdApi.GetMessageThread(chatId, threadId)).filterIsInstance()
    }
}