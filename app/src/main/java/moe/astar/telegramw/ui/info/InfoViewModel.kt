package moe.astar.telegramw.ui.info

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import moe.astar.telegramw.NotificationGroup
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.TelegramClient
import moe.astar.telegramw.ui.settings.NotificationRepository
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val client: TelegramClient,
    private val chatProvider: ChatProvider,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {
    val flow: Flow<NotificationGroup> = notificationRepository.flow

    fun setNoitificationEnabled(chatId: Long, state: Boolean) {
        viewModelScope.launch {
            notificationRepository.setNotification(chatId, state)
        }
    }

    fun getUser(id: Long): TdApi.User? {
        return client.getUser(id)
    }

    fun getUserInfo(id: Long): TdApi.UserFullInfo? {
        return client.getUserInfo(id)
    }

    fun getGroup(id: Long): TdApi.BasicGroup? {
        return client.getBasicGroup(id)
    }

    fun getGroupInfo(id: Long): TdApi.BasicGroupFullInfo? {
        return client.getBasicGroupInfo(id)
    }

    fun getChannel(id: Long): TdApi.Supergroup? {
        return client.getSupergroup(id)
    }

    fun getChannelInfo(id: Long): TdApi.SupergroupFullInfo? {
        return client.getSupergroupInfo(id)
    }

    fun fetchPhoto(photo: TdApi.File): Flow<ImageBitmap?> {
        return client.getFilePath(photo).map {
            it?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            }
        }
    }

    fun searchPublicGroup(username: String): Flow<TdApi.Chat> {
        return client.sendRequest(TdApi.SearchPublicChat(username)).filterIsInstance()
    }

    fun deleteChat(chatId: Long) {
        client.sendUnscopedRequest(TdApi.DeleteChat(chatId))
    }

    fun leaveChat(chatId: Long) {
        client.sendUnscopedRequest(TdApi.LeaveChat(chatId))
    }

    fun joinChat(chatId: Long) {
        client.sendUnscopedRequest(TdApi.JoinChat(chatId))
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
        return client.sendRequest(TdApi.GetSupergroupMembers(channelId, null, limit, 0))
            .filterIsInstance()
    }

}