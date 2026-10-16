package com.example.jarvis.core.memory

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class JarvisMemoryManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "jarvis_memory_prefs"
        private const val KEY_MASTER_NAME = "master_name"
        private const val KEY_MASTER_TITLE = "master_title"
        private const val KEY_MASTER_AGE = "master_age"
        private const val KEY_MASTER_LOCK = "master_lock"
        private const val KEY_BALANCE = "master_balance"
        private const val KEY_MEMORIES_JSON = "memories_json"
        private const val KEY_DATES_JSON = "dates_json"

        @Volatile
        private var instance: JarvisMemoryManager? = null

        fun getInstance(context: Context): JarvisMemoryManager {
            return instance ?: synchronized(this) {
                instance ?: JarvisMemoryManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _profileFlow = MutableStateFlow(loadProfile())
    val profileFlow: StateFlow<UserProfile> = _profileFlow.asStateFlow()

    private fun loadProfile(): UserProfile {
        val name = prefs.getString(KEY_MASTER_NAME, "Rafael") ?: "Rafael"
        val title = prefs.getString(KEY_MASTER_TITLE, "Chefe") ?: "Chefe"
        val age = prefs.getInt(KEY_MASTER_AGE, 30)
        val lock = prefs.getBoolean(KEY_MASTER_LOCK, false)
        val balance = prefs.getFloat(KEY_BALANCE, 12500.0f).toDouble()

        val memories = loadMemories()
        val dates = loadDates()

        return UserProfile(
            masterName = name,
            masterTitle = title,
            age = age,
            isMasterLockEnabled = lock,
            accountBalance = balance,
            memories = memories,
            importantDates = dates
        )
    }

    private fun loadMemories(): Map<String, String> {
        val jsonStr = prefs.getString(KEY_MEMORIES_JSON, "{}") ?: "{}"
        val map = mutableMapOf<String, String>()
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = json.getString(k)
            }
        } catch (_: Exception) {}
        if (map.isEmpty()) {
            map["status"] = "Chefe Supremo e Comandante do Sistema"
            map["prioridade"] = "Nível Alfa - Total"
        }
        return map
    }

    private fun loadDates(): List<ImportantDate> {
        val jsonStr = prefs.getString(KEY_DATES_JSON, "[]") ?: "[]"
        val list = mutableListOf<ImportantDate>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(ImportantDate(
                    id = obj.optString("id", "$i"),
                    title = obj.getString("title"),
                    dateString = obj.getString("date"),
                    note = obj.optString("note", "")
                ))
            }
        } catch (_: Exception) {}
        if (list.isEmpty()) {
            list.add(ImportantDate(title = "Aniversário do Chefe", dateString = "15 de Novembro", note = "Festa principal"))
            list.add(ImportantDate(title = "Reunião Executiva Stark", dateString = "Segunda-feira 09:00", note = "Apresentação"))
        }
        return list
    }

    fun addImportantDate(title: String, dateString: String, note: String = "") {
        val current = _profileFlow.value.importantDates.toMutableList()
        current.add(ImportantDate(title = title, dateString = dateString, note = note))
        saveDates(current)
        _profileFlow.value = _profileFlow.value.copy(importantDates = current)
    }

    fun updateBalance(newBalance: Double) {
        prefs.edit().putFloat(KEY_BALANCE, newBalance.toFloat()).apply()
        _profileFlow.value = _profileFlow.value.copy(accountBalance = newBalance)
    }

    fun toggleMasterLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MASTER_LOCK, enabled).apply()
        _profileFlow.value = _profileFlow.value.copy(isMasterLockEnabled = enabled)
    }

    fun rememberFact(key: String, fact: String) {
        val current = _profileFlow.value.memories.toMutableMap()
        current[key.lowercase().trim()] = fact
        saveMemories(current)
        _profileFlow.value = _profileFlow.value.copy(memories = current)
    }

    private fun saveMemories(memories: Map<String, String>) {
        val json = JSONObject()
        for ((k, v) in memories) json.put(k, v)
        prefs.edit().putString(KEY_MEMORIES_JSON, json.toString()).apply()
    }

    private fun saveDates(dates: List<ImportantDate>) {
        val arr = JSONArray()
        for (d in dates) {
            arr.put(JSONObject().apply {
                put("id", d.id)
                put("title", d.title)
                put("date", d.dateString)
                put("note", d.note)
            })
        }
        prefs.edit().putString(KEY_DATES_JSON, arr.toString()).apply()
    }
}
