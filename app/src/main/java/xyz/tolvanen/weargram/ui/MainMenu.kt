package xyz.tolvanen.weargram.ui

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
import org.drinkless.tdlib.TdApi
import xyz.tolvanen.weargram.R
import xyz.tolvanen.weargram.Screen
import xyz.tolvanen.weargram.client.Authenticator
import xyz.tolvanen.weargram.client.TelegramClient
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
fun ExitChip(viewModel: MainMenuViewModel, navController: NavController) {
    Chip(
        modifier = Modifier.fillMaxWidth(0.9f),
        onClick = {
            exitProcess(0)
        },
        label = { Text("Exit") },
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
        item { ExitChip(viewModel, navController) }
    }
}

