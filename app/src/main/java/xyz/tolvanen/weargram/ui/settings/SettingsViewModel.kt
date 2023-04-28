package xyz.tolvanen.weargram.ui.settings

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.android.datastore.UserPreferences
import com.google.protobuf.InvalidProtocolBufferException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi.User
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

val Context.userDataStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_pref.pb",
    serializer = UserPreferencesSerializer
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