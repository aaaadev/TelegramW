package moe.astar.telegramw.ui.info

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import moe.astar.telegramw.NotificationGroup
import moe.astar.telegramw.NotificationPreferneces
import moe.astar.telegramw.R
import org.drinkless.tdlib.TdApi


@Composable
fun ChannelInfoScreen(channelId: Long, viewModel: InfoViewModel, navController: NavController) {

    viewModel.getChannel(channelId)?.also { group ->
        ChannelInfoScaffold(group, viewModel, navController)
    }
}

@Composable
fun ChannelInfoScaffold(
    group: TdApi.Supergroup,
    viewModel: InfoViewModel,
    navController: NavController
) {
    //val listState = rememberScalingLazyListState()
    val chat = viewModel.getChannelChat(group.id).collectAsState(null)
    val info = viewModel.getChannelInfo(group.id)
    val members = info?.memberCount?.let {
        viewModel.getMembers(group.id, it).collectAsState(
            initial = null
        )
    }
    chat.value?.also { chatValue ->
        val settings by viewModel.flow.collectAsState(initial = NotificationGroup.getDefaultInstance())
        val groupSetting = settings?.getGroupsOrDefault(
            chatValue.id,
            NotificationPreferneces.getDefaultInstance()
        )

        //Log.d("ChannelInfoScaffold", chat.value.toString())
        Scaffold(
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        ) {
            ScalingLazyColumn(
                //state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        chat.value?.photo?.also {
                            InfoImage(it.big, it.minithumbnail, viewModel)
                        } ?: run {
                            PlaceholderInfoImage(painterResource(R.drawable.baseline_group_24))
                        }
                    }
                }

                chat.value?.also {
                    item { ChannelName(it) }
                }

                info?.also {
                    item { ChannelDescription(it) }
                }

                item {
                    Text("Notification Settings")
                }

                item {
                    NotificationToggle(
                        state = groupSetting?.isEnabled
                            ?: NotificationPreferneces.getDefaultInstance().isEnabled,
                        checkedChange = {
                            viewModel.setNoitificationEnabled(chatValue.id, it)
                        })
                }

                info?.canGetMembers?.also { canGetMembers ->
                    if (canGetMembers) {
                        item {
                            Text("Members")
                        }
                        members
                            ?.value
                            ?.members
                            ?.map { it.memberId }
                            ?.filterIsInstance<TdApi.MessageSenderUser>()
                            ?.map { it.userId }
                            ?.also { members ->
                                items(members) {
                                    UserItem(it, viewModel, navController)
                                }
                            }
                    }
                }
            }

        }
    }
}

@Composable
fun ChannelName(chat: TdApi.Chat) {
    Text(
        chat.title,
        style = MaterialTheme.typography.title2,
        modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
    )
}

@Composable
fun ChannelDescription(info: TdApi.SupergroupFullInfo) {
    Text(info.description)
}