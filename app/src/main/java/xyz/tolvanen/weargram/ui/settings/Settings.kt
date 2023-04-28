package xyz.tolvanen.weargram.ui.settings

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.codelab.android.datastore.UserPreferences
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

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
fun SettingsScreen(viewModel: SettingsViewModel) {
    /*val context = LocalContext.current

    var settings by remember { mutableStateOf(Settings(false)) }

    LaunchedEffect(Unit) {
        settings = viewModel.getSettings(context).first()
        if (settings == null) {
            val defaultSettings = Settings(
                notificationEnabled = true
            )
            viewModel.saveSettings(context, defaultSettings)
            settings = defaultSettings
        }
    }*/

    val settings by viewModel.flow.collectAsState(initial = UserPreferences.getDefaultInstance())
    Log.d("SettingsScreen", settings.toString())

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        content = {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                NotificationToggle(state = settings.notificationEnabled, checkedChange = { value ->
                    Log.d("SettingsScreen", value.toString())
                    viewModel.setNotificationEnabled(value)
                })
            }
        }
    )
}