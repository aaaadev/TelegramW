package moe.astar.telegramw.ui.stickers

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import moe.astar.telegramw.R
import moe.astar.telegramw.ui.chat.STICKERS_PER_ROW
import moe.astar.telegramw.ui.chat.gzipFileToString
import org.drinkless.tdlib.TdApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectStickersScreen(
    navController: NavController,
    chatId: Long,
    messageId: Long,
    viewModel: SelectStickersViewModel
) {
    val stickers = viewModel.getInstalledStickerSets(TdApi.StickerTypeRegular()).collectAsState(
        initial = null
    )
    stickers.value?.also { stickerSets ->
        Scaffold(
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        ) {
            val maxPages = stickerSets.totalCount
            var finalValue by remember { mutableStateOf(0) }
            var state = rememberPagerState(0)

            val animatedSelectedPage by animateFloatAsState(
                targetValue = state.currentPage.toFloat(),
            ) {
                finalValue = it.toInt()
            }

            val pageIndicatorState: PageIndicatorState = remember {
                object : PageIndicatorState {
                    override val pageOffset: Float
                        get() = animatedSelectedPage - finalValue
                    override val selectedPage: Int
                        get() = finalValue
                    override val pageCount: Int
                        get() = maxPages
                }
            }
            val shape = if (LocalConfiguration.current.isScreenRound) CircleShape else null
            HorizontalPager(
                pageCount = maxPages,
                state = state,
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize().run {
                        if (shape != null) {
                            clip(shape)
                        } else {
                            this
                        }
                    }
                ) {
                    StickersView(
                        chatId,
                        messageId,
                        stickerSets.sets[page].id,
                        viewModel,
                        navController
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            ) {
                HorizontalPageIndicator(
                    pageIndicatorState = pageIndicatorState,
                )
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun StickersView(
    chatId: Long,
    messageId: Long,
    setId: Long,
    viewModel: SelectStickersViewModel,
    navController: NavController
) {
    val stickers = viewModel.getStickerSet(setId).collectAsState(initial = null)
    stickers.value?.also {
        ScalingLazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                Button(
                    modifier = Modifier.padding(bottom = 8.dp),
                    onClick = {
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primaryVariant,
                        contentColor = MaterialTheme.colors.onSurface
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.outline_close_24),
                        contentDescription = null,
                    )
                }
            }
            item {
                Text(it.title, textAlign = TextAlign.Center)
            }
            if (it.stickers.isNotEmpty()) {
                for (i in 0..it.stickers.size / STICKERS_PER_ROW) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (j in 0..if (i == (it.stickers.size / STICKERS_PER_ROW)) {
                                ((it.stickers.size - 1) % STICKERS_PER_ROW)
                            } else {
                                STICKERS_PER_ROW - 1
                            }) {
                                val sticker = it.stickers[i * STICKERS_PER_ROW + j]
                                val stickerFile by remember { viewModel.fetchFile(sticker.sticker) }.collectAsState(
                                    initial = null
                                )
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                ) {
                                    stickerFile?.also {
                                        when (sticker.format) {
                                            is TdApi.StickerFormatWebp -> {
                                                val painter = rememberAsyncImagePainter(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data("file://$it")
                                                        .size(Size.ORIGINAL) // Set the target size to load the image at.
                                                        .build()
                                                )
                                                val state = painter.state
                                                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                                                    CircularProgressIndicator()
                                                } else {
                                                    Image(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                Log.d("tt", "$chatId $messageId")
                                                                viewModel.sendSticker(
                                                                    chatId,
                                                                    messageId,
                                                                    sticker
                                                                )
                                                                navController.popBackStack()
                                                            },
                                                        painter = painter,
                                                        contentDescription = sticker.emoji
                                                    )
                                                }
                                            }
                                            is TdApi.StickerFormatTgs -> {
                                                gzipFileToString(it)?.also { json ->
                                                    val composition by rememberLottieComposition(
                                                        LottieCompositionSpec.JsonString(
                                                            json
                                                        )
                                                    )
                                                    LottieAnimation(
                                                        iterations = LottieConstants.IterateForever,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(50.dp),
                                                        composition = composition,
                                                    )
                                                } ?: CircularProgressIndicator()
                                            }
                                            is TdApi.StickerFormatWebm -> {
                                                val painter = rememberAsyncImagePainter(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .decoderFactory(VideoFrameDecoder.Factory())
                                                        .data("file://$it")
                                                        .size(Size.ORIGINAL) // Set the target size to load the image at.
                                                        .build()
                                                )
                                                val state = painter.state
                                                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                                                    CircularProgressIndicator()
                                                } else {
                                                    Image(
                                                        painter = painter,
                                                        contentDescription = sticker.emoji,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    } ?: Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
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