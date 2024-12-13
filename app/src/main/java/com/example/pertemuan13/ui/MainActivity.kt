package com.example.pertemuan13.ui

import android.os.Bundle
import android.provider.SyncStateContract.Helpers.insert
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pertemuan13.R
import com.example.pertemuan13.database.Note
import com.example.pertemuan13.database.NoteDao
import com.example.pertemuan13.database.NoteRoomDatabase
import com.example.pertemuan13.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        noteDao = db!!.noteDao()!!

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        with(binding) {
            btnAdd.setOnClickListener (View.OnClickListener{
                insert(
                    Note(
                        title = edtTitle.text.toString(),
                        description = edtDesc.text.toString(),
                        date = edtDate.text.toString()
                    )
                )
                setEmptyField()
            })

            btnUpdate.setOnClickListener{
                update(
                    Note(
                        id = updateId,
                        title = edtTitle.text.toString(),
                        description = edtDesc.text.toString(),
                        date = edtDate.text.toString()
                    )
                )
                updateId = 0
                setEmptyField()
            }

            listView.setOnItemClickListener { adapterView, view, i, l ->
                val item = adapterView.adapter.getItem(i) as Note
                updateId = item.id
                edtTitle.setText(item.title)
                edtDesc.setText(item.description)
                edtDate.setText(item.date)
            }

            listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
                    val item = adapterView.adapter.getItem(i) as Note
                    delete(item)
                    true
                }

        }

    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    private fun setEmptyField() {
        with(binding) {
            edtTitle.setText("")
            edtDesc.setText("")
            edtDate.setText("")
        }
    }

    private fun getAllNotes(){
        noteDao.allNotes.observe(this) { notes ->
            val adapter: ArrayAdapter<Note> = ArrayAdapter<Note>(this, android.R.layout.simple_list_item_1, notes)
            binding.listView.adapter = adapter
        }
    }

    private fun insert(note : Note){
        executorService.execute {
            noteDao.insert(note)
        }
    }

    private fun delete(note: Note) {
        executorService.execute {
            noteDao.delete(note)
        }
    }

    private fun update(note: Note) {
        executorService.execute {
            noteDao.update(note)
        }
    }

}