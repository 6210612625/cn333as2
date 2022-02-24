package com.example.mynotes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.mynotes.databinding.MainActivityBinding
import com.example.mynotes.models.TaskList
import com.example.mynotes.ui.detail.ListDetailFragment
import com.example.mynotes.ui.main.MainFragment
import com.example.mynotes.ui.main.MainViewModel
import com.example.mynotes.ui.main.MainViewModelFactory

class  MainActivity : AppCompatActivity(), MainFragment.MainFragmentInteractionListener {

    private lateinit var binding: MainActivityBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this,
            MainViewModelFactory(PreferenceManager.getDefaultSharedPreferences(this))
        ).get(MainViewModel::class.java)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val mainFragment = MainFragment.newInstance()
            mainFragment.clickListener = this
            mainFragment.holdClickListener = this
            val fragmentContainerId : Int = if (binding.mainFragmentContainer == null) {
                R.id.container
            }else {
                R.id.main_fragment_container
            }
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(fragmentContainerId, mainFragment)
            }
        }

        binding.addButton.setOnClickListener {
            frontCreateList()
        }
    }
    private fun frontCreateList() {
        val dialogTitle = getString(R.string.enterNameOfNote)
        val positiveButTitle = getString(R.string.createList)

        val builder = AlertDialog.Builder(this)
        val listTitleEditText = EditText(this)
        listTitleEditText.inputType = InputType.TYPE_CLASS_TEXT
        builder.setTitle(dialogTitle)
        builder.setView(listTitleEditText)

        builder.setPositiveButton(positiveButTitle) { dialog, _ ->
            dialog.dismiss()
            if (viewModel.find(listTitleEditText.text.toString())) {
                Toast.makeText(this,"The note name has been taken",Toast.LENGTH_LONG).show()
            }else {
                val noteList = TaskList(listTitleEditText.text.toString(), "")
                viewModel.createList(noteList)
                showListDetail(noteList)
            }
        }

        builder.create().show()
    }

    private fun showListDetail(list: TaskList) {
        if (binding.mainFragmentContainer == null) {
            val note = Intent(this, ListDetailActivity::class.java)
            note.putExtra(INTENT_LIST_KEY, list)
            startActivity(note)
        }else{
            val bundle = bundleOf(INTENT_LIST_KEY to list)

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.noteFragment, ListDetailFragment::class.java,bundle,null)
            }
        }
    }

    companion object {
        const val INTENT_LIST_KEY = "list"
        const val LIST_DETAIL_REQUEST_CODE = 123
    }

    override fun listItemTapped(note: TaskList) {
        showListDetail(note)
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LIST_DETAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                viewModel.updateList(data.getParcelableExtra(INTENT_LIST_KEY)!!)
            }
        }
    }

    override fun onBackPressed() {
        val listNoteFragment = supportFragmentManager.findFragmentById(R.id.noteFragment)
        if (listNoteFragment == null) {
            super.onBackPressed()
        } else {
            title = resources.getString(R.string.app_name)
            val editNoteText: EditText = findViewById(R.id.editTextNote)
            viewModel.saveList(TaskList(viewModel.note.name,editNoteText.text.toString()))
            editNoteText.setText("")
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                remove(listNoteFragment)
            }
            binding.addButton.setOnClickListener {
                frontCreateList()
            }
        }
    }
}
