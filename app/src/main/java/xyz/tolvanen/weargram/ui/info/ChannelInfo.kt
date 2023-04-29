package xyz.tolvanen.weargram.ui.info

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import org.drinkless.tdlib.TdApi
import xyz.tolvanen.weargram.R
import xyz.tolvanen.weargram.Screen


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
    val members = info?.memberCount?.let { viewModel.getMembers(group.id, it).collectAsState(
        initial = null
    ) }

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

            info?.canGetMembers?.also { canGetMembers ->
                if (canGetMembers) {
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

@Composable
fun ChannelName(chat: TdApi.Chat) {
    Text(
        chat.title,
        style = MaterialTheme.typography.title2,
        modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
    )
}