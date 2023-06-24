package edu.shape.postdraftsforsocialmedia.Controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import edu.shape.postdraftsforsocialmedia.Model.ContactViewHolder
import edu.shape.postdraftsforsocialmedia.Model.Contacts
import edu.shape.postdraftsforsocialmedia.Model.SqliteDatabase
import edu.shape.postdraftsforsocialmedia.R

import java.util.*
internal class ContactAdapter(private val context: Context, listContacts: ArrayList<Contacts>) :
    RecyclerView.Adapter<ContactViewHolder>(), Filterable {
    private var listContacts: ArrayList<Contacts>
    private val mArrayList: ArrayList<Contacts>
    private val mDatabase: SqliteDatabase
    init {
        this.listContacts = listContacts
        this.mArrayList = listContacts
        mDatabase = SqliteDatabase(context)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.contact_list_layout, parent, false)
        return ContactViewHolder(view)
    }
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contacts = listContacts[position]
        holder.tvPhoneNum.text = contacts.id.toString()
        holder.tvName.text = contacts.name
        holder.editContact.setOnClickListener { editTaskDialog(contacts) }
        holder.deleteContact.setOnClickListener {
            mDatabase.deleteContact(contacts.id)
            (context as Activity).finish()
            context.startActivity(context.intent)
        }
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                listContacts = if (charString.isEmpty()) {
                    mArrayList
                }
                else {
                    val filteredList = ArrayList<Contacts>()
                    for (contacts in mArrayList) {
                        if (contacts.name.toLowerCase().contains(charString)) {
                            filteredList.add(contacts)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = listContacts
                return filterResults
            }
            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            )
            {
                listContacts =
                    filterResults.values as ArrayList<Contacts>
                notifyDataSetChanged()
            }
        }
    }
    override fun getItemCount(): Int {
        return listContacts.size
    }
    private fun editTaskDialog(contacts: Contacts) {
        val intent = Intent(this.context, MainActivity::class.java)
        intent.putExtra("id",contacts.id.toString())
        context.startActivity(intent)
    }
}