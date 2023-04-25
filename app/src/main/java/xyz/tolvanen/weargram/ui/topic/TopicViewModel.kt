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
) : ViewModel() {
    fun getTopics(chatId: Long): Flow<ForumTopics> {
        return client.sendRequest(TdApi.GetForumTopics(chatId, "", 0, 0, 0, Int.MAX_VALUE))
            .filterIsInstance()
    }
}