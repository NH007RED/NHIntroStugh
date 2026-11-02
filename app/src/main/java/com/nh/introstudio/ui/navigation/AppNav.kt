package com.nh.introstudio.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nh.introstudio.ui.components.GradientBackground
import com.nh.introstudio.ui.screens.AboutScreen
import com.nh.introstudio.ui.screens.CustomizeScreen
import com.nh.introstudio.ui.screens.EditorScreen
import com.nh.introstudio.ui.screens.ExportSettingsScreen
import com.nh.introstudio.ui.screens.HomeScreen
import com.nh.introstudio.ui.screens.LogoAiScreen
import com.nh.introstudio.ui.screens.ProjectsScreen
import com.nh.introstudio.ui.screens.RenderingScreen
import com.nh.introstudio.ui.screens.SettingsScreen
import com.nh.introstudio.ui.screens.SplashScreen
import com.nh.introstudio.ui.screens.TemplateGalleryScreen
import com.nh.introstudio.ui.screens.TemplatePreviewScreen
import com.nh.introstudio.ui.screens.TextIntroScreen
import com.nh.introstudio.ui.screens.VideoGalleryScreen
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.viewmodel.AppViewModel
import com.nh.introstudio.viewmodel.EditorViewModel
import com.nh.introstudio.viewmodel.ExportViewModel

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem("home", "Home", Icons.Filled.Home),
    BottomItem("logo_ai", "Logo AI", Icons.Filled.AutoAwesome),
    BottomItem("text_intros", "Text Intros", Icons.Filled.Movie),
    BottomItem("templates", "Templates", Icons.Filled.GridView),
    BottomItem("projects", "Projects", Icons.Filled.Folder),
    BottomItem("videos", "Videos", Icons.Filled.VideoLibrary)
)

private fun NavController.goTab(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavBackStackEntry.projectId(): Long = arguments?.getLong("projectId") ?: 0L

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val app: AppViewModel = viewModel()
    val editor: EditorViewModel = viewModel()
    val exportVm: ExportViewModel = viewModel()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (bottomItems.any { it.route == route }) {
                    NavigationBar(containerColor = Color(0xE60B0618)) {
                        for (item in bottomItems) {
                            NavigationBarItem(
                                selected = route == item.route,
                                onClick = { if (route != item.route) nav.goTab(item.route) },
                                icon = { Icon(item.icon, null) },
                                label = { Text(item.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    indicatorColor = NH.Purple.copy(alpha = 0.55f),
                                    unselectedIconColor = NH.Muted,
                                    unselectedTextColor = NH.Muted
                                )
                            )
                        }
                    }
                }
            }
        ) { pad ->
            NavHost(nav, startDestination = "splash", modifier = Modifier.padding(pad)) {
                composable("splash") {
                    SplashScreen { nav.navigate("home") { popUpTo("splash") { inclusive = true } } }
                }
                composable("home") {
                    HomeScreen(
                        app,
                        onLogoAi = { nav.goTab("logo_ai") },
                        onTextIntros = { nav.goTab("text_intros") },
                        onTemplates = { nav.goTab("templates") },
                        onFavorites = { nav.navigate("favorites") },
                        onVideos = { nav.goTab("videos") },
                        onSettings = { nav.navigate("settings") },
                        onOpenTemplate = { nav.navigate("preview/$it") },
                        onOpenProject = { nav.navigate("customize/$it") }
                    )
                }
                composable("logo_ai") {
                    LogoAiScreen(
                        app = app,
                        onExportProject = { pid -> nav.navigate("export/$pid") },
                        onOpenEditor = { pid -> nav.navigate("editor/$pid") }
                    )
                }
                composable("text_intros") {
                    TextIntroScreen(
                        app = app,
                        onExportProject = { pid -> nav.navigate("export/$pid") },
                        onOpenEditor = { pid -> nav.navigate("editor/$pid") }
                    )
                }
                composable("templates") {
                    TemplateGalleryScreen(app, favoritesOnly = false, onBack = null, onOpen = { nav.navigate("preview/$it") })
                }
                composable("favorites") {
                    TemplateGalleryScreen(app, favoritesOnly = true, onBack = { nav.popBackStack() }, onOpen = { nav.navigate("preview/$it") })
                }
                composable("preview/{templateId}", arguments = listOf(navArgument("templateId") { type = NavType.IntType })) { e ->
                    val id = e.arguments?.getInt("templateId") ?: 1
                    TemplatePreviewScreen(id, app, onBack = { nav.popBackStack() }, onUse = { tid ->
                        app.createProject(tid) { pid -> nav.navigate("customize/$pid") { popUpTo("preview/{templateId}") { inclusive = true } } }
                    })
                }
                composable("customize/{projectId}", arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { e ->
                    val pid = e.projectId()
                    CustomizeScreen(editor, pid, onBack = { nav.popBackStack() }, onEditor = { nav.navigate("editor/$pid") })
                }
                composable("editor/{projectId}", arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { e ->
                    val pid = e.projectId()
                    EditorScreen(editor, pid, onBack = { nav.popBackStack() }, onExport = { nav.navigate("export/$pid") })
                }
                composable("export/{projectId}", arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { e ->
                    val pid = e.projectId()
                    ExportSettingsScreen(editor, exportVm, app, pid, onBack = { nav.popBackStack() }, onStarted = { nav.navigate("rendering") })
                }
                composable("rendering") {
                    RenderingScreen(
                        app,
                        onHome = { nav.navigate("home") { popUpTo("home") { inclusive = false }; launchSingleTop = true } },
                        onGallery = { nav.goTab("videos") },
                        onBack = { nav.popBackStack() }
                    )
                }
                composable("projects") {
                    ProjectsScreen(app, onOpen = { nav.navigate("customize/$it") }, onNew = { nav.goTab("templates") })
                }
                composable("videos") { VideoGalleryScreen(app, onNew = { nav.goTab("templates") }) }
                composable("settings") {
                    SettingsScreen(app, onAbout = { nav.navigate("about") }, onFavorites = { nav.navigate("favorites") })
                }
                composable("about") { AboutScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}
