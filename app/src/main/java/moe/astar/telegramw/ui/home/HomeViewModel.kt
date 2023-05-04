package moe.astar.telegramw.ui.home

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import moe.astar.telegramw.client.Authenticator
import moe.astar.telegramw.client.Authorization
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authenticator: Authenticator,
    val client: TelegramClient,
    val chatProvider: ChatProvider
) : ViewModel() {

    val homeState = mutableStateOf<HomeState>(HomeState.Loading)

    init {
        authenticator.authorizationState.onEach {
            Log.d("HomeViewModel", "$it")
            when (it) {
                Authorization.UNAUTHORIZED -> {
                    homeState.value = HomeState.Loading
                    authenticator.startAuthorization()
                }
                Authorization.AUTHORIZED -> {
                    chatProvider.loadChats()
                    homeState.value = HomeState.Ready
                }
                else -> {
                    if (homeState.value != HomeState.Login) {
                        homeState.value = HomeState.Login
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getContact(): Flow<TdApi.Users> {
        return client.sendRequest(TdApi.GetContacts()).filterIsInstance()
    }

    fun fetchPhoto(photo: TdApi.File): Flow<ImageBitmap?> {
        return client.getFilePath(photo).map {
            it?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            }
        }
    }

    fun getUser(id: Long): TdApi.User? {
        return client.getUser(id)
    }

    fun getMe(): TdApi.User? {
        return client.getMe()
    }
}

