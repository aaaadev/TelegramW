package moe.astar.telegramw.ui.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moe.astar.telegramw.client.Authenticator
import moe.astar.telegramw.client.Authorization
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.TelegramClient
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
}

