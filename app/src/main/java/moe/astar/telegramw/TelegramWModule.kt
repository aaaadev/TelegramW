package moe.astar.telegramw

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import moe.astar.telegramw.client.Authenticator
import moe.astar.telegramw.client.ChatProvider
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TelegramWModule {
    @Provides
    fun provideTdlibParameters(@ApplicationContext context: Context): TdApi.SetTdlibParameters {
        val manager = context.packageManager
        val tgwInfo = manager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        return TdApi.SetTdlibParameters().apply {
            // Obtain application identifier hash for Telegram API access at https://my.telegram.org
            apiId = BuildConfig.TELEGRAM_API_ID.toInt()
            apiHash = BuildConfig.TELEGRAM_API_HASH
            useMessageDatabase = true
            useSecretChats = true
            systemLanguageCode = Locale.getDefault().language
            databaseDirectory = context.filesDir.absolutePath
            deviceModel = Build.MODEL
            systemVersion = Build.VERSION.RELEASE
            applicationVersion = tgwInfo.versionName
            enableStorageOptimizer = true
        }
    }

    @Singleton
    @Provides
    fun provideTelegramClient(parameters: TdApi.SetTdlibParameters) = TelegramClient(parameters)

    @Singleton
    @Provides
    fun provideAuthenticator(telegramClient: TelegramClient) = Authenticator(telegramClient)

    @Singleton
    @Provides
    fun provideChatProvider(telegramClient: TelegramClient) = ChatProvider(telegramClient)

}