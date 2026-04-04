package com.example.stickynote

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PresetTask(
    val text: String,
    val enabled: Boolean = true
)

object PresetTasksManager {

    private const val PREFS = "PresetTaskPrefs"
    private const val KEY_PRESETS = "preset_tasks"

    fun loadPresets(context: Context): List<PresetTask> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PRESETS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                PresetTask(
                    text = obj.getString("text"),
                    enabled = obj.optBoolean("enabled", true)
                )
            }
        }.getOrElse { emptyList() }
    }

    fun savePresets(context: Context, presets: List<PresetTask>) {
        val arr = JSONArray()
        presets.forEach { p ->
            arr.put(JSONObject().apply {
                put("text", p.text)
                put("enabled", p.enabled)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PRESETS, arr.toString())
            .apply()
    }

    fun getEnabledPresets(context: Context): List<String> =
        loadPresets(context).filter { it.enabled }.map { it.text }
}
