package xyz.tolvanen.weargram.ui.topic

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
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
) : ViewModel() {
    fun getTopics(chatId: Long): ForumTopics? {
        return chatProvider.threadData.value[chatId]
    }

    fun getTopicInfo(chatId: Long, threadId: Long): Flow<TdApi.MessageThreadInfo> {
        return client.sendRequest(TdApi.GetMessageThread(chatId, threadId)).filterIsInstance()
    }
}