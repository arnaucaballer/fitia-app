package app.fitia.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY pinned DESC, updatedAtEpochMs DESC")
    abstract fun observeThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads ORDER BY updatedAtEpochMs DESC LIMIT 1")
    abstract suspend fun latestThread(): ChatThreadEntity?

    @Query("SELECT * FROM chat_threads WHERE id = :id")
    abstract suspend fun getThread(id: String): ChatThreadEntity?

    /** Insert, never replace: replacing the row would cascade-delete its messages. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertThread(entity: ChatThreadEntity)

    @Update
    abstract suspend fun updateThread(entity: ChatThreadEntity)

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY sortOrder ASC, id ASC")
    abstract suspend fun getMessages(threadId: String): List<ChatMessageEntity>

    @Insert
    abstract suspend fun insertMessages(entities: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    abstract suspend fun deleteMessages(threadId: String)

    @Query("DELETE FROM chat_threads WHERE id = :id")
    abstract suspend fun deleteThread(id: String)

    @Transaction
    open suspend fun saveTranscript(thread: ChatThreadEntity, messages: List<ChatMessageEntity>) {
        updateThread(thread)
        deleteMessages(thread.id)
        if (messages.isNotEmpty()) insertMessages(messages)
    }

    @Transaction
    open suspend fun restore(thread: ChatThreadEntity, messages: List<ChatMessageEntity>) {
        insertThread(thread)
        if (messages.isNotEmpty()) insertMessages(messages)
    }
}
