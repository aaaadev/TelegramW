package moe.astar.telegramw

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.runBlocking
import moe.astar.telegramw.client.NotificationProvider
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import javax.inject.Inject


@AndroidEntryPoint
class NotificationService : Service() {

    private val TAG = this::class.simpleName

    @Inject
    lateinit var client: TelegramClient

    @Inject
    lateinit var notificationProvider: NotificationProvider

    private val binder = NotificationBinder()

    inner class NotificationBinder : Binder() {
        val service: NotificationService = this@NotificationService

    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "binding")
        return binder
    }


    private val markAsReadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val chatId = intent.getLongExtra("chatId", 0)
            val msgIds = intent.getLongArrayExtra("msgIds")
            Log.d(TAG, "trying to read ${msgIds?.size} messages from $chatId")
                    client.sendUnscopedRequest(
                        TdApi.ViewMessages(
                            chatId,
                            msgIds,
                            TdApi.MessageSourceNotification(),
                            true
                        )
                    )
        }
    }

    private val replyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("NotificationService", intent.extras?.containsKey("threadId").toString())
            val chatId = intent.getLongExtra("chatId", 0)
            val threadId = intent.getLongExtra("threadId", 0)
            val messageId = intent.getLongExtra("messageId", 0)
            val reply = RemoteInput.getResultsFromIntent(intent).getCharSequence("reply")
            Log.d("NotificationService", "Send reply $reply to $messageId ($threadId) $chatId")
            client.sendUnscopedRequest(
                        TdApi.SendMessage(
                            chatId,
                            threadId,
                            messageId,
                            TdApi.MessageSendOptions(),
                            null,
                            TdApi.InputMessageText(
                                TdApi.FormattedText(
                                    reply.toString(), emptyArray()
                                ),
                                false,
                                false,
                            )
                        )
                    )

        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(markAsReadReceiver, IntentFilter("MARK_READ"))
        registerReceiver(replyReceiver, IntentFilter("REPLY"))
    }

    override fun onDestroy() {
        unregisterReceiver(markAsReadReceiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                FOREGROUND_NOTIFICATION_CHANNEL_ID,
                "Telegram W",
                NotificationManager.IMPORTANCE_MIN
            )
        )

        val notification: Notification =
            NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setContentTitle("Telegram W")
                .setContentText("Telegram W is running")
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_launcher_foreground
                    )
                )
                .setContentIntent(pendingIntent)
                .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        return START_STICKY

    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "telegramx_foreground"
        private const val FOREGROUND_NOTIFICATION_ID = 97
    }
}