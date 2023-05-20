package moe.astar.telegramw.ui.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import org.drinkless.tdlib.TdApi

@Composable
fun EmojiImage(
    photo: TdApi.File,
    viewModel: MessageMenuViewModel,
    imageSize: Dp = 30.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
    ) {
        viewModel.fetchPhoto(photo).collectAsState(null).value?.also {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file://$it")
                    .size(Size.ORIGINAL) // Set the target size to load the image at.
                    .build()
            )
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Image(
                    modifier = Modifier
                        .clickable { onClick() }
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .size(imageSize),
                    painter = painter,
                    contentDescription = null,
                )
            }
        } ?: run {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun EmojiImage(
    photo: TdApi.File,
    viewModel: SelectReactionViewModel,
    imageSize: Dp = 30.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
    ) {
        viewModel.fetchPhoto(photo).collectAsState(null).value?.also {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file://$it")
                    .size(Size.ORIGINAL) // Set the target size to load the image at.
                    .build()
            )
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Image(
                    modifier = Modifier
                        .clickable { onClick() }
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .size(imageSize),
                    painter = painter,
                    contentDescription = null,
                )
            }
        } ?: run {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun SelectReactionScreen(
    navController: NavController,
    chatId: Long,
    messageId: Long,
    viewModel: SelectReactionViewModel
) {
    val reactions =
        viewModel.getAvailableReactions(chatId, messageId).collectAsState(initial = null)
    reactions.value?.also {
        val topReactions = it.topReactions
        Scaffold(
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        ) {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    Text("Select emojis")
                }
                if (topReactions.isNotEmpty()) {
                    for (i in 0..(topReactions.size-1) / REACTIONS_PER_ROW) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (j in 0..if (i != (topReactions.size / REACTIONS_PER_ROW)) { REACTIONS_PER_ROW-1 } else { ((topReactions.size - 1) % REACTIONS_PER_ROW) }) {
                                        when (val reactionType =
                                            topReactions[i * REACTIONS_PER_ROW + j].type) {
                                            is TdApi.ReactionTypeEmoji -> {
                                                val emoji =
                                                    viewModel.getAnimatedEmoji(reactionType.emoji)
                                                        .collectAsState(
                                                            initial = null
                                                        )
                                                emoji.value?.also { emojiValue ->
                                                    emojiValue.sticker?.also {
                                                        EmojiImage(
                                                            photo = it.thumbnail!!.file,
                                                            viewModel = viewModel,
                                                            onClick = {
                                                                viewModel.addMessageReaction(
                                                                    chatId,
                                                                    messageId,
                                                                    reactionType
                                                                )
                                                                navController.popBackStack()
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                            is TdApi.ReactionTypeCustomEmoji -> {
                                                val emoji =
                                                    viewModel.getCustomEmoji(listOf(reactionType.customEmojiId))
                                                        .collectAsState(
                                                            initial = null
                                                        )
                                                emoji.value?.also { emojiValue ->
                                                    emojiValue.stickers?.also { stickers ->
                                                        stickers[0]?.also {
                                                            EmojiImage(
                                                                photo = it.thumbnail!!.file,
                                                                viewModel = viewModel,
                                                                onClick = {
                                                                    viewModel.addMessageReaction(
                                                                        chatId,
                                                                        messageId,
                                                                        reactionType
                                                                    )
                                                                    navController.popBackStack()
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
}