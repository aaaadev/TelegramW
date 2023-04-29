package xyz.tolvanen.weargram.ui.info

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import org.drinkless.tdlib.TdApi
import xyz.tolvanen.weargram.client.ChatProvider
import xyz.tolvanen.weargram.client.TelegramClient
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val client: TelegramClient,
    private val chatProvider: ChatProvider
) : ViewModel() {

    fun getUser(id: Long): TdApi.User? { return client.getUser(id) }
    fun getGroup(id: Long): TdApi.BasicGroup? { return client.getBasicGroup(id) }
    fun getGroupInfo(id: Long): TdApi.BasicGroupFullInfo? { return client.getBasicGroupInfo(id) }
    fun getChannel(id: Long): TdApi.Supergroup? { return client.getSupergroup(id) }
    fun getChannelInfo(id: Long): TdApi.SupergroupFullInfo? { return client.getSupergroupInfo(id) }
    fun fetchPhoto(photo: TdApi.File): Flow<ImageBitmap?> {
        return client.getFilePath(photo).map {
            it?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            }
        }
    }

    fun getPrivateChat(userId: Long): Flow<TdApi.Chat> {
        return client.sendRequest(TdApi.CreatePrivateChat(userId, true)).filterIsInstance()
    }

    fun getGroupChat(groupId: Long): Flow<TdApi.Chat> {
        return client.sendRequest(TdApi.CreateBasicGroupChat(groupId, true)).filterIsInstance()
    }

    fun getChannelChat(channelId: Long): Flow<TdApi.Chat> {
        return client.sendRequest(TdApi.CreateSupergroupChat(channelId, true)).filterIsInstance()
    }

    fun getMembers(channelId: Long, limit: Int): Flow<TdApi.ChatMembers> {
        return client.sendRequest(TdApi.GetSupergroupMembers(channelId, null, limit, 0)).filterIsInstance()
    }

}