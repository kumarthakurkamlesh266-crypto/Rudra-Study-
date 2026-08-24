package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.components.SimpleMarkdownCard
import com.example.ui.theme.*

@Composable
fun AboutScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit
) {
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()

    val manifestoMarkdown = """
# Rudra Life OS 2.0 Manifesto

## The Philosophy
- **System > Willpower**: Motivation is temporary; architectural friction and automatic triggers run daily execution.
- **Consistency > Intensity**: 40% every single day beats 100% for 3 days followed by complete burnout.
- **Decision Fatigue Reduction**: The daily timeline removes the 'kya padhoon' delay.
- **The Never-Miss-Twice Rule**: Missing a single block is a human variance; missing two in a row is the start of a new bad habit.

---

## The 5 Absolute Rules
1. **Phone Outside Room**: 9:45 PM to 7:30 AM — zero smartphone in the bedroom.
2. **2-Minute Micro-Start**: When overwhelmed, sit for just 120 seconds and open Question #1.
3. **Spaced Repetition Compliance**: A topic is never done until it passes the 1-3-7 recall cycle.
4. **Low-Energy Honest Mode**: Shrink blocks rather than abandoning the system completely.
5. **Nightly 3-Line Reflection**: Objective audit before sleep.

---
*Built for Class 12 Science Board & Competitive Mastery.*
    """.trimIndent()

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "About Rudra OS",
                onMenuClick = onOpenDrawer,
                currentStreak = streak,
                isLowEnergy = isLowEnergy
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            item {
                SimpleMarkdownCard(markdownText = manifestoMarkdown, modifier = Modifier.testTag("about_manifesto_card"))
            }
        }
    }
}
