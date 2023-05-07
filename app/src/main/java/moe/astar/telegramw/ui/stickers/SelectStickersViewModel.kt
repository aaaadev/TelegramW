package moe.astar.telegramw.ui.stickers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class SelectStickersViewModel @Inject constructor(
    val client: TelegramClient,
    val chatProvider: ChatProvider,
) : ViewModel() {
    fun getInstalledStickerSets(stickerType: TdApi.StickerType): Flow<TdApi.StickerSets> {
        return client.sendRequest(TdApi.GetInstalledStickerSets(stickerType)).filterIsInstance()
    }

    fun getStickerSet(setId: Long): Flow<TdApi.StickerSet> {
        return client.sendRequest(TdApi.GetStickerSet(setId)).filterIsInstance()
    }

    fun fetchFile(file: TdApi.File): Flow<String?> {
        return client.getFilePath(file)
    }

    fun sendSticker(chatId: Long, messageId: Long = 0, sticker: TdApi.Sticker) {
        viewModelScope.launch {
            val result = CompletableDeferred<TdApi.Message>()
            viewModelScope.launch {
                client.sendRequest(
                    TdApi.SendMessage(
                        chatId,
                        0,
                        messageId,
                        TdApi.MessageSendOptions(),
                        null,
                        TdApi.InputMessageSticker(
                            TdApi.InputFileId(sticker.sticker.id),
                            sticker.thumbnail?.file?.id?.let {
                                TdApi.InputThumbnail(
                                    TdApi.InputFileId(it),
                                    sticker.width,
                                    sticker.height
                                )
                            },
                            sticker.width,
                            sticker.height,
                            sticker.emoji,
                        )
                    )
                ).filterIsInstance<TdApi.Message>().collect {
                    result.complete(it)
                }
            }
            result.await()
        }
    }
}