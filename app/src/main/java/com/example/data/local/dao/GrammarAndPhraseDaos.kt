package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GrammarRuleEntity
import com.example.data.local.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarDao {
    @Query("SELECT * FROM grammar_rules ORDER BY date DESC, createdAt DESC")
    fun getAllRules(): Flow<List<GrammarRuleEntity>>

    @Query("SELECT * FROM grammar_rules ORDER BY date DESC, createdAt DESC")
    suspend fun getAllRulesSync(): List<GrammarRuleEntity>

    @Query("SELECT * FROM grammar_rules WHERE date = :date ORDER BY createdAt ASC")
    fun getRulesForDate(date: String): Flow<List<GrammarRuleEntity>>

    @Query("SELECT * FROM grammar_rules WHERE id = :id LIMIT 1")
    fun getRuleById(id: String): Flow<GrammarRuleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: GrammarRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<GrammarRuleEntity>)

    @Update
    suspend fun updateRule(rule: GrammarRuleEntity)

    @Query("DELETE FROM grammar_rules WHERE id = :id")
    suspend fun deleteRule(id: String)

    @Query("DELETE FROM grammar_rules WHERE date = :date")
    suspend fun deleteRulesForDate(date: String)

    @Query("DELETE FROM grammar_rules")
    suspend fun clearAll()

    @Query("UPDATE grammar_rules SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE grammar_rules SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setCompleted(id: String, isCompleted: Boolean)

    @Query("UPDATE grammar_rules SET personalNote = :note WHERE id = :id")
    suspend fun updateNote(id: String, note: String)

    @Query("SELECT * FROM grammar_rules WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavoriteRules(): Flow<List<GrammarRuleEntity>>

    @Query("SELECT * FROM grammar_rules WHERE title LIKE '%' || :query || '%' OR rule LIKE '%' || :query || '%'")
    fun searchRules(query: String): Flow<List<GrammarRuleEntity>>
}

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases_expressions ORDER BY date DESC, createdAt DESC")
    fun getAllPhrases(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases_expressions ORDER BY date DESC, createdAt DESC")
    suspend fun getAllPhrasesSync(): List<PhraseEntity>

    @Query("SELECT * FROM phrases_expressions WHERE date = :date ORDER BY createdAt ASC")
    fun getPhrasesForDate(date: String): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases_expressions WHERE id = :id LIMIT 1")
    fun getPhraseById(id: String): Flow<PhraseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrases(phrases: List<PhraseEntity>)

    @Update
    suspend fun updatePhrase(phrase: PhraseEntity)

    @Query("DELETE FROM phrases_expressions WHERE id = :id")
    suspend fun deletePhrase(id: String)

    @Query("DELETE FROM phrases_expressions WHERE date = :date")
    suspend fun deletePhrasesForDate(date: String)

    @Query("DELETE FROM phrases_expressions")
    suspend fun clearAll()

    @Query("UPDATE phrases_expressions SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE phrases_expressions SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setCompleted(id: String, isCompleted: Boolean)

    @Query("UPDATE phrases_expressions SET personalNote = :note WHERE id = :id")
    suspend fun updateNote(id: String, note: String)

    @Query("SELECT * FROM phrases_expressions WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavoritePhrases(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases_expressions WHERE phrase LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%'")
    fun searchPhrases(query: String): Flow<List<PhraseEntity>>
}
