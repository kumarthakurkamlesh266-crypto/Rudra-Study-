package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraDrawerContent
import com.example.ui.navigation.NavItem
import com.example.ui.screens.*
import com.example.ui.theme.RudraTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: RudraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            RudraTheme(themeMode = themeMode) {
                RudraAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RudraAppContent(viewModel: RudraViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var currentRoute by remember { mutableStateOf(NavItem.DASHBOARD.route) }

    fun navigateTo(item: NavItem) {
        currentRoute = item.route
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RudraDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { navItem ->
                    navigateTo(navItem)
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Crossfade(targetState = currentRoute, label = "screen_transition") { route ->
            when (route) {
                NavItem.DASHBOARD.route -> DashboardScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onNavigate = { navigateTo(it) }
                )
                NavItem.STUDY.route -> StudyScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.TIMELINE.route -> TimelineScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.PLANNER.route -> PlannerScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.REVISION.route -> RevisionScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.TESTS.route -> TestsScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onNavigate = { navigateTo(it) }
                )
                NavItem.AI_TEST_GEN.route -> AiTestGeneratorScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.PYQ_BANK.route -> PyqBankScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.PDF_LIBRARY.route -> PdfLibraryScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.AI_TUTOR.route -> AiTutorScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.AI_COACH.route -> AiCoachScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.ANALYTICS.route -> ScorecardScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.NOTIFICATIONS.route -> NotificationsScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.SETTINGS.route -> SettingsScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                NavItem.ABOUT.route -> AboutScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
                else -> DashboardScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onNavigate = { navigateTo(it) }
                )
            }
        }
    }
}
