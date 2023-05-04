package moe.astar.telegramw.ui.about

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import org.drinkless.tdlib.About

@Composable
fun AboutScreen(
    navController: NavController,
    viewModel: AboutViewModel
) {
    val manager = LocalContext.current.packageManager
    val tgwInfo =
        manager.getPackageInfo(LocalContext.current.packageName, PackageManager.GET_ACTIVITIES)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 0.dp)
    ) {
        item {
            Text(
                text = "About",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.title2
            )
        }
        item {
            TitleCard(
                modifier = Modifier.fillMaxWidth(0.9f),
                onClick = {},
                title = { Text("Telegram W") },
                contentColor = MaterialTheme.colors.onSurface,
                titleColor = MaterialTheme.colors.onSurface
            ) {
                Text(tgwInfo.versionName + " (" + moe.astar.telegramw.BuildConfig.GITHASH + ")")
            }
        }
        item {
            TitleCard(
                modifier = Modifier.fillMaxWidth(0.9f),
                onClick = {},
                title = { Text("TDLight") },
                contentColor = MaterialTheme.colors.onSurface,
                titleColor = MaterialTheme.colors.onSurface
            ) {
                Text(About.TDLIB_VERSION + " (" + About.TDLIB_GITHASH + ")")
            }
        }
    }
}