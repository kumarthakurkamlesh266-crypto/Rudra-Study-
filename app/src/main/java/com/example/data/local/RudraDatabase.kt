package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TimelineBlockEntity::class,
        TopicProgressEntity::class,
        RevisionTaskEntity::class,
        DailyScorecardEntity::class,
        PyqEntity::class,
        FocusSessionEntity::class,
        GeneratedTestEntity::class,
        PdfDocumentEntity::class,
        VaultDocumentEntity::class,
        ChapterEntity::class,
        QuestionEntity::class,
        PatternEntity::class,
        TestAttemptEntity::class,
        AiChatMessageEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class RudraDatabase : RoomDatabase() {
    abstract fun timelineDao(): TimelineDao
    abstract fun topicProgressDao(): TopicProgressDao
    abstract fun revisionTaskDao(): RevisionTaskDao
    abstract fun scorecardDao(): ScorecardDao
    abstract fun pyqDao(): PyqDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun generatedTestDao(): GeneratedTestDao
    abstract fun pdfDao(): PdfDao
    abstract fun vaultDocumentDao(): VaultDocumentDao
    abstract fun chapterDao(): ChapterDao
    abstract fun questionDao(): QuestionDao
    abstract fun patternDao(): PatternDao
    abstract fun testAttemptDao(): TestAttemptDao
    abstract fun aiChatDao(): AiChatDao

    companion object {
        @Volatile
        private var INSTANCE: RudraDatabase? = null

        fun getDatabase(context: Context): RudraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RudraDatabase::class.java,
                    "rudra_life_os.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
