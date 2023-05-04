package moe.astar.telegramw.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import moe.astar.telegramw.R
import moe.astar.telegramw.Screen
import moe.astar.telegramw.UserPreferences
import kotlin.system.exitProcess

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
        modifier = Modifier.fillMaxWidth(0.9f),
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
fun LogoutChip(viewModel: SettingsViewModel, navController: NavController) {
    Chip(
        modifier = Modifier.fillMaxWidth(0.9f),
        onClick = {
            viewModel.logOut()
            navController.navigate(Screen.Home.route)
        },
        label = { Text("Log out") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_logout_24),
                contentDescription = null
            )
        },
        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface)
    )
}

@Composable
fun ExitChip(viewModel: SettingsViewModel, navController: NavController) {
    Chip(
        modifier = Modifier.fillMaxWidth(0.9f),
        onClick = {
            exitProcess(0)
        },
        label = { Text("Exit") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.outline_exit_to_app_24),
                contentDescription = null
            )
        },
        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface)
    )
}

@Composable
fun AboutChip(viewModel: SettingsViewModel, navController: NavController) {
    Chip(
        modifier = Modifier.fillMaxWidth(0.9f),
        onClick = {
            navController.navigate(Screen.About.buildRoute())
        },
        label = { Text("About") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.outline_info_24),
                contentDescription = null
            )
        },
        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface)
    )
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navController: NavController) {
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
            ScalingLazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 0.dp)
            ) {
                item {
                    Text(
                        "Notifications",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.title2
                    )
                }
                item {
                    NotificationToggle(
                        state = settings.notificationEnabled,
                        checkedChange = { value ->
                            Log.d("SettingsScreen", value.toString())
                            viewModel.setNotificationEnabled(value)
                        })
                }
                item {
                    Text(
                        "General",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.title2
                    )
                }
                item { AboutChip(viewModel, navController) }
                item { LogoutChip(viewModel, navController) }
                item { ExitChip(viewModel, navController) }
            }
        }
    )
}