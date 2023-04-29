package moe.astar.telegramw.ui.settings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.InvalidProtocolBufferException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import moe.astar.telegramw.NotificationGroup
import moe.astar.telegramw.NotificationPreferneces
import moe.astar.telegramw.UserPreferences
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

val Context.userDataStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_pref.pb",
    serializer = UserPreferencesSerializer
)

val Context.notificationDataStore: DataStore<NotificationGroup> by dataStore(
    fileName = "notification_pref.pb",
    serializer = NotificationSerializer
)

object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            return UserPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) = t.writeTo(output)
}

object NotificationSerializer : Serializer<NotificationGroup> {
    override val defaultValue: NotificationGroup = NotificationGroup.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): NotificationGroup {
        try {
            return NotificationGroup.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: NotificationGroup, output: OutputStream) = t.writeTo(output)
}

class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // read
    val flow: Flow<UserPreferences> = context.userDataStore.data

    // rw
    suspend fun toggleNotification() {
        context.userDataStore.updateData { userData ->
            val notificationEnabled = userData.notificationEnabled xor true
            userData
                .toBuilder()
                .setNotificationEnabled(notificationEnabled)
                .build()
        }
    }

    suspend fun setNotificationEnabled(state: Boolean) {
        context.userDataStore.updateData { userData ->
            userData
                .toBuilder()
                .setNotificationEnabled(state)
                .build()
        }
    }
}

class NotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // read
    val flow: Flow<NotificationGroup> = context.notificationDataStore.data

    // rw
    suspend fun toggleNotification(chatId: Long) {
        context.notificationDataStore.updateData { data ->
            val group = data.groupsMap.getOrPut(
                chatId,
                defaultValue = { NotificationPreferneces.getDefaultInstance() })
            val newGroup =
                NotificationPreferneces.newBuilder().setIsMuted(group.isMuted xor true).build()
            data.toBuilder().removeGroups(chatId).putGroups(chatId, newGroup).build()
        }
    }


    suspend fun setNotification(chatId: Long, state: Boolean) {
        context.notificationDataStore.updateData { data ->
            val newGroup = NotificationPreferneces.newBuilder().setIsMuted(state).build()
            data.toBuilder().removeGroups(chatId).putGroups(chatId, newGroup).build()
        }
    }
    /*suspend fun toggleNotification() {
        context.notificationDataStore.updateData { userData ->
            val notificationEnabled = userData.notificationEnabled xor true
            userData
                .toBuilder()
                .setNotificationEnabled(notificationEnabled)
                .build()
        }
    }*/
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserPreferencesRepository
    //val client: TelegramClient,
) : ViewModel() {
    val flow: Flow<UserPreferences> = userRepository.flow

    fun toggleNotification() {
        viewModelScope.launch { userRepository.toggleNotification() }
    }

    fun setNotificationEnabled(state: Boolean) {
        viewModelScope.launch { userRepository.setNotificationEnabled(state) }
    }
}