package com.example.mynotes.ui.main

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mynotes.models.TaskList
import java.lang.ClassCastException

class MainViewModel(val sharedPreferences: SharedPreferences) : ViewModel() {
    lateinit var note: TaskList
    lateinit var onListAdded: (() -> Unit)
    lateinit var removeNote: () -> Unit
    var decreaseOne: Int = -1

    val lists: MutableList<TaskList> by lazy {
        retrieveLists()
    }

    private fun retrieveLists(): MutableList<TaskList> {
        val sharedPreferencesContents = sharedPreferences.all
        val note = ArrayList<TaskList>()

        for (noteList in sharedPreferencesContents) {
            try {
                val itemNote = TaskList(noteList.key,noteList.value as String)
                note.add(itemNote)
            } catch (e: ClassCastException){
                Log.d(TAG,"ClassCast")
            }
        }
        return note
    }

    fun saveList(note: TaskList) {
        val editor = sharedPreferences.edit()
        val text: String = note.note
        editor.putString(note.name, text)
        editor.apply()
        Log.d(ContentValues.TAG, note.note)
    }

    fun updateList(note: TaskList) {
        val editor = sharedPreferences.edit()
        val text: String = note.note
        editor.putString(note.name, text)
        editor.apply()
        Log.d(ContentValues.TAG, note.note)
        refreshLists()
    }

    fun createList(note: TaskList) {
        val editor = sharedPreferences.edit()
        val text: String = note.note
        editor.putString(note.name, text)
        editor.apply()
        lists.add(note)
        onListAdded.invoke()
    }

    fun refreshLists() {
        lists.clear()
        lists.addAll(retrieveLists())
    }

    fun removeList(note: TaskList) {
        val index = lists.indexOf(note)
        decreaseOne = index
        lists.remove(note)
        removeNote.invoke()

        val editor = sharedPreferences.edit()
        editor.remove(note.name)
        editor.apply()
    }

    fun find(key: String):Boolean {
        return sharedPreferences.contains(key)
    }
}