package moe.astar.telegramw.ui.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
    var isLeftGroup by remember { mutableStateOf(false) }
    var isJoinGroup by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

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

                    group.usernames?.activeUsernames?.also { arr ->
                        if (arr.isNotEmpty()) {
                            item {
                                Text("Link")
                            }

                            items(arr) { item ->
                                val url = "t.me/$item"
                                val annotatedString = buildAnnotatedString {
                                    append(url)
                                    addStyle(
                                        style = SpanStyle(
                                            color = Color(0xFF64B5F6),
                                            textDecoration = TextDecoration.Underline
                                        ),
                                        start = 0,
                                        end = this.length
                                    )

                                    addStringAnnotation(
                                        tag = "url",
                                        annotation = "https://$url",
                                        start = 0,
                                        end = this.length
                                    )
                                }
                                ClickableText(text = annotatedString, onClick = {
                                    annotatedString.getStringAnnotations("url", it, it)
                                        .firstOrNull()
                                        ?.let { stringAnnotation ->
                                            uriHandler.openUri(stringAnnotation.item)
                                        }
                                })
                            }
                        }
                    }

                    item {
                        Text("Description")
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
    Text(info.description, style = MaterialTheme.typography.caption2)
}