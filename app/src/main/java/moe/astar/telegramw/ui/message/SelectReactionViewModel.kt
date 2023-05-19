package moe.astar.telegramw.ui.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import moe.astar.telegramw.client.MessageProvider
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

const val REACTIONS_PER_ROW = 5

@HiltViewModel
class SelectReactionViewModel @Inject constructor(
    val client: TelegramClient,
    val messageProvider: MessageProvider,
) : ViewModel() {
    fun getAvailableReactions(chatId: Long, messageId: Long): Flow<TdApi.AvailableReactions?> {
        return client.sendRequest(
            TdApi.GetMessageAvailableReactions(
                chatId,
                messageId,
                REACTIONS_PER_ROW
            )
        ).filterIsInstance()
    }

    fun fetchPhoto(photo: TdApi.File): Flow<String?> {
        return client.getFilePath(photo).map { it }
    }

    fun getAnimatedEmoji(emoji: String): Flow<TdApi.AnimatedEmoji> {
        return client.sendRequest(TdApi.GetAnimatedEmoji(emoji)).filterIsInstance()
    }

    fun getCustomEmoji(customEmojiId: List<Long>): Flow<TdApi.Stickers> {
        return client.sendRequest(TdApi.GetCustomEmojiStickers(customEmojiId.toLongArray()))
            .filterIsInstance()
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

    fun initialize(chatId: Long, messageId: Long) {
    }
}