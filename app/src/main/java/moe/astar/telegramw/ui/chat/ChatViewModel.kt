package moe.astar.telegramw.ui.chat

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.ScalingLazyListItemInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.MessageProvider
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import java.lang.Float.max
import java.lang.Float.min
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sign

const val STICKERS_PER_ROW: Int = 3

@HiltViewModel
class ChatViewModel @Inject constructor(
    val client: TelegramClient,
    val chatProvider: ChatProvider,
    val messageProvider: MessageProvider,
    @ApplicationContext context: Context
) : ViewModel() {
    val readState = mutableSetOf<Long>()
    val chatState = mutableStateOf<ChatState>(ChatState.Loading)

    private val TAG = this::class.simpleName

    private val screenWidth = context.resources.displayMetrics.widthPixels

    private var _chatFlow = MutableStateFlow(TdApi.Chat())
    val chatFlow: StateFlow<TdApi.Chat> get() = _chatFlow

    private var startVisible = false

    init {
        messageProvider.messageData.onEach {
            if (it.size != 0) {
                chatState.value = ChatState.Ready
            } else {
                chatState.value = ChatState.Loading
            }
        }.launchIn(viewModelScope)
    }

    fun initialize(chatId: Long, threadId: Long?) {
        messageProvider.initialize(chatId, threadId)
        pullMessages()

        chatProvider.chatData.onEach {
            it[chatId]?.also { chat ->
                _chatFlow.value = chat
            }
        }.launchIn(viewModelScope)
    }

    fun pullMessages() {
        messageProvider.pullMessages()
    }

    fun sendMessageAsync(
        threadId: Long,
        content: TdApi.InputMessageContent
    ): Deferred<TdApi.Message> {
        return messageProvider.sendMessageAsync(threadId, 0, TdApi.MessageSendOptions(), content)
    }

    fun getUser(id: Long?): TdApi.User? = id?.let { client.getUser(it) }
    fun getBasicGroup(id: Long): TdApi.BasicGroup? = client.getBasicGroup(id)
    fun getSupergroup(id: Long): TdApi.Supergroup? = client.getSupergroup(id)

    fun getUserInfo(id: Long): TdApi.UserFullInfo? = client.getUserInfo(id)
    fun getBasicGroupInfo(id: Long): TdApi.BasicGroupFullInfo? = client.getBasicGroupInfo(id)
    fun getSupergroupInfo(id: Long): TdApi.SupergroupFullInfo? = client.getSupergroupInfo(id)

    fun onStart(chatId: Long) {
        client.sendUnscopedRequest(TdApi.OpenChat(chatId))
    }

    fun onStop(chatId: Long) {
        client.sendUnscopedRequest(TdApi.CloseChat(chatId))
    }

    fun updateVisibleItems(visibleItems: List<ScalingLazyListItemInfo>) {
        //Log.d(TAG, visibleItems.toString())
        messageProvider.updateSeenItems(
            visibleItems.map {
                if (it.key is Pair<*, *>) {
                    (it.key as Pair<*, *>).first as Long
                } else {
                    null
                }
            }.filterIsInstance<Long>()
        )

        startVisible = visibleItems.map { it.index }.contains(0)
    }

    fun fetchFile(file: TdApi.File): Flow<String?> {
        return client.getFilePath(file)
    }

    fun fetchPhoto(photo: TdApi.Photo): Flow<String?> {
        // Take the smallest photo size whose width is larger than screen width,
        // or the largest available photo size if it doesn't exist
        val photoSize = photo.sizes.dropWhile { it.width < screenWidth }.firstOrNull()
            ?: photo.sizes.last()

        return fetchFile(photoSize.photo).map {
            it
        }
    }

    fun fetchPhotoFile(photo: TdApi.File): Flow<String?> {
        return client.getFilePath(photo).map { it }
    }

    fun getAnimatedEmoji(emoji: String): Flow<TdApi.AnimatedEmoji> {
        return client.sendRequest(TdApi.GetAnimatedEmoji(emoji)).filterIsInstance()
    }

    fun getCustomEmoji(customEmojiId: List<Long>): Flow<TdApi.Stickers> {
        return client.sendRequest(TdApi.GetCustomEmojiStickers(customEmojiId.toLongArray()))
            .filterIsInstance()
    }

    fun removeMessageReaction(chatId: Long, messageId: Long, reactionType: TdApi.ReactionType) {
        viewModelScope.launch {
            client.sendUnscopedRequest(TdApi.RemoveMessageReaction(chatId, messageId, reactionType))
        }
    }

    fun fetchAudio(content: TdApi.File): Flow<MediaPlayer?> {
        return fetchFile(content).map {
            it?.let {
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(it)
                    prepare()
                }
            }
        }
    }

    fun addMessageReaction(chatId: Long, messageId: Long, reactionType: TdApi.ReactionType) {
        viewModelScope.launch {
            client.sendUnscopedRequest(
                TdApi.AddMessageReaction(
                    chatId,
                    messageId,
                    reactionType,
                    false,
                    true
                )
            )
        }
    }

    private var _scrollDirectionFlow = MutableStateFlow(1)
    val scrollDirectionFlow: StateFlow<Int> get() = _scrollDirectionFlow

    private var scrollOffset = 0f
    private val threshold = 50f

    val scrollListener = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.y > 0) {
                scrollOffset = max(scrollOffset, 0f)

            } else if (available.y < 0) {
                scrollOffset = min(scrollOffset, 0f)
            }

            scrollOffset += available.y

            if (abs(scrollOffset) > threshold) {
                _scrollDirectionFlow.value = sign(scrollOffset).toInt()
            }

            return super.onPreScroll(available, source)
        }
    }

}

sealed class ChatState {
    object Loading : ChatState()
    object Ready : ChatState()
}
