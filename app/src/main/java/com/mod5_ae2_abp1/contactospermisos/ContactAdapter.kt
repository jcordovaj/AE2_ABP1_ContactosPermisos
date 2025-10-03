package com.mod5_ae2_abp1.contactospermisos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mod5_ae2_abp1.contactospermisos.R // Asumiendo que R está disponible

// Muestra listado de contactos en un RecyclerView.
class ContactAdapter(private var contactList: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    /**
     * Reemplaza el objeto actual "lista de contactos" con una nueva lista y notifica al RecyclerView.
     * @param newContactList la nueva lista de contactos.
     */
    fun setContactList(newContactList: List<Contact>) {
        this.contactList = newContactList
        // Datos han cambiado.
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        // Inflamos la vista. Se asume la existencia de un layout item_contact.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]

        // Uso de interpolación de strings para construir el nombre completo
        holder.nameTextView.text = "${contact.name} ${contact.surname}"
        // Acceso directo a la propiedad 'phone' de la data class
        holder.phoneTextView.text = contact.phone
    }

    // Expresión de una sola línea para getItemCount
    override fun getItemCount(): Int = contactList.size

    /**
     * ViewHolder interno para el RecyclerView.
     */
    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Uso de 'val' e inferencia de tipos para las vistas
        val nameTextView: TextView = itemView.findViewById(R.id.contact_name_text_view)
        val phoneTextView: TextView = itemView.findViewById(R.id.contact_phone_text_view)
    }
}