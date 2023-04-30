package moe.astar.telegramw.ui.about

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import org.drinkless.tdlib.About
import org.drinkless.tdlib.BuildConfig

@Composable
fun AboutScreen(
    navController: NavController,
    viewModel: AboutViewModel
) {
    val manager = LocalContext.current.packageManager
    val tgwInfo = manager.getPackageInfo(LocalContext.current.packageName, PackageManager.GET_ACTIVITIES)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 0.dp)
    ) {
        item { Text(text = "About", textAlign = TextAlign.Center, style = MaterialTheme.typography.title1) }
        item {
            TitleCard(
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
                onClick = {},
                title = { Text("TDLib") },
                contentColor = MaterialTheme.colors.onSurface,
                titleColor = MaterialTheme.colors.onSurface
            ) {
                Text(About.TDLIB_VERSION + " (" + About.TDLIB_GITHASH + ")")
            }
        }
    }
}