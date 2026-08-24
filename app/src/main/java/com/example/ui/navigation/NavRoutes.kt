package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavItem(
    val route: String,
    val title: String,
    val section: String,
    val icon: ImageVector
) {
    // Core OS
    DASHBOARD("dashboard", "Dashboard", "System Core", Icons.Default.Dashboard),
    STUDY("study", "Study Focus", "System Core", Icons.Default.Timer),
    TIMELINE("timeline", "Timeline & Routine", "System Core", Icons.Default.Schedule),
    PLANNER("planner", "Weekly Planner", "Academic", Icons.Default.CalendarMonth),
    REVISION("revision", "Revision System", "Academic", Icons.Default.Repeat),
    TESTS("tests", "Tests & Papers", "Academic", Icons.Default.Assignment),
    PYQ_BANK("pyq_bank", "PYQ Bank", "Academic", Icons.Default.Storage),
    PDF_LIBRARY("pdf_library", "Resource Vault (PDFs)", "Academic", Icons.Default.FolderSpecial),

    // AI Engine
    AI_TUTOR("ai_tutor", "AI Tutor", "AI Intelligence", Icons.Default.SmartToy),
    AI_COACH("ai_coach", "AI OS Coach", "AI Intelligence", Icons.Default.Psychology),
    AI_TEST_GEN("ai_test_gen", "AI Test Generator", "AI Intelligence", Icons.Default.AutoAwesome),

    // System Analytics & Preferences
    ANALYTICS("analytics", "Analytics & Scorecard", "System", Icons.Default.BarChart),
    NOTIFICATIONS("notifications", "Notifications", "System", Icons.Default.Notifications),
    SETTINGS("settings", "Settings", "System", Icons.Default.Settings),
    ABOUT("about", "About Rudra OS", "System", Icons.Default.Info)
}
