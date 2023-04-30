package moe.astar.telegramw.client

import android.util.Log
import kotlinx.collections.immutable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.drinkless.tdlib.TdApi
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class MessageProvider @Inject constructor(
    private val client: TelegramClient,
) {

    private val TAG = this::class.simpleName

    private var chatId: Long = -1
    private var threadId: Long? = null

    private val oldestMessageId = AtomicLong(0)
    private val lastQueriedMessageId = AtomicLong(-1)

    private val _messageIds = MutableStateFlow(persistentListOf<Long>())
    val messageIds: StateFlow<PersistentList<Long>> get() = _messageIds

    private val _messageData = MutableStateFlow(persistentHashMapOf<Long, TdApi.Message>())
    val messageData: StateFlow<PersistentMap<Long, TdApi.Message>> get() = _messageData

    private val scope = CoroutineScope(Dispatchers.Default)

    fun initialize(chatId: Long, threadId: Long?) {
        this.chatId = chatId
        this.threadId = threadId

        Log.d("MessageProvider", "threadId: " + this.threadId.toString())

        client.updateFlow
            .filterIsInstance<TdApi.UpdateNewMessage>()
            .filter { it.message.chatId == chatId && (threadId == null || threadId == it.message.messageThreadId) }
            .onEach {
                if (!_messageData.value.contains(it.message.id)) {
                    _messageData.value = _messageData.value.put(it.message.id, it.message)
                    _messageIds.value = _messageIds.value.add(0, it.message.id)
                }
            }.launchIn(scope)

        client.updateFlow
            .filterIsInstance<TdApi.UpdateMessageSendSucceeded>()
            .filter { it.message.chatId == chatId && (threadId == null || threadId == it.message.messageThreadId) }
            .onEach {
                if (_messageData.value.contains(it.oldMessageId)) {
                    _messageIds.value = _messageIds.value.mutate { list ->
                        if (_messageIds.value.contains(it.oldMessageId)) {
                            list[_messageIds.value.indexOf(it.oldMessageId)] = it.message.id
                        } else {
                            _messageIds.value = _messageIds.value.add(0, it.message.id)
                        }
                    }
                    _messageData.value = _messageData.value.remove(it.oldMessageId)
                    _messageData.value = _messageData.value.put(it.message.id, it.message)
                } else {
                    if (!_messageData.value.contains(it.message.id)) {
                        _messageData.value = _messageData.value.put(it.message.id, it.message)
                        _messageIds.value = _messageIds.value.add(0, it.message.id)
                    }
                }
            }.launchIn(scope)

        client.updateFlow
            .filterIsInstance<TdApi.UpdateDeleteMessages>()
            .filter { it.chatId == chatId }
            .filter { it.isPermanent }
            .onEach {
                var removeList = mutableListOf<Long>()
                it.messageIds.forEach { id ->
                    if (_messageData.value.contains(id)) {
                        _messageData.value = _messageData.value.remove(id)
                        removeList.add(id)
                    }
                }
                _messageIds.value = _messageIds.value.removeAll(removeList.toList())
            }.launchIn(scope)

        client.updateFlow
            .filterIsInstance<TdApi.UpdateMessageContent>()
            .filter { it.chatId == chatId }
            .onEach {
                _messageData.value[it.messageId]?.also { msg ->
                    if (_messageData.value.contains(it.messageId)) {
                        msg.content = it.newContent
                        _messageData.value = _messageData.value.remove(it.messageId)
                        _messageData.value = _messageData.value.put(it.messageId, msg)
                    }
                }
            }.launchIn(scope)
    }

    fun pullMessages(limit: Int = 10) {
        if (lastQueriedMessageId.get() != oldestMessageId.get()) {
            val msgId = oldestMessageId.get()
            lastQueriedMessageId.set(msgId)

            val messageSource = getMessages(msgId, limit)
            scope.launch {
                messageSource.collect { messages ->
                    _messageData.value =
                        _messageData.value.putAll(messages.associateBy { message -> message.id })
                    _messageIds.value =
                        _messageIds.value.addAll(messages.map { message -> message.id })

                    _messageIds.value.lastOrNull()?.also { id ->
                        if (oldestMessageId.get() != id) {
                            oldestMessageId.set(id)
                        }
                    }
                }
            }
        }

    }

    private fun getMessages(fromMessageId: Long, limit: Int): Flow<List<TdApi.Message>> =
        if (threadId != null) {
            client.sendRequest(
                TdApi.GetMessageThreadHistory(
                    chatId,
                    threadId!!,
                    fromMessageId,
                    0,
                    limit
                )
            )
                .filterIsInstance<TdApi.Messages>()
                .map { it.messages.toList() }
        } else {
            client.sendRequest(TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false))
                .filterIsInstance<TdApi.Messages>()
                .map { it.messages.toList() }
        }


    fun sendMessageAsync(
        messageThreadId: Long = 0,
        replyToMessageId: Long = 0,
        options: TdApi.MessageSendOptions = TdApi.MessageSendOptions(),
        inputMessageContent: TdApi.InputMessageContent
    ): Deferred<TdApi.Message> = sendMessageAsync(
        TdApi.SendMessage(
            chatId,
            messageThreadId,
            replyToMessageId,
            options,
            null,
            inputMessageContent
        )
    )

    private fun sendMessageAsync(sendMessage: TdApi.SendMessage): Deferred<TdApi.Message> {
        val result = CompletableDeferred<TdApi.Message>()
        scope.launch {
            client.sendRequest(sendMessage).filterIsInstance<TdApi.Message>().collect {
                result.complete(it)
            }
        }
        return result
    }

    fun updateSeenItems(items: List<Long>) {
        client.sendUnscopedRequest(
            TdApi.ViewMessages(
                chatId,
                items.toLongArray(),
                TdApi.MessageSourceChatList(),
                false
            )
        )
    }
}