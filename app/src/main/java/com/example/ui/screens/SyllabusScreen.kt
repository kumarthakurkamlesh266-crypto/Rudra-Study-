package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TopicProgressEntity
import com.example.ui.RudraViewModel
import com.example.ui.components.RudraTopAppBar
import com.example.ui.navigation.NavItem
import com.example.ui.theme.*

// Heatmap Colors
private val ColorCompleted = Color(0xFF10B981)   // Green
private val ColorInProgress = Color(0xFFF59E0B)  // Yellow / Amber
private val ColorNotStarted = Color(0xFFEF4444)  // Red

private data class SubjectVisual(
    val name: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val containerColor: Color
)

private val SUBJECT_VISUALS = mapOf(
    "Physics" to SubjectVisual("Physics", Icons.Default.ElectricBolt, RudraCyan, Color(0xFF0E7490)),
    "Chemistry" to SubjectVisual("Chemistry", Icons.Default.Science, RudraEmerald, Color(0xFF047857)),
    "Biology" to SubjectVisual("Biology", Icons.Default.Biotech, RudraAmber, Color(0xFFB45309)),
    "Mathematics" to SubjectVisual("Mathematics", Icons.Default.Calculate, RudraPurple, Color(0xFF6D28D9)),
    "Hindi" to SubjectVisual("Hindi", Icons.Default.Translate, Color(0xFFEC4899), Color(0xFFBE185D)),
    "English" to SubjectVisual("English", Icons.Default.MenuBook, Color(0xFF3B82F6), Color(0xFF1D4ED8))
)

private fun calculateProgressPercent(topics: List<TopicProgressEntity>): Int {
    if (topics.isEmpty()) return 0
    val completedCount = topics.count { it.status == "COMPLETED" || it.status == "MASTERED" }
    return (completedCount * 100) / topics.size
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    viewModel: RudraViewModel,
    onOpenDrawer: () -> Unit,
    onNavigate: (NavItem) -> Unit
) {
    val context = LocalContext.current

    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle()
    val selectedSubjects by viewModel.selectedSyllabusSubjects.collectAsStateWithLifecycle()
    val searchQuery by viewModel.syllabusSearchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.syllabusStatusFilter.collectAsStateWithLifecycle()

    // Track expanded state for Units and Chapters
    var expandedUnits by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedChapters by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Auto-expand first unit on first load
    LaunchedEffect(selectedSubjects, allTopics) {
        if (expandedUnits.isEmpty() && selectedSubjects.isNotEmpty() && allTopics.isNotEmpty()) {
            val firstUnit = allTopics.filter { selectedSubjects.contains(it.subject) }
                .map { it.unitName }
                .firstOrNull()
            if (firstUnit != null) {
                expandedUnits = setOf(firstUnit)
            }
        }
    }

    // PCB Summary Progress Calculations
    val physicsTopics = remember(allTopics) { allTopics.filter { it.subject.equals("Physics", ignoreCase = true) } }
    val chemistryTopics = remember(allTopics) { allTopics.filter { it.subject.equals("Chemistry", ignoreCase = true) } }
    val biologyTopics = remember(allTopics) { allTopics.filter { it.subject.equals("Biology", ignoreCase = true) } }

    val physicsProgress = remember(physicsTopics) { calculateProgressPercent(physicsTopics) }
    val chemistryProgress = remember(chemistryTopics) { calculateProgressPercent(chemistryTopics) }
    val biologyProgress = remember(biologyTopics) { calculateProgressPercent(biologyTopics) }
    val overallPcbProgress = remember(physicsTopics, chemistryTopics, biologyTopics) {
        val pcbList = physicsTopics + chemistryTopics + biologyTopics
        calculateProgressPercent(pcbList)
    }

    // Filtered topics based on Subject, Search Query, and Status Filter
    val filteredTopics = remember(allTopics, selectedSubjects, searchQuery, statusFilter) {
        allTopics.filter { topic ->
            // 1. Subject match
            val matchesSubject = selectedSubjects.contains(topic.subject)

            // 2. Search query match
            val matchesSearch = if (searchQuery.isBlank()) true else {
                topic.subject.contains(searchQuery, ignoreCase = true) ||
                topic.unitName.contains(searchQuery, ignoreCase = true) ||
                topic.chapterName.contains(searchQuery, ignoreCase = true) ||
                topic.topicName.contains(searchQuery, ignoreCase = true)
            }

            // 3. Status filter match
            val matchesFilter = when (statusFilter) {
                "COMPLETED" -> topic.status == "COMPLETED" || topic.status == "MASTERED"
                "LEARNING" -> topic.status == "LEARNING" || topic.status == "IN_PROGRESS" || topic.status == "PRACTICING"
                "NOT_STARTED" -> topic.status == "NOT_STARTED"
                else -> true
            }

            matchesSubject && matchesSearch && matchesFilter
        }
    }

    // Grouping by Subject -> Unit -> Chapter -> List<Topic>
    val hierarchicalData = remember(filteredTopics) {
        filteredTopics
            .groupBy { it.subject }
            .mapValues { (_, subTopics) ->
                subTopics
                    .groupBy { it.unitName }
                    .mapValues { (_, unitTopics) ->
                        unitTopics.groupBy { it.chapterName }
                    }
            }
    }

    Scaffold(
        topBar = {
            RudraTopAppBar(
                title = "Syllabus",
                onMenuClick = onOpenDrawer,
                currentStreak = streak,
                isLowEnergy = isLowEnergy,
                actions = {
                    IconButton(
                        onClick = {
                            val allVisibleUnits = filteredTopics.map { it.unitName }.toSet()
                            expandedUnits = if (expandedUnits.containsAll(allVisibleUnits)) {
                                emptySet()
                            } else {
                                allVisibleUnits
                            }
                        },
                        modifier = Modifier.testTag("syllabus_toggle_expand_all")
                    ) {
                        Icon(
                            imageVector = if (expandedUnits.isNotEmpty()) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = "Expand / Collapse All",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("syllabus_screen_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. OVERALL PCB PROGRESS DASHBOARD WIDGET
            item {
                PcbProgressDashboardWidget(
                    physicsPercent = physicsProgress,
                    chemistryPercent = chemistryProgress,
                    biologyPercent = biologyProgress,
                    overallPercent = overallPcbProgress,
                    onSelectSubject = { subject ->
                        viewModel.selectSingleSyllabusSubject(subject)
                    },
                    onSelectPcb = {
                        viewModel.selectAllPcbSyllabus()
                    }
                )
            }

            // 2. SUBJECT SELECTION BAR (Single & Multi-Select with Presets)
            item {
                SubjectSelectionSection(
                    selectedSubjects = selectedSubjects,
                    allTopics = allTopics,
                    onToggleSubject = { viewModel.toggleSyllabusSubject(it) },
                    onSelectPcb = { viewModel.selectAllPcbSyllabus() },
                    onSelectAll = { viewModel.selectAllSyllabusSubjects() }
                )
            }

            // 3. SEARCH & STATUS FILTER SYSTEM
            item {
                SearchAndFilterCard(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSyllabusSearch(it) },
                    selectedFilter = statusFilter,
                    onSelectFilter = { viewModel.setSyllabusFilter(it) },
                    totalFilteredTopics = filteredTopics.size,
                    completedCount = filteredTopics.count { it.status == "COMPLETED" || it.status == "MASTERED" },
                    inProgressCount = filteredTopics.count { it.status == "LEARNING" || it.status == "IN_PROGRESS" || it.status == "PRACTICING" },
                    notStartedCount = filteredTopics.count { it.status == "NOT_STARTED" }
                )
            }

            // 4. EMPTY STATE IF NO MATCH
            if (filteredTopics.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Syllabus Topics Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing search keywords or switching filters to see full syllabus.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.setSyllabusSearch("")
                                    viewModel.setSyllabusFilter("ALL")
                                    viewModel.selectAllPcbSyllabus()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            }

            // 5. HIERARCHICAL SYLLABUS TREE: SUBJECT -> UNIT -> CHAPTER -> TOPICS
            hierarchicalData.forEach { (subject, unitsMap) ->
                val subjectVisual = SUBJECT_VISUALS[subject] ?: SubjectVisual(
                    name = subject,
                    icon = Icons.Default.MenuBook,
                    primaryColor = RudraCyan,
                    containerColor = Color(0xFF0E7490)
                )

                val subjectAllTopics = unitsMap.values.flatMap { chapMap -> chapMap.values.flatten() }
                val subjectCompleted = subjectAllTopics.count { it.status == "COMPLETED" || it.status == "MASTERED" }
                val subjectTotal = subjectAllTopics.size

                item(key = "subject_header_$subject") {
                    SubjectSectionDivider(
                        subject = subject,
                        subjectVisual = subjectVisual,
                        totalUnits = unitsMap.size,
                        totalTopics = subjectTotal,
                        completedTopics = subjectCompleted
                    )
                }

                unitsMap.forEach { (unitName, chaptersMap) ->
                    val isUnitExpanded = expandedUnits.contains(unitName)
                    val unitTopics = chaptersMap.values.flatten()
                    val unitCompleted = unitTopics.count { it.status == "COMPLETED" || it.status == "MASTERED" }
                    val unitTotal = unitTopics.size
                    val unitPercent = if (unitTotal > 0) (unitCompleted * 100) / unitTotal else 0

                    item(key = "unit_$unitName") {
                        UnitExpandableCard(
                            unitName = unitName,
                            subjectVisual = subjectVisual,
                            totalChapters = chaptersMap.size,
                            completedTopics = unitCompleted,
                            totalTopics = unitTotal,
                            progressPercent = unitPercent,
                            isExpanded = isUnitExpanded,
                            onToggleExpand = {
                                expandedUnits = if (isUnitExpanded) {
                                    expandedUnits - unitName
                                } else {
                                    expandedUnits + unitName
                                }
                            }
                        )
                    }

                    if (isUnitExpanded) {
                        chaptersMap.forEach { (chapterName, topics) ->
                            val chapterKey = "${subject}_${unitName}_$chapterName"
                            val isChapterExpanded = expandedChapters.contains(chapterKey)
                            val chapterCompleted = topics.count { it.status == "COMPLETED" || it.status == "MASTERED" }
                            val chapterTotal = topics.size
                            val chapterPercent = if (chapterTotal > 0) (chapterCompleted * 100) / chapterTotal else 0

                            item(key = "chapter_$chapterKey") {
                                ChapterExpandableCard(
                                    subject = subject,
                                    chapterName = chapterName,
                                    topics = topics,
                                    completedTopics = chapterCompleted,
                                    totalTopics = chapterTotal,
                                    progressPercent = chapterPercent,
                                    isExpanded = isChapterExpanded,
                                    subjectColor = subjectVisual.primaryColor,
                                    onToggleExpand = {
                                        expandedChapters = if (isChapterExpanded) {
                                            expandedChapters - chapterKey
                                        } else {
                                            expandedChapters + chapterKey
                                        }
                                    },
                                    onMarkComplete = {
                                        viewModel.markChapterComplete(subject, chapterName)
                                        Toast.makeText(context, "Marked \"$chapterName\" as Completed!", Toast.LENGTH_SHORT).show()
                                    },
                                    onStartRevision = {
                                        viewModel.startChapterRevision(subject, chapterName)
                                        Toast.makeText(context, "Added \"$chapterName\" to Spaced Revision queue!", Toast.LENGTH_SHORT).show()
                                    },
                                    onGenerateAiTest = {
                                        onNavigate(NavItem.AI_TEST_GEN)
                                    },
                                    onOpenNotes = {
                                        onNavigate(NavItem.PDF_LIBRARY)
                                    },
                                    onOpenPyq = {
                                        onNavigate(NavItem.PYQ_BANK)
                                    },
                                    onTopicStatusCycle = { topicId ->
                                        viewModel.cycleTopicStatus(topicId)
                                    },
                                    onAskAiAboutTopic = { topicName ->
                                        viewModel.sendAiTutorMessage(
                                            userQuery = "Explain the Class 12 $subject topic: \"$topicName\" (Chapter: $chapterName)",
                                            subject = subject,
                                            mode = "EL10"
                                        )
                                        onNavigate(NavItem.AI_TUTOR)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 1. OVERALL PCB PROGRESS DASHBOARD WIDGET
// ------------------------------------------------------------------------------------------------
@Composable
private fun PcbProgressDashboardWidget(
    physicsPercent: Int,
    chemistryPercent: Int,
    biologyPercent: Int,
    overallPercent: Int,
    onSelectSubject: (String) -> Unit,
    onSelectPcb: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pcb_progress_dashboard_widget")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Overall PCB Progress + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RudraPurple.copy(alpha = 0.2f))
                            .border(1.dp, RudraPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = RudraPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Overall PCB Progress",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Class 12th Board Syllabus Completion",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Overall Percentage Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        overallPercent >= 75 -> ColorCompleted.copy(alpha = 0.15f)
                        overallPercent >= 40 -> ColorInProgress.copy(alpha = 0.15f)
                        else -> ColorNotStarted.copy(alpha = 0.15f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            overallPercent >= 75 -> ColorCompleted
                            overallPercent >= 40 -> ColorInProgress
                            else -> ColorNotStarted
                        }
                    ),
                    modifier = Modifier.clickable { onSelectPcb() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$overallPercent%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = when {
                                    overallPercent >= 75 -> ColorCompleted
                                    overallPercent >= 40 -> ColorInProgress
                                    else -> ColorNotStarted
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall Progress Animated Bar
            val animatedOverall by animateFloatAsState(
                targetValue = overallPercent / 100f,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                label = "pcb_bar"
            )
            LinearProgressIndicator(
                progress = { animatedOverall },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = RudraPurple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Individual 3-Pillar Cards: Physics, Chemistry, Biology
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubjectProgressPillar(
                    subject = "Physics",
                    percent = physicsPercent,
                    color = RudraCyan,
                    icon = Icons.Default.ElectricBolt,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSubject("Physics") }
                )

                SubjectProgressPillar(
                    subject = "Chemistry",
                    percent = chemistryPercent,
                    color = RudraEmerald,
                    icon = Icons.Default.Science,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSubject("Chemistry") }
                )

                SubjectProgressPillar(
                    subject = "Biology",
                    percent = biologyPercent,
                    color = RudraAmber,
                    icon = Icons.Default.Biotech,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSubject("Biology") }
                )
            }
        }
    }
}

@Composable
private fun SubjectProgressPillar(
    subject: String,
    percent: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = subject,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subject,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 2. SUBJECT SELECTION SECTION
// ------------------------------------------------------------------------------------------------
@Composable
private fun SubjectSelectionSection(
    selectedSubjects: Set<String>,
    allTopics: List<TopicProgressEntity>,
    onToggleSubject: (String) -> Unit,
    onSelectPcb: () -> Unit,
    onSelectAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_selection_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUBJECT SELECTION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Preset Chips Row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedSubjects == setOf("Physics", "Chemistry", "Biology")) RudraPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (selectedSubjects == setOf("Physics", "Chemistry", "Biology")) RudraPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.clickable { onSelectPcb() }
                ) {
                    Text(
                        text = "PCB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSubjects == setOf("Physics", "Chemistry", "Biology")) RudraPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedSubjects.size >= 5) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (selectedSubjects.size >= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.clickable { onSelectAll() }
                ) {
                    Text(
                        text = "All Subjects",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSubjects.size >= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Subject Badges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val availableSubjects = listOf("Physics", "Chemistry", "Biology", "Hindi", "English")

            items(availableSubjects) { subject ->
                val isSelected = selectedSubjects.contains(subject)
                val visual = SUBJECT_VISUALS[subject] ?: SubjectVisual(subject, Icons.Default.MenuBook, RudraCyan, Color(0xFF0E7490))
                val subjectTopics = remember(allTopics, subject) { allTopics.filter { it.subject.equals(subject, ignoreCase = true) } }
                val completedCount = subjectTopics.count { it.status == "COMPLETED" || it.status == "MASTERED" }
                val totalCount = subjectTopics.size
                val percent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) visual.primaryColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) visual.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .clickable { onToggleSubject(subject) }
                        .testTag("subject_toggle_$subject")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox indicator
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) visual.primaryColor else Color.Transparent)
                                .border(1.dp, if (isSelected) visual.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = visual.icon,
                            contentDescription = subject,
                            tint = if (isSelected) visual.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = subject,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) visual.primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "$percent%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) visual.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 3. SEARCH & STATUS FILTER SYSTEM
// ------------------------------------------------------------------------------------------------
@Composable
private fun SearchAndFilterCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    totalFilteredTopics: Int,
    completedCount: Int,
    inProgressCount: Int,
    notStartedCount: Int
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("syllabus_search_and_filter_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "Search Subjects, Units, Chapters, Topics...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("syllabus_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RudraAmber,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusFilterTab(
                    label = "All ($totalFilteredTopics)",
                    isSelected = selectedFilter == "ALL",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFilter("ALL") }
                )

                StatusFilterTab(
                    label = "Done ($completedCount)",
                    isSelected = selectedFilter == "COMPLETED",
                    accentColor = ColorCompleted,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFilter("COMPLETED") }
                )

                StatusFilterTab(
                    label = "Learning ($inProgressCount)",
                    isSelected = selectedFilter == "LEARNING",
                    accentColor = ColorInProgress,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFilter("LEARNING") }
                )

                StatusFilterTab(
                    label = "Pending ($notStartedCount)",
                    isSelected = selectedFilter == "NOT_STARTED",
                    accentColor = ColorNotStarted,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFilter("NOT_STARTED") }
                )
            }
        }
    }
}

@Composable
private fun StatusFilterTab(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 4. SUBJECT SECTION DIVIDER
// ------------------------------------------------------------------------------------------------
@Composable
private fun SubjectSectionDivider(
    subject: String,
    subjectVisual: SubjectVisual,
    totalUnits: Int,
    totalTopics: Int,
    completedTopics: Int
) {
    val percent = if (totalTopics > 0) (completedTopics * 100) / totalTopics else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = subjectVisual.icon,
                contentDescription = subject,
                tint = subjectVisual.primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = subject.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = subjectVisual.primaryColor
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = subjectVisual.primaryColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "$totalUnits Units",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = subjectVisual.primaryColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = "$completedTopics / $totalTopics Topics ($percent%)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

// ------------------------------------------------------------------------------------------------
// 5. UNIT EXPANDABLE CARD
// ------------------------------------------------------------------------------------------------
@Composable
private fun UnitExpandableCard(
    unitName: String,
    subjectVisual: SubjectVisual,
    totalChapters: Int,
    completedTopics: Int,
    totalTopics: Int,
    progressPercent: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "unit_expand_rotation"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.5.dp,
            if (isExpanded) subjectVisual.primaryColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        shadowElevation = if (isExpanded) 3.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .testTag("unit_card_${unitName.filter { it.isLetterOrDigit() }}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = unitName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalChapters Chapters • $completedTopics / $totalTopics Topics Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Heatmap progress badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            progressPercent == 100 -> ColorCompleted.copy(alpha = 0.15f)
                            progressPercent > 0 -> ColorInProgress.copy(alpha = 0.15f)
                            else -> ColorNotStarted.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                progressPercent == 100 -> ColorCompleted
                                progressPercent > 0 -> ColorInProgress
                                else -> ColorNotStarted
                            }
                        )
                    ) {
                        Text(
                            text = "$progressPercent%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = when {
                                progressPercent == 100 -> ColorCompleted
                                progressPercent > 0 -> ColorInProgress
                                else -> ColorNotStarted
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = subjectVisual.primaryColor,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    progressPercent == 100 -> ColorCompleted
                    progressPercent > 0 -> ColorInProgress
                    else -> ColorNotStarted
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 6. CHAPTER EXPANDABLE CARD WITH TOPICS & QUICK ACTIONS
// ------------------------------------------------------------------------------------------------
@Composable
private fun ChapterExpandableCard(
    subject: String,
    chapterName: String,
    topics: List<TopicProgressEntity>,
    completedTopics: Int,
    totalTopics: Int,
    progressPercent: Int,
    isExpanded: Boolean,
    subjectColor: Color,
    onToggleExpand: () -> Unit,
    onMarkComplete: () -> Unit,
    onStartRevision: () -> Unit,
    onGenerateAiTest: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenPyq: () -> Unit,
    onTopicStatusCycle: (String) -> Unit,
    onAskAiAboutTopic: (String) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chap_expand_rotation"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isExpanded) subjectColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
            .testTag("chapter_card_${chapterName.filter { it.isLetterOrDigit() }}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Chapter Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chapterName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedTopics / $totalTopics Topics • $progressPercent%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            progressPercent == 100 -> ColorCompleted.copy(alpha = 0.15f)
                            progressPercent > 0 -> ColorInProgress.copy(alpha = 0.15f)
                            else -> ColorNotStarted.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = "$progressPercent%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                progressPercent == 100 -> ColorCompleted
                                progressPercent > 0 -> ColorInProgress
                                else -> ColorNotStarted
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Chapter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter Progress Bar
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = when {
                    progressPercent == 100 -> ColorCompleted
                    progressPercent > 0 -> ColorInProgress
                    else -> ColorNotStarted
                },
                trackColor = MaterialTheme.colorScheme.surface
            )

            // Expanded Topics List & Quick Actions
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Quick Action Buttons Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChapterActionButton(
                            label = "Mark Complete",
                            icon = Icons.Default.DoneAll,
                            color = ColorCompleted,
                            onClick = onMarkComplete,
                            modifier = Modifier.weight(1f)
                        )
                        ChapterActionButton(
                            label = "Revision",
                            icon = Icons.Default.Repeat,
                            color = RudraPurple,
                            onClick = onStartRevision,
                            modifier = Modifier.weight(1f)
                        )
                        ChapterActionButton(
                            label = "AI Test",
                            icon = Icons.Default.Quiz,
                            color = RudraAmber,
                            onClick = onGenerateAiTest,
                            modifier = Modifier.weight(1f)
                        )
                        ChapterActionButton(
                            label = "Notes",
                            icon = Icons.Default.MenuBook,
                            color = RudraCyan,
                            onClick = onOpenNotes,
                            modifier = Modifier.weight(1f)
                        )
                        ChapterActionButton(
                            label = "PYQ",
                            icon = Icons.Default.HistoryEdu,
                            color = RudraEmerald,
                            onClick = onOpenPyq,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Topic Rows
                    topics.forEach { topic ->
                        TopicTrackableRow(
                            topic = topic,
                            onCycleStatus = { onTopicStatusCycle(topic.topicId) },
                            onAskAi = { onAskAiAboutTopic(topic.topicName) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 7. TOPIC TRACKABLE ROW
// ------------------------------------------------------------------------------------------------
@Composable
private fun TopicTrackableRow(
    topic: TopicProgressEntity,
    onCycleStatus: () -> Unit,
    onAskAi: () -> Unit
) {
    val isDone = topic.status == "COMPLETED" || topic.status == "MASTERED"
    val isLearning = topic.status == "LEARNING" || topic.status == "IN_PROGRESS" || topic.status == "PRACTICING"

    val statusColor = when {
        isDone -> ColorCompleted
        isLearning -> ColorInProgress
        else -> ColorNotStarted
    }

    val statusLabel = when {
        isDone -> "Completed"
        isLearning -> "Learning"
        else -> "Not Started"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCycleStatus() }
            .testTag("topic_row_${topic.topicId}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Checkbox / Status Circle
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDone) ColorCompleted else if (isLearning) ColorInProgress.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.5.dp,
                            statusColor,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (isLearning) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ColorInProgress)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = topic.topicName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Status: $statusLabel (Tap to cycle)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = statusColor
                        )
                    )
                }
            }

            // Quick Ask AI Button
            IconButton(
                onClick = onAskAi,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Ask AI Tutor",
                    tint = RudraAmber,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
