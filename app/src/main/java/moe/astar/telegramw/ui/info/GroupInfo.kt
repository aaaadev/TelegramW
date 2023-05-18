package moe.astar.telegramw.ui.info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import moe.astar.telegramw.NotificationGroup
import moe.astar.telegramw.NotificationPreferneces
import moe.astar.telegramw.R
import moe.astar.telegramw.Screen
import moe.astar.telegramw.ui.util.MenuItem
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
    var isLeftGroup by remember { mutableStateOf(false) }
    var isJoinGroup by remember { mutableStateOf(false) }

    if (isLeftGroup) {
        Alert(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = "left",
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(align = Alignment.Center),
                )
            },
            title = { Text("Left the channel", textAlign = TextAlign.Center) },
            negativeButton = {
                Button(
                    colors = ButtonDefaults.secondaryButtonColors(),
                    onClick = {
                        isLeftGroup = false
                    }) {
                    Text("No")
                }
            },
            positiveButton = {
                Button(onClick = {
                    chat.value?.also {
                        viewModel.leaveChat(it.id)
                    }
                    navController.navigate(Screen.Home.route)
                    isLeftGroup = false
                }) { Text("Yes") }
            },
            contentPadding =
            PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
        ) {
            Text(
                text = "Are you sure?",
                textAlign = TextAlign.Center
            )
        }
    } else if (isJoinGroup) {
        Alert(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Login,
                    contentDescription = "join",
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(align = Alignment.Center),
                )
            },
            title = { Text("Join the channel", textAlign = TextAlign.Center) },
            negativeButton = {
                Button(
                    colors = ButtonDefaults.secondaryButtonColors(),
                    onClick = {
                        isJoinGroup = false
                    }) {
                    Text("No")
                }
            },
            positiveButton = {
                Button(onClick = {
                    chat.value?.also {
                        viewModel.joinChat(it.id)
                    }
                    isJoinGroup = false
                }) { Text("Yes") }
            },
            contentPadding =
            PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
        ) {
            Text(
                text = "Are you sure?",
                textAlign = TextAlign.Center
            )
        }
    } else {
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

                    item {
                        when (group.status) {
                            is TdApi.ChatMemberStatusMember, is TdApi.ChatMemberStatusAdministrator, is TdApi.ChatMemberStatusCreator, is TdApi.ChatMemberStatusRestricted -> {
                                MenuItem(
                                    title = "Joined",
                                    imageVector = Icons.Outlined.Check,
                                    onClick = {
                                        isLeftGroup = true
                                    },
                                    backgroundColor = MaterialTheme.colors.primary,
                                )
                            }
                            is TdApi.ChatMemberStatusLeft -> {
                                MenuItem(
                                    title = "Join",
                                    imageVector = Icons.Outlined.Login,
                                    onClick = {
                                        isJoinGroup = true
                                    }
                                )
                            }
                            is TdApi.ChatMemberStatusBanned -> {
                                MenuItem(
                                    title = "Banned",
                                    imageVector = Icons.Outlined.Close,
                                    onClick = {},
                                    backgroundColor = MaterialTheme.colors.secondary,
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(5.dp))
                    }

                    item {
                        Text("Description")
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
                    item {
                        Text("Chat Options")
                    }
                    chat.value?.also { chatValue ->
                        item {
                            MenuItem(
                                title = "Delete",
                                imageVector = Icons.Outlined.Delete,
                                onClick = { viewModel.deleteChat(chatValue.id) }
                            )
                        }
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
    Text(group.description, style = MaterialTheme.typography.caption2)
}

@Composable
fun GroupName(chat: TdApi.Chat) {
    Text(
        chat.title,
        style = MaterialTheme.typography.title2,
        modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
    )
}



