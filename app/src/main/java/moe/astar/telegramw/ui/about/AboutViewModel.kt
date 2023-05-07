package moe.astar.telegramw.ui.about

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.MessageProvider
import moe.astar.telegramw.client.TelegramClient
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    val client: TelegramClient,
    val chatProvider: ChatProvider,
    val messageProvider: MessageProvider,
    @ApplicationContext context: Context
) : ViewModel()