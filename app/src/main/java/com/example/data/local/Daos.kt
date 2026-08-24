package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_blocks ORDER BY orderIndex ASC")
    fun getAllBlocks(): Flow<List<TimelineBlockEntity>>

    @Query("SELECT * FROM timeline_blocks ORDER BY orderIndex ASC")
    suspend fun getAllBlocksList(): List<TimelineBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<TimelineBlockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: TimelineBlockEntity): Long

    @Update
    suspend fun updateBlock(block: TimelineBlockEntity)

    @Query("DELETE FROM timeline_blocks WHERE id = :id")
    suspend fun deleteBlockById(id: Long)

    @Query("UPDATE timeline_blocks SET isCompletedToday = :completed WHERE id = :id")
    suspend fun setCompletedToday(id: Long, completed: Boolean)

    @Query("UPDATE timeline_blocks SET isCompletedToday = 0")
    suspend fun resetDailyCompletion()
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM syllabus_subjects ORDER BY orderIndex ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM syllabus_subjects WHERE board = :board ORDER BY orderIndex ASC")
    fun getSubjectsByBoard(board: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM syllabus_subjects WHERE subjectId = :subjectId LIMIT 1")
    fun getSubjectById(subjectId: String): Flow<SubjectEntity?>

    @Query("SELECT * FROM syllabus_subjects WHERE subjectName = :subjectName LIMIT 1")
    suspend fun getSubjectByName(subjectName: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM syllabus_subjects WHERE subjectId = :subjectId")
    suspend fun deleteSubjectById(subjectId: String)

    @Query("SELECT COUNT(*) FROM syllabus_subjects")
    fun getSubjectsCount(): Flow<Int>
}

@Dao
interface UnitDao {
    @Query("SELECT * FROM syllabus_units ORDER BY orderIndex ASC, unitNumber ASC")
    fun getAllUnits(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM syllabus_units WHERE subjectId = :subjectId ORDER BY orderIndex ASC, unitNumber ASC")
    fun getUnitsBySubjectId(subjectId: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM syllabus_units WHERE subjectName = :subjectName ORDER BY orderIndex ASC, unitNumber ASC")
    fun getUnitsBySubjectName(subjectName: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM syllabus_units WHERE board = :board AND subjectName = :subjectName ORDER BY orderIndex ASC, unitNumber ASC")
    fun getUnitsByBoardAndSubject(board: String, subjectName: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM syllabus_units WHERE unitId = :unitId LIMIT 1")
    fun getUnitById(unitId: String): Flow<UnitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity): Long

    @Update
    suspend fun updateUnit(unit: UnitEntity)

    @Query("DELETE FROM syllabus_units WHERE unitId = :unitId")
    suspend fun deleteUnitById(unitId: String)

    @Query("SELECT COUNT(*) FROM syllabus_units")
    fun getUnitsCount(): Flow<Int>
}

@Dao
interface SyllabusChapterDao {
    @Query("SELECT * FROM syllabus_chapters ORDER BY orderIndex ASC, chapterNumber ASC")
    fun getAllChapters(): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE unitId = :unitId ORDER BY orderIndex ASC, chapterNumber ASC")
    fun getChaptersByUnitId(unitId: String): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE subjectId = :subjectId ORDER BY orderIndex ASC, chapterNumber ASC")
    fun getChaptersBySubjectId(subjectId: String): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE subjectName = :subjectName ORDER BY orderIndex ASC, chapterNumber ASC")
    fun getChaptersBySubject(subjectName: String): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE board = :board AND subjectName = :subjectName ORDER BY orderIndex ASC, chapterNumber ASC")
    fun getChaptersByBoardAndSubject(board: String, subjectName: String): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE chapterId = :chapterId LIMIT 1")
    fun getChapterById(chapterId: String): Flow<SyllabusChapterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<SyllabusChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: SyllabusChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: SyllabusChapterEntity)

    @Query("DELETE FROM syllabus_chapters WHERE chapterId = :chapterId")
    suspend fun deleteChapterById(chapterId: String)

    @Query("SELECT COUNT(*) FROM syllabus_chapters")
    fun getChaptersCount(): Flow<Int>
}

@Dao
interface SyllabusHierarchyDao {
    @Transaction
    @Query("SELECT * FROM syllabus_chapters WHERE chapterId = :chapterId")
    fun getChapterWithTopics(chapterId: String): Flow<ChapterWithTopics?>

    @Transaction
    @Query("SELECT * FROM syllabus_chapters WHERE unitId = :unitId ORDER BY orderIndex ASC, chapterNumber ASC")
    fun getChaptersWithTopicsByUnit(unitId: String): Flow<List<ChapterWithTopics>>

    @Transaction
    @Query("SELECT * FROM syllabus_units WHERE unitId = :unitId")
    fun getUnitWithChapters(unitId: String): Flow<UnitWithChapters?>

    @Transaction
    @Query("SELECT * FROM syllabus_units WHERE subjectId = :subjectId ORDER BY orderIndex ASC, unitNumber ASC")
    fun getUnitsWithChaptersBySubject(subjectId: String): Flow<List<UnitWithChapters>>

    @Transaction
    @Query("SELECT * FROM syllabus_subjects WHERE subjectId = :subjectId")
    fun getSubjectWithUnits(subjectId: String): Flow<SubjectWithUnits?>

    @Transaction
    @Query("SELECT * FROM syllabus_subjects WHERE board = :board ORDER BY orderIndex ASC")
    fun getAllSubjectsWithUnits(board: String): Flow<List<SubjectWithUnits>>
}

@Dao
interface TopicProgressDao {
    @Query("SELECT * FROM topic_progress ORDER BY orderIndex ASC")
    fun getAllProgress(): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE board = :board AND subject = :subject ORDER BY orderIndex ASC")
    fun getProgressBySubject(board: String, subject: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getTopicsByChapterId(chapterId: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE unitId = :unitId ORDER BY orderIndex ASC")
    fun getTopicsByUnitId(unitId: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getTopicsBySubjectId(subjectId: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE subject = :subject AND chapterName = :chapterName ORDER BY orderIndex ASC")
    fun getTopicsBySubjectAndChapter(subject: String, chapterName: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE topicId = :topicId LIMIT 1")
    suspend fun getTopicById(topicId: String): TopicProgressEntity?

    @Query("SELECT * FROM topic_progress WHERE topicId = :topicId LIMIT 1")
    fun getTopicByIdFlow(topicId: String): Flow<TopicProgressEntity?>

    @Query("SELECT * FROM topic_progress WHERE isWeakTopic = 1")
    fun getWeakTopics(): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE status = :status ORDER BY orderIndex ASC")
    fun getTopicsByStatus(status: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE topicName LIKE '%' || :query || '%' OR chapterName LIKE '%' || :query || '%' OR unitName LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%'")
    fun searchTopics(query: String): Flow<List<TopicProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicProgressEntity)

    @Update
    suspend fun updateTopic(topic: TopicProgressEntity)

    @Query("UPDATE topic_progress SET status = :status, completionPercent = :completion, lastRevisedTimestamp = :timestamp, revisionCount = revisionCount + 1 WHERE topicId = :topicId")
    suspend fun markRevised(topicId: String, status: String, completion: Int, timestamp: Long)

    @Query("UPDATE topic_progress SET status = :status, completionPercent = :completion WHERE topicId = :topicId")
    suspend fun updateTopicStatus(topicId: String, status: String, completion: Int)

    @Query("UPDATE topic_progress SET status = :status, completionPercent = :completion WHERE subject = :subject AND chapterName = :chapterName")
    suspend fun markChapterStatus(subject: String, chapterName: String, status: String, completion: Int)

    @Query("UPDATE topic_progress SET isWeakTopic = :isWeak WHERE topicId = :topicId")
    suspend fun toggleWeakTopic(topicId: String, isWeak: Boolean)

    @Query("SELECT COUNT(*) FROM topic_progress")
    fun getTotalTopicsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM topic_progress WHERE status = 'COMPLETED' OR status = 'MASTERED'")
    fun getCompletedTopicsCount(): Flow<Int>
}

@Dao
interface RevisionTaskDao {
    @Query("SELECT * FROM revision_tasks WHERE isCompleted = 0 ORDER BY dueDateTimestamp ASC")
    fun getPendingTasks(): Flow<List<RevisionTaskEntity>>

    @Query("SELECT * FROM revision_tasks ORDER BY dueDateTimestamp DESC")
    fun getAllTasks(): Flow<List<RevisionTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: RevisionTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<RevisionTaskEntity>)

    @Query("UPDATE revision_tasks SET isCompleted = 1, completedTimestamp = :timestamp WHERE id = :id")
    suspend fun markTaskCompleted(id: Long, timestamp: Long)

    @Query("DELETE FROM revision_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)
}

@Dao
interface ScorecardDao {
    @Query("SELECT * FROM daily_scorecards ORDER BY dateString DESC")
    fun getAllScorecards(): Flow<List<DailyScorecardEntity>>

    @Query("SELECT * FROM daily_scorecards WHERE dateString = :dateString LIMIT 1")
    suspend fun getScorecardByDate(dateString: String): DailyScorecardEntity?

    @Query("SELECT * FROM daily_scorecards WHERE dateString = :dateString LIMIT 1")
    fun getScorecardByDateFlow(dateString: String): Flow<DailyScorecardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScorecard(scorecard: DailyScorecardEntity)

    @Query("SELECT * FROM daily_scorecards ORDER BY dateString DESC LIMIT 7")
    fun getPastWeekScorecards(): Flow<List<DailyScorecardEntity>>
}

@Dao
interface PyqDao {
    @Query("SELECT * FROM pyq_bank")
    fun getAllPyqs(): Flow<List<PyqEntity>>

    @Query("SELECT * FROM pyq_bank WHERE board = :board AND subject = :subject ORDER BY year DESC")
    fun getPyqsBySubject(board: String, subject: String): Flow<List<PyqEntity>>

    @Query("SELECT * FROM pyq_bank WHERE subject = :subject AND chapter = :chapter ORDER BY year DESC")
    fun getPyqsByChapter(subject: String, chapter: String): Flow<List<PyqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPyqs(pyqs: List<PyqEntity>)

    @Query("SELECT DISTINCT chapter FROM pyq_bank WHERE subject = :subject")
    suspend fun getChaptersWithPyqs(subject: String): List<String>
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE timestamp >= :sinceTimestamp")
    suspend fun getTotalMinutesSince(sinceTimestamp: Long): Int?
}

@Dao
interface GeneratedTestDao {
    @Query("SELECT * FROM generated_tests ORDER BY createdTimestamp DESC")
    fun getAllTests(): Flow<List<GeneratedTestEntity>>

    @Query("SELECT * FROM generated_tests WHERE id = :id LIMIT 1")
    suspend fun getTestById(id: Long): GeneratedTestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: GeneratedTestEntity): Long

    @Update
    suspend fun updateTest(test: GeneratedTestEntity)

    @Query("DELETE FROM generated_tests WHERE id = :id")
    suspend fun deleteTestById(id: Long)
}

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf_documents ORDER BY lastOpenedTimestamp DESC")
    fun getAllPdfs(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE isBookmarked = 1")
    fun getBookmarkedPdfs(): Flow<List<PdfDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfs(pdfs: List<PdfDocumentEntity>)

    @Query("UPDATE pdf_documents SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmarked(id: Long, isBookmarked: Boolean)

    @Query("UPDATE pdf_documents SET lastOpenedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: Long, timestamp: Long)
}

@Dao
interface VaultDocumentDao {
    @Query("SELECT * FROM vault_documents ORDER BY uploadTimestamp DESC")
    fun getAllDocuments(): Flow<List<VaultDocumentEntity>>

    @Query("SELECT * FROM vault_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): VaultDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: VaultDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<VaultDocumentEntity>)

    @Update
    suspend fun updateDocument(doc: VaultDocumentEntity)

    @Query("DELETE FROM vault_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("UPDATE vault_documents SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmarked(id: Long, isBookmarked: Boolean)

    @Query("UPDATE vault_documents SET lastOpenedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: Long, timestamp: Long)

    @Query("UPDATE vault_documents SET isAnalyzed = :isAnalyzed, analyzedSummary = :summary, questionsCount = :qCount, patternsCount = :pCount, extractedText = :extractedText WHERE id = :id")
    suspend fun updateAnalyzedData(
        id: Long,
        isAnalyzed: Boolean,
        summary: String,
        qCount: Int,
        pCount: Int,
        extractedText: String
    )

    @Query("SELECT SUM(fileSizeBytes) FROM vault_documents")
    fun getTotalStorageBytes(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM vault_documents")
    fun getTotalDocumentsCount(): Flow<Int>
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapter_database ORDER BY chapterNumber ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapter_database WHERE subject = :subject ORDER BY chapterNumber ASC")
    fun getChaptersBySubject(subject: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapter_database WHERE board = :board AND subject = :subject ORDER BY chapterNumber ASC")
    fun getChaptersByBoardAndSubject(board: String, subject: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapter_database WHERE subject = :subject AND chapterName = :chapterName LIMIT 1")
    fun getChapterByName(subject: String, chapterName: String): Flow<ChapterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapter_database WHERE id = :id")
    suspend fun deleteChapterById(id: Long)

    @Query("SELECT COUNT(*) FROM chapter_database")
    fun getTotalChaptersCount(): Flow<Int>
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM question_database ORDER BY createdTimestamp DESC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM question_database WHERE subject = :subject ORDER BY createdTimestamp DESC")
    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM question_database WHERE subject = :subject AND (chapterName LIKE '%' || :chapter || '%' OR :chapter = '') ORDER BY createdTimestamp DESC")
    fun getQuestionsBySubjectAndChapter(subject: String, chapter: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM question_database WHERE subject = :subject AND topicName LIKE '%' || :topic || '%' ORDER BY createdTimestamp DESC")
    fun getQuestionsByTopic(subject: String, topic: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM question_database WHERE sourceVaultDocId = :docId ORDER BY id ASC")
    fun getQuestionsByDocId(docId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM question_database WHERE questionText LIKE '%' || :query || '%' OR chapterName LIKE '%' || :query || '%' OR topicName LIKE '%' || :query || '%'")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM question_database WHERE subject = :subject AND (:chapter = '' OR chapterName LIKE '%' || :chapter || '%') ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(subject: String, chapter: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM question_database WHERE subject = :subject AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByDifficulty(subject: String, difficulty: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM question_database WHERE subject = :subject AND topicName = :topicName AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByTopicAndDifficulty(subject: String, topicName: String, difficulty: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM question_database WHERE subject = :subject AND marks = :marks ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByMarks(subject: String, marks: Int, limit: Int): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM question_database WHERE id = :id")
    suspend fun deleteQuestionById(id: Long)

    @Query("DELETE FROM question_database WHERE sourceVaultDocId = :docId")
    suspend fun deleteQuestionsByDocId(docId: Long)

    @Query("UPDATE question_database SET isImportant = :isImp WHERE id = :id")
    suspend fun toggleImportant(id: Long, isImp: Boolean)

    @Query("SELECT COUNT(*) FROM question_database")
    fun getTotalQuestionsCount(): Flow<Int>
}

@Dao
interface PatternDao {
    @Query("SELECT * FROM pattern_database ORDER BY frequency DESC, weightagePercentage DESC")
    fun getAllPatterns(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM pattern_database WHERE subject = :subject ORDER BY frequency DESC, weightagePercentage DESC")
    fun getPatternsBySubject(subject: String): Flow<List<PatternEntity>>

    @Query("SELECT * FROM pattern_database WHERE subject = :subject AND (chapterName LIKE '%' || :chapter || '%' OR :chapter = '') ORDER BY frequency DESC")
    fun getPatternsBySubjectAndChapter(subject: String, chapter: String): Flow<List<PatternEntity>>

    @Query("SELECT * FROM pattern_database WHERE subject = :subject AND topicName LIKE '%' || :topic || '%' ORDER BY frequency DESC")
    fun getPatternsByTopic(subject: String, topic: String): Flow<List<PatternEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: PatternEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<PatternEntity>)

    @Query("DELETE FROM pattern_database WHERE id = :id")
    suspend fun deletePatternById(id: Long)

    @Query("DELETE FROM pattern_database WHERE sourceVaultDocId = :docId")
    suspend fun deletePatternsByDocId(docId: Long)

    @Query("SELECT COUNT(*) FROM pattern_database")
    fun getTotalPatternsCount(): Flow<Int>
}

@Dao
interface TestAttemptDao {
    @Query("SELECT * FROM test_attempts ORDER BY attemptTimestamp DESC")
    fun getAllAttempts(): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE testId = :testId ORDER BY attemptTimestamp DESC")
    fun getAttemptsForTest(testId: Long): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE subject = :subject ORDER BY attemptTimestamp DESC")
    fun getAttemptsBySubject(subject: String): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts ORDER BY attemptTimestamp DESC LIMIT 1")
    fun getLatestAttempt(): Flow<TestAttemptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: TestAttemptEntity): Long

    @Query("DELETE FROM test_attempts WHERE id = :id")
    suspend fun deleteAttemptById(id: Long)

    @Query("SELECT COUNT(*) FROM test_attempts")
    fun getTotalAttemptsCount(): Flow<Int>
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AiChatMessageEntity>>

    @Query("SELECT * FROM ai_chat_messages WHERE subject = :subject ORDER BY timestamp ASC")
    fun getMessagesBySubject(subject: String): Flow<List<AiChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiChatMessageEntity): Long

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clearAllMessages()

    @Query("DELETE FROM ai_chat_messages WHERE subject = :subject")
    suspend fun clearMessagesBySubject(subject: String)

    @Query("UPDATE ai_chat_messages SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun toggleBookmark(id: Long, isBookmarked: Boolean)
}

