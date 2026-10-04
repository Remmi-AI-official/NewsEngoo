package com.example.data.repository

import com.example.data.local.dao.GrammarDao
import com.example.data.local.entity.GrammarRuleEntity
import com.example.domain.model.GrammarImportItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GrammarRepository(private val grammarDao: GrammarDao) {
    fun getAllRules(): Flow<List<GrammarRuleEntity>> = grammarDao.getAllRules()

    fun getRulesForDate(date: String): Flow<List<GrammarRuleEntity>> = grammarDao.getRulesForDate(date)

    fun getRuleById(id: String): Flow<GrammarRuleEntity?> = grammarDao.getRuleById(id)

    suspend fun saveRule(rule: GrammarRuleEntity) = grammarDao.insertRule(rule)

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = grammarDao.setFavorite(id, isFavorite)

    suspend fun setCompleted(id: String, isCompleted: Boolean) = grammarDao.setCompleted(id, isCompleted)

    suspend fun updateNote(id: String, note: String) = grammarDao.updateNote(id, note)

    suspend fun deleteRule(id: String) = grammarDao.deleteRule(id)

    fun getFavoriteRules(): Flow<List<GrammarRuleEntity>> = grammarDao.getFavoriteRules()

    fun searchRules(query: String): Flow<List<GrammarRuleEntity>> = grammarDao.searchRules(query)

    suspend fun importRules(items: List<GrammarImportItem>, date: String): Int {
        val entities = items.map { item ->
            GrammarRuleEntity(
                id = "rule_${date}_${item.title.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")}",
                date = date,
                title = item.title.trim(),
                rule = item.rule.trim(),
                explanation = item.explanation.trim(),
                correctExamples = item.correctExamples,
                incorrectExamples = item.incorrectExamples,
                commonMistakes = item.commonMistakes.trim(),
                editorialExample = item.editorialExample.trim(),
                difficulty = item.difficulty,
                tags = item.tags
            )
        }
        grammarDao.insertRules(entities)
        return entities.size
    }
}
