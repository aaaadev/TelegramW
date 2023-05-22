package moe.astar.telegramw.ui.chat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
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
import moe.astar.telegramw.Screen
import moe.astar.telegramw.ui.util.*
import okio.buffer
import okio.source
import org.drinkless.tdlib.TdApi
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.text.DateFormat
import java.util.zip.GZIPInputStream


@Composable
fun MessageContent(
    message: TdApi.Message,
    viewModel: ChatViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    showSender: Long?,
) {
    val onClick =
        { navController.navigate(Screen.MessageMenu.buildRoute(message.chatId, message.id)) }
    when (val content = message.content) {
        is TdApi.MessageText -> TextMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessagePhoto -> PhotoMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender
        )
        is TdApi.MessageAudio -> AudioMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender
        )
        is TdApi.MessageVoiceNote -> VoiceNoteMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender
        )
        is TdApi.MessageVideo -> VideoMessage(
            message,
            content,
            viewModel,
            navController,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageVideoNote -> VideoNoteMessage(
            message,
            content,
            viewModel,
            navController,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageSticker -> StickerMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageDocument -> DocumentMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageLocation -> LocationMessage(
            message,
            content,
            viewModel,
            navController,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageAnimatedEmoji -> AnimatedEmojiMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageAnimation -> AnimationMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageCall -> CallMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessagePoll -> PollMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageContact -> ContactMessage(
            message,
            content,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
        is TdApi.MessageChatAddMembers -> ChatAddMembersMessage(
            message,
            content,
            viewModel,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
        )
        is TdApi.MessageChatDeleteMember -> ChatDeleteMemberMessage(
            message,
            content,
            viewModel,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
        )
        is TdApi.MessageChatJoinByLink -> ChatJoinByLinkMessage(
            message,
            content,
            viewModel,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
        )
        is TdApi.MessageChatJoinByRequest -> ChatJoinByRequestMessage(
            message,
            content,
            viewModel,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
        )
        is TdApi.MessageChatChangeTitle -> ChatChangeTitleMessage(
            message,
            content,
            viewModel,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
        )
        is TdApi.MessageChatDeletePhoto -> ChatDeletePhotoMessage(
            message,
            content,
            viewModel,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
        )
        is TdApi.MessageChatChangePhoto -> ChatChangePhotoMessage(
            message,
            content,
            viewModel,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
        )
        is TdApi.MessagePinMessage -> PinMessage(
            message,
            content,
            viewModel,
            modifier,
            onClick = onClick,
            scrollReply = scrollReply,
        )
        else -> UnsupportedMessage(
            message,
            viewModel,
            modifier,
            navController,
            onClick = onClick,
            scrollReply = scrollReply,
            showSender = showSender,
        )
    }
}

@Composable
fun MessageCard(
    message: TdApi.Message,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    contentPadding: PaddingValues = CardDefaults.ContentPadding,
    onClick: () -> Unit = {},
    scrollReply: (Long) -> Unit,
    showSender: Long?,
    content: @Composable (ColumnScope.() -> Unit),
) {
    showSender?.also { sender ->
        Box(modifier = Modifier.fillMaxWidth()) {
            Sender(
                sender,
                viewModel,
                navController,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable {}
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                //navController.navigate(Screen.MessageMenu.buildRoute(chat.id, message.id))
            },
        contentAlignment = if (message.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart,

        ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Card(
                onClick = onClick,
                contentPadding = contentPadding,
                backgroundPainter = ColorPainter(
                    if (message.isOutgoing) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.surface
                ),
            ) {
                if (message.replyToMessageId != 0L) {
                    val messages by viewModel.messageProvider.messageData.collectAsState()
                    val chats by viewModel.chatProvider.chatData.collectAsState()
                    messages[message.replyToMessageId]?.also { reply ->
                        chats[message.chatId]?.also { chat ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .padding(start = 3.dp, bottom = 7.dp)
                                    .clickable {
                                        scrollReply(message.replyToMessageId)
                                    }
                            ) {
                                ShortDescription(
                                    message = reply,
                                    chat = chat,
                                    viewModel = viewModel,
                                    client = viewModel.client,
                                )
                            }
                        }
                    }
                }
                message.forwardInfo?.also {
                    ShortText("Forwarded message")
                }
                content()
            }
        }
    }
}

@Composable
fun EmojiImage(
    photo: TdApi.File,
    viewModel: ChatViewModel,
    imageSize: Dp = 30.dp,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
    ) {
        viewModel.fetchPhotoFile(photo).collectAsState(null).value?.also {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageInfo(message: TdApi.Message, viewModel: ChatViewModel) {
    val chat = viewModel.chatFlow.collectAsState()
    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        message.interactionInfo?.also { interactionInfo ->
            val reactions = interactionInfo.reactions
            reactions.forEach { reaction ->
                Box(
                    modifier = Modifier
                        .background(
                            if (!reaction.isChosen) {
                                Color(0x44000000)
                            } else {
                                Color(0x44CCCCCC)
                            },
                            RoundedCornerShape(10.dp)
                        )
                        .padding(3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (reaction.isChosen) {
                                viewModel.removeMessageReaction(
                                    message.chatId,
                                    message.id,
                                    reaction.type,
                                )
                            } else {
                                viewModel.addMessageReaction(
                                    message.chatId,
                                    message.id,
                                    reaction.type,
                                )
                            }
                        }
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        when (val reactionType =
                            reaction.type) {
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
                                            imageSize = 17.dp,
                                        )
                                        if (reaction.totalCount > 1) {
                                            Text(
                                                reaction.totalCount.toString(),
                                            )
                                        }
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
                                                imageSize = 17.dp,
                                            )
                                            if (reaction.totalCount > 1) {
                                                Text(
                                                    reaction.totalCount.toString(),
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        if (message.editDate > message.date)
            Text(
                "edited",
                style = MaterialTheme.typography.caption1,
            )
        Time(message.date)
        if (message.isOutgoing) {
            MessageStatusIcon(
                message,
                chat.value,
                modifier = Modifier
                    .size(20.dp)
            )
        }
    }
}

@Composable
fun Time(timestamp: Int) {
    Text(
        text = DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp.toLong() * 1000),
        modifier = Modifier.padding(0.dp),
        style = MaterialTheme.typography.caption1
    )
}

@Composable
fun FormattedText(
    text: TdApi.FormattedText,
    navController: NavController,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.body2,
    length: Int? = null
) {
    val formattedString = buildAnnotatedString {
        pushStyle(SpanStyle(color = MaterialTheme.colors.onSurfaceVariant))
        length?.also {
            if (text.text.length > it) {
                append(text.text.dropLast(text.text.length - it))
            } else {
                append(text.text)
            }
        } ?: append(text.text)
        for (entity in text.entities) {
            when (val entityType = entity.type) {
                is TdApi.TextEntityTypeBold -> {
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        entity.offset,
                        entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeItalic -> {
                    addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic),
                        entity.offset,
                        entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeCode -> {
                    addStyle(
                        SpanStyle(fontFamily = FontFamily.Monospace),
                        entity.offset,
                        entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeUnderline -> {
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                        entity.offset,
                        entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeStrikethrough -> {
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                        entity.offset,
                        entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeMention -> {
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xff64B5F6), textDecoration = TextDecoration.Underline
                        ), start = entity.offset, end = entity.offset + entity.length
                    )
                    var username = text.text.drop(entity.offset).take(entity.length)
                    addStringAnnotation(
                        tag = "mention",
                        annotation = username,
                        start = entity.offset,
                        end = entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeMentionName -> {
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xff64B5F6), textDecoration = TextDecoration.Underline
                        ), start = entity.offset, end = entity.offset + entity.length
                    )
                    addStringAnnotation(
                        tag = "mentionName",
                        annotation = entityType.userId.toString(),
                        start = entity.offset,
                        end = entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeTextUrl -> {
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xff64B5F6), textDecoration = TextDecoration.Underline
                        ), start = entity.offset, end = entity.offset + entity.length
                    )
                    var url = Uri.parse(entityType.url)
                    if (url.scheme.isNullOrEmpty()) {
                        url = Uri.parse("http://" + entityType.url)
                    }

                    addStringAnnotation(
                        tag = "url",
                        annotation = url.toString(),
                        start = entity.offset,
                        end = entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeUrl -> {
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xFF64B5F6), textDecoration = TextDecoration.Underline
                        ), start = entity.offset, end = entity.offset + entity.length
                    )

                    var url = Uri.parse(text.text.drop(entity.offset).take(entity.length))
                    if (url.scheme.isNullOrEmpty()) {
                        url =
                            Uri.parse("http://" + text.text.drop(entity.offset).take(entity.length))
                    }


                    addStringAnnotation(
                        tag = "url",
                        annotation = url.toString(),
                        start = entity.offset,
                        end = entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeEmailAddress -> {
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xFF64B5F6), textDecoration = TextDecoration.Underline
                        ), start = entity.offset, end = entity.offset + entity.length
                    )
                    addStringAnnotation(
                        tag = "email",
                        annotation = text.text.drop(entity.offset).take(entity.length),
                        start = entity.offset,
                        end = entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypePhoneNumber -> {
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xFF64B5F6), textDecoration = TextDecoration.Underline
                        ), start = entity.offset, end = entity.offset + entity.length
                    )
                    addStringAnnotation(
                        tag = "phone",
                        annotation = text.text.drop(entity.offset).take(entity.length),
                        start = entity.offset,
                        end = entity.offset + entity.length
                    )
                }
                is TdApi.TextEntityTypeBankCardNumber -> {}
                is TdApi.TextEntityTypeBotCommand -> {}
                is TdApi.TextEntityTypeCashtag -> {}
                is TdApi.TextEntityTypeHashtag -> {}
                is TdApi.TextEntityTypeMediaTimestamp -> {}
                is TdApi.TextEntityTypePre -> {}
                is TdApi.TextEntityTypePreCode -> {}
            }
        }
        length?.also {
            if (it < text.text.length) {
                append("...")
            }
        }
    }

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    ClickableText(
        text = formattedString, onClick = {
            formattedString.getStringAnnotations("url", it, it).firstOrNull()
                ?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }

            formattedString.getStringAnnotations("email", it, it).firstOrNull()
                ?.let { stringAnnotation ->
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, stringAnnotation.item)
                    }
                    emailIntent.resolveActivity(context.packageManager)?.also {
                        startActivity(context, emailIntent, null)
                    }
                }

            formattedString.getStringAnnotations("phone", it, it).firstOrNull()
                ?.let { stringAnnotation ->
                    val phoneIntent = Intent(
                        Intent.ACTION_DIAL, Uri.fromParts("tel", stringAnnotation.item, null)
                    )
                    phoneIntent.resolveActivity(context.packageManager)?.also {
                        startActivity(context, phoneIntent, null)
                    }
                }

            formattedString.getStringAnnotations("mention", it, it).firstOrNull()
                ?.let { stringAnnotation ->
                    navController.navigate(
                        Screen.Info.buildRoute(
                            "search",
                            0,
                            stringAnnotation.item
                        )
                    )
                }

            formattedString.getStringAnnotations("mentionName", it, it).firstOrNull()
                ?.let { stringAnnotation ->
                    navController.navigate(
                        Screen.Info.buildRoute(
                            "user",
                            stringAnnotation.item.toLong()
                        )
                    )
                }
        }, style = style, modifier = modifier
    )
}

@Composable
fun TextMessage(
    message: TdApi.Message,
    content: TdApi.MessageText,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            FormattedText(content.text, navController = navController)
            content.webPage?.also {
                WebpagePreview(webPage = it, viewModel = viewModel, navController = navController)
            }
            MessageInfo(message, viewModel)
        }
    }
}

const val MAX_PREVIEW_DESCRIPTION = 100

@Composable
fun WebpagePreview(navController: NavController, webPage: TdApi.WebPage, viewModel: ChatViewModel) {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp, bottom = 7.dp)
    )
    Column() {
        Text(
            text = webPage.siteName,
            style = MaterialTheme.typography.title3.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = webPage.title,
            style = MaterialTheme.typography.title3.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
        if (webPage.description.text.isNotEmpty()) {
            Spacer(modifier = Modifier.height(5.dp))
            FormattedText(
                modifier = Modifier.padding(start = 3.dp),
                text = webPage.description,
                style = MaterialTheme.typography.caption3.copy(color = Color(0xFF888888)),
                length = MAX_PREVIEW_DESCRIPTION,
                navController = navController,
            )
        }
        webPage.photo?.also { photo ->
            Spacer(modifier = Modifier.height(5.dp))
            val image = remember { viewModel.fetchPhoto(photo) }.collectAsState(initial = null)
            image.value?.also {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file://$it")
                        .size(Size.ORIGINAL) // Set the target size to load the image at.
                        .build()
                )
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    Box {
                        photo.minithumbnail?.also {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it.data)
                                    .size(Size.ORIGINAL) // Set the target size to load the image at.
                                    .build()
                            )
                            val state = painter.state
                            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                            } else {
                                Image(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    contentScale = ContentScale.FillWidth,
                                    painter = painter,
                                    contentDescription = "photo",
                                )
                            }
                        }
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentScale = ContentScale.FillWidth,
                        painter = painter,
                        contentDescription = "photo",
                    )
                }
            } ?: Box {
                photo.minithumbnail?.also {
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(it.data)
                            .size(Size.ORIGINAL) // Set the target size to load the image at.
                            .build()
                    )
                    val state = painter.state
                    if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    } else {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentScale = ContentScale.FillWidth,
                            painter = painter,
                            contentDescription = "photo",
                        )
                    }
                }
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun PhotoMessage(
    message: TdApi.Message,
    content: TdApi.MessagePhoto,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    val thumbnail = remember {
        content.photo.minithumbnail?.let { thumbnail ->
            val data = thumbnail.data
            val aspectRatio = thumbnail.width.toFloat() / thumbnail.height.toFloat()
            val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
            Bitmap.createScaledBitmap(bmp, 400, (400 / aspectRatio).toInt(), true).asImageBitmap()
        }
    }
    val image = remember { viewModel.fetchPhoto(content.photo) }.collectAsState(initial = null)

    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender,
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
            image.value?.also {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file://$it")
                        .size(Size.ORIGINAL) // Set the target size to load the image at.
                        .build()
                )
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    Box {
                        thumbnail?.also { tn ->
                            Image(bitmap = tn, contentDescription = null)
                        }
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    Box {
                        Image(
                            painter = painter,
                            contentDescription = "photo",
                        )
                    }
                }
            } ?: Box {
                thumbnail?.also { tn ->
                    Image(bitmap = tn, contentDescription = null)
                }
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            content.caption.text.takeIf { it.isNotEmpty() }?.let {
                FormattedText(
                    text = content.caption,
                    modifier = modifier.padding(CardDefaults.ContentPadding),
                    navController = navController,
                )
            }
        }
    }
}

@Composable
fun AudioMessage(
    message: TdApi.Message,
    content: TdApi.MessageAudio,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    // TODO: think about audio playback lifecycle
    val player =
        remember { viewModel.fetchAudio(content.audio.audio) }.collectAsState(initial = null)

    val isPlaying = remember { mutableStateOf(false) }
    val position = remember { mutableStateOf(0f) }


    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CompactButton(
                onClick = {
                    player.value?.also { p ->

                        if (isPlaying.value) {
                            p.pause()
                        } else {
                            p.start()
                            object : CountDownTimer((p.duration - p.currentPosition).toLong(), 50) {
                                override fun onTick(p0: Long) {
                                    if (isPlaying.value) {
                                        position.value = p.currentPosition.toFloat() / p.duration
                                    }
                                }

                                override fun onFinish() {}
                            }.start()

                            p.setOnCompletionListener {
                                position.value = 0f
                                isPlaying.value = false
                            }
                        }

                        isPlaying.value = !isPlaying.value
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            ) {
                Icon(
                    painter = painterResource(
                        id =
                        if (isPlaying.value) R.drawable.baseline_pause_circle_24 else R.drawable.baseline_play_circle_24
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface,
                    modifier = Modifier.fillMaxSize()
                )

            }
            CircularProgressIndicator(progress = position.value)
        }

        content.caption.text.takeIf { it.isNotEmpty() }?.let {
            Text(text = it, style = MaterialTheme.typography.body2)
        }

        MessageInfo(message, viewModel)

    }

}

@Composable
fun VoiceNoteMessage(
    message: TdApi.Message,
    content: TdApi.MessageVoiceNote,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    // TODO: think about audio playback lifecycle
    val player =
        remember { viewModel.fetchAudio(content.voiceNote.voice) }.collectAsState(initial = null)

    val isPlaying = remember { mutableStateOf(false) }
    val position = remember { mutableStateOf(0f) }


    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CompactButton(
                onClick = {
                    player.value?.also { p ->

                        if (isPlaying.value) {
                            p.pause()
                        } else {
                            p.start()
                            object : CountDownTimer((p.duration - p.currentPosition).toLong(), 50) {
                                override fun onTick(p0: Long) {
                                    if (isPlaying.value) {
                                        position.value = p.currentPosition.toFloat() / p.duration
                                    }
                                }

                                override fun onFinish() {}
                            }.start()

                            p.setOnCompletionListener {
                                position.value = 0f
                                isPlaying.value = false
                            }
                        }

                        isPlaying.value = !isPlaying.value
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            ) {
                Icon(
                    painter = painterResource(
                        id =
                        if (isPlaying.value) R.drawable.baseline_pause_circle_24 else R.drawable.baseline_play_circle_24
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface,
                    modifier = Modifier.fillMaxSize()
                )

            }
            CircularProgressIndicator(progress = position.value)
        }

        content.caption.text.takeIf { it.isNotEmpty() }?.let {
            Text(text = it, style = MaterialTheme.typography.body2)
        }

        MessageInfo(message, viewModel)

    }

}

@Composable
fun VideoMessage(
    message: TdApi.Message,
    content: TdApi.MessageVideo,
    viewModel: ChatViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    val path =
        remember(content) { viewModel.fetchFile(content.video.video) }.collectAsState(initial = null)

    val frame = remember(path) {
        path.value?.let {
            MediaMetadataRetriever().apply {
                setDataSource(it)
            }.getFrameAtIndex(0)?.asImageBitmap()
        }
    }

    val thumbnail = remember {
        content.video.minithumbnail?.let { thumbnail ->
            val data = thumbnail.data
            val aspectRatio = thumbnail.width.toFloat() / thumbnail.height.toFloat()
            val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
            Bitmap.createScaledBitmap(bmp, 400, (400 / aspectRatio).toInt(), true).asImageBitmap()
        }
    }

    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top
        ) {

            path.value?.also {

                MediaMetadataRetriever().apply {
                    setDataSource(it)
                }.getFrameAtIndex(0)?.asImageBitmap()?.also { frame ->
                    Box {
                        Image(
                            frame,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        CompactButton(
                            onClick = {
                                navController.navigate(Screen.Video.buildRoute(it))
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(
                                    0x7E000000
                                )
                            ), modifier = Modifier.align(Alignment.Center)

                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }


                }
            } ?: run {
                Box {

                    thumbnail?.also { tn ->
                        Image(bitmap = tn, contentDescription = null)
                    }

                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(4.dp)
                    )
                }

            }

            content.caption.text.takeIf { it.isNotEmpty() }?.let {
                Text(
                    text = it, style = MaterialTheme.typography.body2,
                    modifier = modifier.padding(CardDefaults.ContentPadding),
                )
            }
        }
    }
}

@Composable
fun VideoNoteMessage(
    message: TdApi.Message,
    content: TdApi.MessageVideoNote,
    viewModel: ChatViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    val path =
        remember(content) { viewModel.fetchFile(content.videoNote.video) }.collectAsState(initial = null)


    val thumbnail = remember {
        content.videoNote.minithumbnail?.let { thumbnail ->
            val data = thumbnail.data
            val aspectRatio = thumbnail.width.toFloat() / thumbnail.height.toFloat()
            val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
            Bitmap.createScaledBitmap(bmp, 400, (400 / aspectRatio).toInt(), true).asImageBitmap()
        }
    }

    MessageCard(
        message,
        onClick = onClick,
        navController = navController,
        contentPadding = PaddingValues(0.dp),
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top
        ) {

            path.value?.also {

                MediaMetadataRetriever().apply {
                    setDataSource(it)
                }.getFrameAtIndex(0)?.asImageBitmap()?.also { frame ->
                    Box {
                        Image(
                            frame,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        CompactButton(
                            onClick = {
                                navController.navigate(Screen.Video.buildRoute(it))
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(
                                    0x7E000000
                                )
                            ), modifier = Modifier.align(Alignment.Center)

                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }


                }
            } ?: run {
                Box {

                    thumbnail?.also { tn ->
                        Image(bitmap = tn, contentDescription = null)
                    }

                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(4.dp)
                    )
                }

            }
        }
    }
}

@JvmField
val UTF_8: Charset =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) java.nio.charset.StandardCharsets.UTF_8 else Charset.forName(
        "UTF-8"
    )

fun gzipFileToString(path: String?): String? {
    try {
        GZIPInputStream(FileInputStream(File(path))).source().buffer().use { buffer ->
            return buffer.readString(
                UTF_8
            )
        }
    } catch (t: Throwable) {
        Log.d("MessageContent", "Cannot decode GZip, path: $path")
        return null
    }
}


@Composable
fun StickerMessage(
    message: TdApi.Message,
    content: TdApi.MessageSticker,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    val path =
        remember { viewModel.fetchFile(content.sticker.sticker) }.collectAsState(initial = null)

    path.value?.also {
        when (content.sticker.format) {
            is TdApi.StickerFormatWebp -> {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file://$it")
                        .size(Size.ORIGINAL) // Set the target size to load the image at.
                        .build()
                )
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    MessageCard(
                        message,
                        navController = navController,
                        onClick = onClick,
                        viewModel = viewModel,
                        scrollReply = scrollReply,
                        showSender = showSender,
                    ) {
                        Text(content.sticker.emoji + " Sticker")
                    }
                } else {
                    MessageCard(
                        message,
                        navController = navController,
                        onClick = onClick,
                        viewModel = viewModel,
                        scrollReply = scrollReply,
                        showSender = showSender,
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = content.sticker.emoji + " Sticker",
                            modifier = Modifier.width(130.dp)
                        )
                    }
                }
            }
            is TdApi.StickerFormatTgs -> {
                gzipFileToString(it)?.also { json ->
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.JsonString(
                            json
                        )
                    )
                    MessageCard(
                        message,
                        navController = navController,
                        onClick = onClick,
                        viewModel = viewModel,
                        scrollReply = scrollReply,
                        showSender = showSender,
                    ) {
                        LottieAnimation(
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier
                                .width(130.dp)
                                .height(130.dp),
                            composition = composition,
                        )
                    }
                } ?: MessageCard(
                    message,
                    navController = navController,
                    onClick = onClick,
                    viewModel = viewModel,
                    scrollReply = scrollReply,
                    showSender = showSender,
                ) {
                    Text(content.sticker.emoji + " Sticker")
                }
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
                    MessageCard(
                        message,
                        navController = navController,
                        onClick = onClick,
                        viewModel = viewModel,
                        scrollReply = scrollReply,
                        showSender = showSender,
                    ) {
                        Text(content.sticker.emoji + " Sticker")
                    }
                } else {
                    MessageCard(
                        message,
                        navController = navController,
                        onClick = onClick,
                        viewModel = viewModel,
                        scrollReply = scrollReply,
                        showSender = showSender,
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = content.sticker.emoji + " Sticker",
                            modifier = Modifier.width(130.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentMessage(
    message: TdApi.Message,
    content: TdApi.MessageDocument,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        Text("file: " + content.document.fileName)
    }
}

@Composable
fun LocationMessage(
    message: TdApi.Message,
    content: TdApi.MessageLocation,
    viewModel: ChatViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {

    val context = LocalContext.current
    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender,
    ) {

        MapView(
            onLoad = {
                val position = GeoPoint(content.location.latitude, content.location.longitude)
                val marker = Marker(it)
                marker.position = position
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.icon = ContextCompat.getDrawable(context, R.drawable.baseline_location_on_24)
                it.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                it.overlays.add(marker)
                it.setOnClickListener {
                    Log.d(
                        "LocationMessage",
                        "${content.location.latitude}, ${content.location.longitude}"
                    )
                    navController.navigate(
                        Screen.Map.buildRoute(
                            content.location.latitude,
                            content.location.longitude
                        )
                    )
                }
                it.setOnTouchListener { v, e ->
                    when (e.action) {
                        MotionEvent.ACTION_UP -> v.performClick()
                    }
                    true
                }
                it.controller.animateTo(position, 15.0, 0)
                it.invalidate()
            }, modifier = Modifier
                .defaultMinSize(minHeight = 120.dp)
                .fillMaxSize()
                .clickable { Log.d("MapView", "click") }
        )
    }
}

@Composable
fun AnimatedEmojiMessage(
    message: TdApi.Message,
    content: TdApi.MessageAnimatedEmoji,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        Text(content.emoji)
    }
}

@Composable
fun AnimationMessage(
    message: TdApi.Message,
    content: TdApi.MessageAnimation,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    val path =
        remember { viewModel.fetchFile(content.animation.animation) }.collectAsState(initial = null)

    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender,
    ) {

        path.value?.also {
            VideoView(videoUri = it, repeat = true)
        } ?: run { CircularProgressIndicator(modifier = modifier.padding(4.dp)) }
    }
}

@Composable
fun CallMessage(
    message: TdApi.Message,
    content: TdApi.MessageCall,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        Text("Call")
    }
}

@Composable
fun PollMessage(
    message: TdApi.Message,
    content: TdApi.MessagePoll,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        Text("Poll")
    }
}

@Composable
fun ContactMessage(
    message: TdApi.Message,
    content: TdApi.MessageContact,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {},
    showSender: Long?,
) {
    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        val name = content.contact.let { it.firstName + " " + it.lastName }
        val number = content.contact.phoneNumber
        Text("Contact:\n $name, $number")
    }
}

@Composable
fun ChatAddMembersMessage(
    message: TdApi.Message,
    content: TdApi.MessageChatAddMembers,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick }) {
        val userIds = content.memberUserIds
        val users = userIds.joinToString(",") { userId ->
            viewModel.getUser(userId)?.let { it.firstName + " " + it.lastName } ?: String()
        }
        val senderId = (message.senderId as? TdApi.MessageSenderUser)?.userId
        senderId?.also { senderIdValue ->
            viewModel.getUser(senderIdValue)?.also { sender ->
                if (userIds.size > 1) {
                    Text(
                        "$users were added by ${sender.firstName + " " + sender.lastName} to the group",
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .align(Alignment.Center),
                        style = MaterialTheme.typography.caption1.copy(textAlign = TextAlign.Center)
                    )
                } else {
                    Text(
                        "$users was added by ${sender.firstName + " " + sender.lastName} to the group",
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .align(Alignment.Center),
                        style = MaterialTheme.typography.caption1.copy(textAlign = TextAlign.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatDeleteMemberMessage(
    message: TdApi.Message,
    content: TdApi.MessageChatDeleteMember,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick }) {
        val userId = content.userId
        val senderId = (message.senderId as? TdApi.MessageSenderUser)?.userId
        senderId?.also { senderIdValue ->
            viewModel.getUser(senderIdValue)?.also { sender ->
                val user =
                    viewModel.getUser(userId)?.let { it.firstName + " " + it.lastName } ?: String()
                Text(
                    "$user was deleted by ${sender.firstName + " " + sender.lastName} to the group",
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.caption1.copy(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@Composable
fun ChatJoinByLinkMessage(
    message: TdApi.Message,
    content: TdApi.MessageChatJoinByLink,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick }) {
        val senderId = (message.senderId as? TdApi.MessageSenderUser)?.userId
        senderId?.also { senderIdValue ->
            viewModel.getUser(senderIdValue)?.also { sender ->
                Text(
                    "${sender.firstName + " " + sender.lastName} joined group via an invite link",
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.caption1.copy(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@Composable
fun ChatJoinByRequestMessage(
    message: TdApi.Message,
    content: TdApi.MessageChatJoinByRequest,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick }) {
        val senderId = (message.senderId as? TdApi.MessageSenderUser)?.userId
        senderId?.also { senderIdValue ->
            viewModel.getUser(senderIdValue)?.also { sender ->
                Text(
                    "${sender.firstName + " " + sender.lastName} joined chat by request",
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.caption1.copy(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@Composable
fun ChatChangeTitleMessage(
    message: TdApi.Message,
    content: TdApi.MessageChatChangeTitle,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick }) {
        val title = content.title
        val senderId = (message.senderId as? TdApi.MessageSenderUser)?.userId
        senderId?.also { senderIdValue ->
            viewModel.getUser(senderIdValue)?.also { sender ->
                Text(
                    "${sender.firstName + " " + sender.lastName} changed group's title to $title",
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.caption1.copy(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@Composable
fun ChatDeletePhotoMessage(
    message: TdApi.Message,
    content: TdApi.MessageChatDeletePhoto,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick }) {
        val senderId = (message.senderId as? TdApi.MessageSenderUser)?.userId
        senderId?.also { senderIdValue ->
            viewModel.getUser(senderIdValue)?.also { sender ->
                Text(
                    "${sender.firstName + " " + sender.lastName} deleted group's photo",
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.caption1.copy(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@Composable
fun ChatChangePhotoMessage(
    message: TdApi.Message,
    content: TdApi.MessageChatChangePhoto,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick }) {
        val senderId = (message.senderId as? TdApi.MessageSenderUser)?.userId
        senderId?.also { senderIdValue ->
            viewModel.getUser(senderIdValue)?.also { sender ->
                Text(
                    "${sender.firstName + " " + sender.lastName} changed group's photo",
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.caption1.copy(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@Composable
fun PinMessage(
    message: TdApi.Message,
    content: TdApi.MessagePinMessage,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    scrollReply: (Long) -> Unit,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick }) {
        val senderId = (message.senderId as? TdApi.MessageSenderUser)?.userId
        val msg: AnnotatedString = (senderId?.let { senderIdValue ->
            viewModel.getUser(senderIdValue)?.let { sender ->
                buildAnnotatedString {
                    append("${sender.firstName + " " + sender.lastName} pinned ")
                    pushStringAnnotation(tag = "msg", annotation = String())
                    append("message")
                    pop()
                }
            } ?: buildAnnotatedString {
                append("Channel pinned ")
                pushStringAnnotation(tag = "msg", annotation = String())
                append("message")
                pop()
            }
        } ?: buildAnnotatedString {
            append("Channel pinned ")
            pushStringAnnotation(tag = "msg", annotation = String())
            append("message")
            pop()
        })
        ClickableText(text = msg,
            style = MaterialTheme.typography.caption1.copy(color = MaterialTheme.colors.onSurface)
                .copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .padding(bottom = 4.dp)
                .align(Alignment.Center),
            onClick = { offset ->
                msg.getStringAnnotations(tag = "msg", start = offset, end = offset).firstOrNull()
                    ?.let {
                        scrollReply(content.messageId)
                    }
            })
    }
}

@Composable
fun UnsupportedMessage(
    message: TdApi.Message,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    onClick: () -> Unit = {},
    scrollReply: (Long) -> Unit,
    showSender: Long?,
) {
    MessageCard(
        message,
        navController = navController,
        onClick = onClick,
        viewModel = viewModel,
        scrollReply = scrollReply,
        showSender = showSender
    ) {
        Text("Unsupported message")
    }
}