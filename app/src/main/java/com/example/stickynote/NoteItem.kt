package com.example.stickynote

import org.json.JSONArray
import org.json.JSONObject

/** Single bullet-point item inside a widget's note list. */
data class NoteItem(
    val id: String,
    val text: String,
    val isChecked: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("text", text)
        put("isChecked", isChecked)
    }

    companion object {
        fun fromJson(obj: JSONObject) = NoteItem(
            id       = obj.getString("id"),
            text     = obj.getString("text"),
            isChecked = obj.optBoolean("isChecked", false)
        )
    }
}

fun List<NoteItem>.toJsonString(): String =
    JSONArray().also { arr -> forEach { arr.put(it.toJson()) } }.toString()

fun String.toNoteItems(): List<NoteItem> = runCatching {
    val arr = JSONArray(this)
    (0 until arr.length()).map { NoteItem.fromJson(arr.getJSONObject(it)) }
}.getOrElse { emptyList() }
