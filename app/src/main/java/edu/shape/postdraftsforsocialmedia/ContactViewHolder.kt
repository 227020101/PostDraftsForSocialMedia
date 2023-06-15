package edu.shape.postdraftsforsocialmedia

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvName: TextView = itemView.findViewById(R.id.contactName)
    var tvPhoneNum: TextView = itemView.findViewById(R.id.phoneNum)
    var deleteContact: ImageView = itemView.findViewById(R.id.deleteContact)
    var editContact: ImageView = itemView.findViewById(R.id.editContact)
}