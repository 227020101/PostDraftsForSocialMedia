package edu.shape.postdraftsforsocialmedia


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.View
import android.widget.Button

import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class SelectActivity : AppCompatActivity() {
    private lateinit var dataBase: SqliteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        title = "Social Media Draft"
        loadList()
        val btnAdd: Button = findViewById(R.id.btnAdd)
        btnAdd.setOnClickListener { addTaskDialog() }
    }

    private fun loadList() {
        val contactView: RecyclerView = findViewById(R.id.myContactList)
        val linearLayoutManager = LinearLayoutManager(this)
        contactView.layoutManager = linearLayoutManager
        contactView.setHasFixedSize(true)
        dataBase = SqliteDatabase(this)
//        dataBase = SqliteDatabase(<Contacts>= dataBase.listContacts()
        if (dataBase.listContacts().size > 0) {
            contactView.visibility = View.VISIBLE
            val mAdapter = ContactAdapter(this, dataBase.listContacts())
            contactView.adapter = mAdapter
        } else {
            contactView.visibility = View.GONE
            Toast.makeText(
                this,
                "There is no contact in the database. Start adding now",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun addTaskDialog() {
        dataBase.addContacts()
        loadList()
    }

    override fun onDestroy() {
        super.onDestroy()
        dataBase.close()
    }
}