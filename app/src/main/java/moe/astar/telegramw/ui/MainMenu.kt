package moe.astar.telegramw.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import dagger.hilt.android.lifecycle.HiltViewModel
import moe.astar.telegramw.R
import moe.astar.telegramw.Screen
import moe.astar.telegramw.client.Authenticator
import moe.astar.telegramw.client.TelegramClient
import org.drinkless.tdlib.TdApi
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val authenticator: Authenticator,
    private val client: TelegramClient,
) : ViewModel() {
    fun logOut() {
        authenticator.reset()
    }

    fun getMe(): TdApi.User? {
        return client.getMe()
    }
}

@Composable
fun LogoutChip(viewModel: MainMenuViewModel, navController: NavController) {
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
fun ProfileChip(viewModel: MainMenuViewModel, navController: NavController) {
    Chip(
        modifier = Modifier.fillMaxWidth(0.9f),
        onClick = {
            navController.navigate(Screen.Info.buildRoute("user", viewModel.getMe()!!.id))
        },
        label = { Text("Profile") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = null
            )
        },
        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface)
    )
}

@Composable
fun SettingsChip(viewModel: MainMenuViewModel, navController: NavController) {
    Chip(
        modifier = Modifier.fillMaxWidth(0.9f),
        onClick = {
            navController.navigate(Screen.Settings.buildRoute())
        },
        label = { Text("Settings") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.outline_settings_24),
                contentDescription = null
            )
        },
        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface)
    )
}

@Composable
fun AboutChip(viewModel: MainMenuViewModel, navController: NavController) {
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
fun ExitChip(viewModel: MainMenuViewModel, navController: NavController) {
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
fun MainMenuScreen(navController: NavController, viewModel: MainMenuViewModel) {

    ScalingLazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 0.dp)
    ) {
        item { LogoutChip(viewModel, navController) }
        item { ProfileChip(viewModel, navController) }
        item { SettingsChip(viewModel, navController) }
        item { AboutChip(viewModel, navController) }
        item { ExitChip(viewModel, navController) }
    }
}

