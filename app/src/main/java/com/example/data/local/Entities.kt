package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeline_blocks")
data class TimelineBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String,
    val startTime: String, // e.g. "06:15" (24h)
    val endTime: String,   // e.g. "07:30" (24h)
    val type: String,      // "DEEP_FOCUS", "REVISION", "ROUTINE", "SCHOOL", "REST", "FITNESS", "FREE", "SHUTDOWN", "SLEEP"
    val subjectTag: String = "",
    val triggerAction: String = "",
    val backupVersion: String = "",
    val isAnchor: Boolean = true,
    val orderIndex: Int = 0,
    val isCompletedToday: Boolean = false
)

@Entity(tableName = "topic_progress")
data class TopicProgressEntity(
    @PrimaryKey
    val topicId: String, // "BSEB_PHY_U1_CH1_TOPIC1"
    val board: String,   // "BSEB", "CBSE"
    val subject: String, // "Physics", "Chemistry", "Biology", "Hindi", "English"
    val unitName: String,
    val chapterName: String,
    val topicName: String,
    val difficulty: String = "Medium", // "Beginner", "Medium", "Advanced"
    val status: String = "NOT_STARTED", // "NOT_STARTED", "LEARNING", "PRACTICING", "REVISED", "MASTERED"
    val completionPercent: Int = 0,
    val revisionCount: Int = 0,
    val lastRevisedTimestamp: Long = 0L,
    val isWeakTopic: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "revision_tasks")
data class RevisionTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topicId: String,
    val topicName: String,
    val subject: String,
    val chapterName: String,
    val intervalType: String, // "SAME_DAY", "PLUS_1", "PLUS_3", "PLUS_7", "SUNDAY_SUMMARY", "CUSTOM"
    val dueDateTimestamp: Long,
    val isCompleted: Boolean = false,
    val completedTimestamp: Long = 0L
)

@Entity(tableName = "daily_scorecards")
data class DailyScorecardEntity(
    @PrimaryKey
    val dateString: String, // "YYYY-MM-DD"
    val wokeUpOnTime: Boolean = false,
    val completedBlock1: Boolean = false,
    val completedBlock3: Boolean = false,
    val completedFitness: Boolean = false,
    val completedBlock5: Boolean = false,
    val didShutdownRitual: Boolean = false,
    val noPhoneBlockedHours: Boolean = false,
    val studyHoursMinutes: Int = 0, // total study minutes logged
    val journalLineDone: String = "", // "Aaj kya kiya"
    val journalLineMissed: String = "", // "Kya miss hua"
    val journalLineFocusTomorrow: String = "", // "Kal ka ek focus"
    val isEmergencyDay: Boolean = false,
    val isLowEnergyDay: Boolean = false
) {
    val totalScore: Int
        get() = (if (wokeUpOnTime) 1 else 0) +
                (if (completedBlock1) 1 else 0) +
                (if (completedBlock3) 1 else 0) +
                (if (completedFitness) 1 else 0) +
                (if (completedBlock5) 1 else 0) +
                (if (didShutdownRitual) 1 else 0) +
                (if (noPhoneBlockedHours) 1 else 0)

    val scoreStatus: String
        get() = when {
            totalScore >= 5 -> "GREEN"
            totalScore >= 3 -> "YELLOW"
            else -> "RED"
        }
}

@Entity(tableName = "pyq_bank")
data class PyqEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val board: String, // "BSEB", "CBSE"
    val subject: String,
    val unit: String,
    val chapter: String,
    val topic: String,
    val year: Int,
    val marks: Int,
    val questionType: String, // "MCQ", "SHORT", "LONG", "NUMERICAL"
    val difficulty: String,   // "Beginner", "Medium", "Advanced"
    val questionText: String,
    val optionsJson: String = "", // For MCQs: ["Option A", "Option B", ...]
    val answerText: String,
    val stepByStepSolution: String = ""
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String,
    val topicOrTask: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val parkingLotThoughts: String = "",
    val qualityRating: Int = 5 // 1 to 5
)

@Entity(tableName = "generated_tests")
data class GeneratedTestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val board: String,
    val subject: String,
    val unit: String = "",
    val chapter: String = "",
    val topic: String = "",
    val testType: String, // "TOPIC_TEST", "CHAPTER_TEST", "UNIT_TEST", "SUBJECT_TEST", "HALF_SYLLABUS", "FULL_SYLLABUS", "MOCK_EXAM", "CUSTOM", "RAPID_QUIZ", "REVISION_QUIZ", "PYQ_ONLY", "MODEL_PAPER_STYLE", "HIGH_PROBABILITY", "WEAK_TOPICS"
    val difficulty: String, // "Beginner", "Medium", "Advanced"
    val totalMarks: Int,
    val timeMinutes: Int,
    val questionsJson: String, // JSON array of questions
    val questionPaperMarkdown: String = "",
    val answerKeyMarkdown: String = "",
    val solutionMarkdown: String = "",
    val analysisSummaryJson: String = "",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val completedTimestamp: Long = 0L,
    val userScore: Int = -1,
    val accuracyPercent: Float = 0f,
    val isCompleted: Boolean = false
)

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "FORMULA_SHEET", "SUMMARY_NOTES", "TEST_PAPER", "PYQ_SET"
    val subject: String,
    val description: String,
    val contentMarkdown: String,
    val isBookmarked: Boolean = false,
    val lastOpenedTimestamp: Long = 0L
)

@Entity(tableName = "vault_documents")
data class VaultDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val title: String,
    val fileUriOrPath: String,
    val fileSizeBytes: Long = 0L,
    val pageCount: Int = 1,
    val subject: String = "Physics", // "Physics", "Chemistry", "Biology", "Mathematics", "Hindi", "English", "General"
    val category: String = "QUESTION_BANK", // "PYQ_PAPER", "SAMPLE_PAPER", "CHAPTER_NOTES", "FORMULA_SHEET", "QUESTION_BANK", "REFERENCE_MATERIAL"
    val board: String = "BSEB", // "BSEB", "CBSE", "ALL"
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val extractedText: String = "",
    val isAnalyzed: Boolean = false,
    val analyzedSummary: String = "",
    val questionsCount: Int = 0,
    val patternsCount: Int = 0,
    val isBookmarked: Boolean = false,
    val lastOpenedTimestamp: Long = 0L
)

@Entity(
    tableName = "chapter_database",
    indices = [
        androidx.room.Index(value = ["subject", "chapterName"]),
        androidx.room.Index(value = ["board", "subject"])
    ]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: String = "", // e.g. "BSEB_PHY_CH01"
    val board: String = "BSEB", // "BSEB", "CBSE", "ALL"
    val subject: String = "Physics", // "Physics", "Chemistry", "Biology", "Mathematics", "Hindi", "English"
    val unitName: String = "",
    val chapterName: String,
    val chapterNumber: Int = 1,
    val weightageMarks: Int = 8,
    val weightagePercentage: Float = 10.0f,
    val topicsJson: String = "[]", // JSON array of mapped topic names e.g. ["Coulomb's Law", "Electric Dipole", "Gauss Theorem"]
    val difficultyDistribution: String = "Medium", // "40% Easy, 40% Medium, 20% Hard"
    val keyFormulas: String = "",
    val highYieldDerivations: String = "",
    val extractedQuestionsCount: Int = 0,
    val isHighYield: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "question_database",
    indices = [
        androidx.room.Index(value = ["subject", "chapterName"]),
        androidx.room.Index(value = ["difficulty"]),
        androidx.room.Index(value = ["topicName"])
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceVaultDocId: Long = 0L,
    val sourceDocName: String = "Resource Vault",
    val board: String = "BSEB", // "BSEB", "CBSE", "ALL"
    val subject: String,
    val unitName: String = "",
    val chapterName: String,
    val topicName: String = "", // Topic mapping for weak-area targeting & chapter filtering
    val subtopic: String = "",
    val questionText: String,
    val questionType: String = "SHORT", // "MCQ", "SHORT", "LONG", "NUMERICAL", "DERIVATION", "ASSERTION_REASON", "CASE_STUDY"
    val optionsJson: String = "", // JSON array e.g. ["A", "B", "C", "D"]
    val correctAnswer: String = "",
    val stepByStepSolution: String = "",
    val marks: Int = 2, // 1, 2, 3, 5
    val difficulty: String = "Medium", // "Easy", "Medium", "Hard", "Advanced"
    val frequencyScore: Int = 3, // 1 to 5
    val yearOrSource: String = "Board Model / PYQ",
    val isImportant: Boolean = false,
    val bloomsTaxonomyLevel: String = "Application", // "Recall", "Understanding", "Application", "Analysis", "Evaluation"
    val formulaUsed: String = "",
    val diagramRequired: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "pattern_database",
    indices = [
        androidx.room.Index(value = ["subject", "chapterName"]),
        androidx.room.Index(value = ["patternType"]),
        androidx.room.Index(value = ["difficultyLevel"])
    ]
)
data class PatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceVaultDocId: Long = 0L,
    val subject: String,
    val unitName: String = "",
    val chapterName: String,
    val topicName: String = "", // Topic mapping
    val patternType: String = "HIGH_WEIGHTAGE_DERIVATION", // "HIGH_WEIGHTAGE_DERIVATION", "REPEATED_CONCEPT", "FAVORITE_NUMERICAL_TYPE", "COMMON_BOARD_TRAP", "FREQUENT_MCQ", "CASE_STUDY"
    val difficultyLevel: String = "Medium", // "Easy", "Medium", "Hard", "Advanced"
    val frequency: Int = 3, // Number of recurring appearances in exams/vault
    val averageMarks: Int = 5,
    val title: String,
    val description: String,
    val weightagePercentage: Int = 15,
    val recurringQuestionIdsJson: String = "[]",
    val examTip: String = "",
    val commonMistakesToAvoid: String = "",
    val createdTimestamp: Long = System.currentTimeMillis()
)

// Convenience Type Aliases for direct semantic usage
typealias Question = QuestionEntity
typealias Chapter = ChapterEntity
typealias Pattern = PatternEntity

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testId: Long = 0L,
    val testTitle: String,
    val board: String,
    val subject: String,
    val testMode: String,
    val difficulty: String,
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val skippedCount: Int,
    val totalMarks: Int,
    val scoredMarks: Int,
    val accuracyPercentage: Float,
    val timeTakenSeconds: Int,
    val timeLimitMinutes: Int,
    val weakTopicsJson: String = "[]",
    val strongTopicsJson: String = "[]",
    val improvementSuggestions: String = "",
    val revisionRecommendations: String = "",
    val nextTestRecommendation: String = "",
    val userAnswersJson: String = "{}",
    val attemptTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_chat_messages")
data class AiChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "assistant"
    val content: String,
    val subject: String = "Physics",
    val mode: String = "EL10", // "EL10", "EL5", "BEGINNER", "INTERMEDIATE", "ADVANCED", "BOARD_EXAM"
    val language: String = "BILINGUAL", // "BILINGUAL", "HINDI", "ENGLISH"
    val timestamp: Long = System.currentTimeMillis(),
    val isGrounded: Boolean = false,
    val promptTag: String = "", // e.g. "Concept", "Numerical", "Flowchart", "EL5", "Quiz"
    val isBookmarked: Boolean = false
)
