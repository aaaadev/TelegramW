package xyz.tolvanen.weargram.ui.message

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import xyz.tolvanen.weargram.client.MessageProvider
import xyz.tolvanen.weargram.client.TelegramClient
import javax.inject.Inject

@HiltViewModel
class MessageMenuViewModel @Inject constructor(val client: TelegramClient, val messageProvider: MessageProvider,) : ViewModel() {
    fun getMessage(chatId: Long, messageId: Long): Flow<TdApi.Message> {
        return client.sendRequest(TdApi.GetMessage(chatId, messageId)).filterIsInstance()
    }

    fun getMe(): TdApi.User? {
        return client.getMe()
    }

    fun deleteMessage(chatId: Long, messageId: Long) {
        client.sendUnscopedRequest(TdApi.DeleteMessages(chatId, longArrayOf(messageId), true))
    }

    fun getAnimatedEmoji(emoji: String): Flow<TdApi.AnimatedEmoji> {
        return client.sendRequest(TdApi.GetAnimatedEmoji(emoji)).filterIsInstance()
    }

    fun getCustomEmoji(customEmojiId: List<Long>): Flow<TdApi.Stickers> {
        return client.sendRequest(TdApi.GetCustomEmojiStickers(customEmojiId.toLongArray())).filterIsInstance()
    }

    fun removeMessageReaction(chatId: Long, messageId: Long, reactionType: TdApi.ReactionType) {
        viewModelScope.launch {
            client.sendUnscopedRequest(TdApi.RemoveMessageReaction(chatId, messageId, reactionType))
        }
    }

    fun fetchPhoto(photo: TdApi.File): Flow<ImageBitmap?> {
        return client.getFilePath(photo).map {
            it?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            }
        }
    }
}