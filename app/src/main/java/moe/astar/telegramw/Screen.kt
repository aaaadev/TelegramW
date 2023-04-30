package moe.astar.telegramw

import androidx.navigation.NavBackStackEntry
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {

    object Home : Screen("home")
    object MainMenu : Screen("mainMenu")
    object Login : Screen("login")

    object Topic : Screen("topic/{chatId}") {
        fun buildRoute(chatId: Long): String = "topic/${chatId}"

        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()
    }

    object Chat : Screen("chat/{chatId}/{threadId}") {
        fun buildRoute(chatId: Long, threadId: Long): String = "chat/${chatId}/${threadId}"
        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()

        fun getThreadId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("threadId")?.toLong()
    }

    object ChatMenu : Screen("chatMenu/{chatId}") {
        fun buildRoute(chatId: Long): String = "chatMenu/${chatId}"
        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()
    }

    object MessageMenu : Screen("messageMenu/{chatId}/{messageId}") {
        fun buildRoute(chatId: Long, messageId: Long): String = "messageMenu/$chatId/$messageId"
        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()

        fun getMessageId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("messageId")?.toLong()
    }

    object SelectReaction : Screen("selectReaction/{chatId}/{messageId}") {
        fun buildRoute(chatId: Long, messageId: Long): String = "selectReaction/$chatId/$messageId"
        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()

        fun getMessageId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("messageId")?.toLong()
    }

    object Info : Screen("info/{type}/{id}") {
        fun buildRoute(type: String, id: Long): String = "info/$type/$id"
        fun getId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("id")?.toLong()

        fun getType(entry: NavBackStackEntry): String? =
            entry.arguments?.getString("type")

    }

    object Video : Screen("video/{path}") {
        fun buildRoute(path: String): String =
            "video/${URLEncoder.encode(path, StandardCharsets.UTF_8.toString())}"

        fun getPath(entry: NavBackStackEntry): String = URLDecoder.decode(
            entry.arguments!!.getString("path"),
            StandardCharsets.UTF_8.toString()
        )

    }

    object Map : Screen("map/{latitude}/{longitude}") {
        fun buildRoute(latitude: Double, longitude: Double): String =
            "map/$latitude/$longitude"

        fun getCoordinates(entry: NavBackStackEntry): Pair<Double, Double> =
            Pair(
                entry.arguments?.getString("latitude")?.toDouble() ?: 0.0,
                entry.arguments?.getString("longitude")?.toDouble() ?: 0.0
            )

    }

    object Settings : Screen("settings") {
        fun buildRoute(): String = "settings"
    }

    object About : Screen("about") {
        fun buildRoute(): String = "about"
    }
    //object CreateChat : Screen("createChat")
}
