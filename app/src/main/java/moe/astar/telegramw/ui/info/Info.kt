package moe.astar.telegramw.ui.info

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import moe.astar.telegramw.Screen
import org.drinkless.tdlib.TdApi

@Composable
fun InfoScreen(
    type: String,
    id: Long,
    viewModel: InfoViewModel,
    navController: NavController,
    username: String? = null
) {
    Log.d("InfoScreen", type)
    when (type) {
        "user" -> UserInfoScreen(id, viewModel, navController)
        "group" -> GroupInfoScreen(id, viewModel, navController)
        "channel" -> ChannelInfoScreen(id, viewModel, navController)
        "search" -> {
            username?.also {
                val info by remember { viewModel.searchPublicGroup(it) }.collectAsState(initial = null)
                info?.also {
                    when (val ty = it.type) {
                        is TdApi.ChatTypeSupergroup -> {
                            navController.popBackStack()
                            navController.navigate(
                                Screen.Info.buildRoute(
                                    "channel",
                                    ty.supergroupId
                                )
                            )
                        }
                        is TdApi.ChatTypePrivate -> {
                            navController.popBackStack()
                            navController.navigate(Screen.Info.buildRoute("user", ty.userId))
                        }
                        is TdApi.ChatTypeBasicGroup -> {
                            navController.popBackStack()
                            navController.navigate(Screen.Info.buildRoute("group", ty.basicGroupId))
                        }
                    }
                } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        else -> {
            navController.popBackStack()
        }
    }
}

@Composable
fun NotificationToggle(state: Boolean, checkedChange: (Boolean) -> Unit) {
    var checked by remember { mutableStateOf(true) }
    checked = state
    ToggleChip(
        colors = ToggleChipDefaults.toggleChipColors(
            uncheckedToggleControlColor = ToggleChipDefaults.SwitchUncheckedIconColor
        ),
        toggleControl = {
            Switch(
                checked = checked,
                enabled = true,
                modifier = Modifier.semantics {
                    this.contentDescription =
                        if (checked) "On" else "Off"
                }
            )
        },
        modifier = Modifier.fillMaxWidth(),
        checked = checked,
        enabled = true,
        onCheckedChange = {
            checkedChange(it)
            checked = it
        },
        label = {
            Text(
                text = "Notification",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
    )
}

@Composable
fun InfoImage(
    photo: TdApi.File,
    thumbnail: TdApi.Minithumbnail?,
    viewModel: InfoViewModel,
    imageSize: Dp = 120.dp
) {

    val thumbnailBitmap = remember {
        thumbnail?.let {
            val data = thumbnail.data
            val aspectRatio = thumbnail.width.toFloat() / thumbnail.height.toFloat()
            val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
            Bitmap.createScaledBitmap(bmp, 400, (400 / aspectRatio).toInt(), true).asImageBitmap()
        }
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
    ) {

        val imageModifier = Modifier
            .clip(CircleShape)
            .align(Alignment.Center)
            .size(imageSize)

        viewModel.fetchPhoto(photo).collectAsState(null).value?.also {
            Image(it, null, modifier = imageModifier)
        } ?: run {
            thumbnailBitmap?.also {
                Image(it, null, modifier = imageModifier)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

}

@Composable
fun PlaceholderInfoImage(painter: Painter, imageSize: Dp = 120.dp) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color = Color(0xFF888888))
    ) {

        val imageModifier =
            Image(
                painter, null, modifier = Modifier
                    .clip(CircleShape)
                    .align(Alignment.Center)
                    .size(imageSize)
            )

    }

}



