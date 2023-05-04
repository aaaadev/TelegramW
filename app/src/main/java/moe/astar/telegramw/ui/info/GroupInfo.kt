package moe.astar.telegramw.ui.info

import androidx.compose.foundation.clickable
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
import moe.astar.telegramw.Screen
import org.drinkless.tdlib.TdApi


@Composable
fun GroupInfoScreen(groupId: Long, viewModel: InfoViewModel, navController: NavController) {

    viewModel.getGroup(groupId)?.also { group ->
        GroupInfoScaffold(group, viewModel, navController)
    }
}

@Composable
fun GroupInfoScaffold(
    group: TdApi.BasicGroup,
    viewModel: InfoViewModel,
    navController: NavController
) {
    //val listState = rememberScalingLazyListState()
    val chat = viewModel.getGroupChat(group.id).collectAsState(null)
    val info = viewModel.getGroupInfo(group.id)

    chat.value?.also { chatValue ->
        val settings by viewModel.flow.collectAsState(initial = NotificationGroup.getDefaultInstance())
        val groupSetting = settings?.getGroupsOrDefault(
            chatValue.id,
            NotificationPreferneces.getDefaultInstance()
        )

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
                    item { GroupName(it) }
                }
                info?.also {
                    item { BasicGroupDescription(it) }
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

                item {
                    Text("Members")
                }

                info?.members
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

@Composable
fun UserItem(userId: Long, viewModel: InfoViewModel, navController: NavController) {
    val imageSize = 30.dp
    viewModel.getUser(userId)?.also { user ->
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable {
                    navController.navigate(Screen.Info.buildRoute("user", user.id))
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(20.dp))
                user.profilePhoto?.also {
                    InfoImage(it.small, it.minithumbnail, viewModel, imageSize = imageSize)
                } ?: run {
                    PlaceholderInfoImage(
                        painterResource(R.drawable.baseline_person_24),
                        imageSize = imageSize
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                Text(user.let { it.firstName + " " + it.lastName })

            }

        }

    }

}

@Composable
fun BasicGroupDescription(group: TdApi.BasicGroupFullInfo) {
    Text(group.description)
}

@Composable
fun GroupName(chat: TdApi.Chat) {
    Text(
        chat.title,
        style = MaterialTheme.typography.title2,
        modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
    )
}



