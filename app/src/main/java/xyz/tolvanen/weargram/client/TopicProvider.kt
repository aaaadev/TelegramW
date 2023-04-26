package xyz.tolvanen.weargram.client

import androidx.compose.runtime.collectAsState
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.TdApi.ForumTopics
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class TopicProvider @Inject constructor(private val client: TelegramClient, private val chatId: Long, private val initialTopics: TdApi.ForumTopics) {

    private val TAG = this::class.simpleName

    // remove of ConcurrentSkipListSet didn't work as expected. Instead, synchronize
    // access to non-threadsafe SortedSet with a ReentrantLock
    private val topicOrdering =
        sortedSetOf<Pair<Long, Long>>(comparator = { a, b -> if (a.second < b.second) 1 else -1 })
    private val topicOrderingLock = ReentrantLock()

    private val _threadIds = MutableStateFlow(listOf<Long>())
    val threadIds: StateFlow<List<Long>> get() = _threadIds

    private val _threadData = MutableStateFlow(persistentHashMapOf<Long, TdApi.ForumTopic>())
    val threadData: StateFlow<PersistentMap<Long, TdApi.ForumTopic>> get() = _threadData

    private val scope = CoroutineScope(Dispatchers.Default)

    private fun updateProperty(topicId: Long, update: (TdApi.ForumTopic) -> TdApi.ForumTopic) {
        _threadData.value[topicId]?.also {
            _threadData.value = _threadData.value.remove(topicId)
            _threadData.value = _threadData.value.put(topicId, update(it))
        }
    }

    init {
        var _list = mutableListOf<Long>()
        initialTopics.topics.onEach { topic ->
            _list.add(topic.info.messageThreadId)
            _threadData.value = _threadData.value.put(topic.info.messageThreadId, topic)
        }
        _threadIds.value = _list.toList()

        client.updateFlow.onEach {
            when(it) {
                /*is TdApi.UpdateChatLastMessage -> {
                    if (chatId == it.chatId) {
                        it.lastMessage?.let { it1 ->
                            updateProperty(it1.messageThreadId) { topic ->
                                topic.apply { lastMessage = it.lastMessage }
                            }
                        }
                        updateTopicPositions(it.chatId, it.positions)
                    }
                }*/
                is TdApi.UpdateForumTopicInfo -> {
                    if (chatId == it.chatId) {
                        updateProperty(it.info.messageThreadId) { topic ->
                            topic.apply { info = it.info }
                        }
                    }
                }
                //is TdApi.UpdateChatFilters -> {}
                //is TdApi.UpdateChatHasProtectedContent -> {}
                //is TdApi.UpdateChatMember -> {}
                //is TdApi.UpdateChatMessageSender -> {}
                //is TdApi.UpdateChatMessageTtl -> {}
                //is TdApi.UpdateChatOnlineMemberCount -> {}
                //is TdApi.UpdateChatPendingJoinRequests -> {}
                //is TdApi.UpdateChatTheme -> {}
                //is TdApi.UpdateChatThemes -> {}
                //is TdApi.UpdateChatVideoChat -> {}
                // TODO: Make sure message content updates of last messages are updated here, too

            }
        }.launchIn(scope)
    }

    private fun updateChats() {
        topicOrderingLock.withLock { _threadIds.value = topicOrdering.toList().map { it.first } }
    }

    private fun updateTopicPositions(threadId: Long, positions: Array<TdApi.ChatPosition>) {
        topicOrderingLock.withLock {
            topicOrdering.removeIf { it.first == threadId }
            positions.dropWhile { it.list !is TdApi.ChatListMain }
                .firstOrNull()?.order?.also { order ->
                    topicOrdering.add(Pair(threadId, order))
                }
        }
        updateChats()
    }

    fun getTopic(threadId: Long): TdApi.ForumTopic? {
        return _threadData.value[threadId]
    }
}