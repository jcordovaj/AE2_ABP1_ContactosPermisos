package com.mod5_ae2_abp1.contactospermisos

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.mod5_ae2_abp1.contactospermisos.R

/**
 * Fragmento para agregar un nuevo contacto.
 * * Utiliza un Intent Implícito (ContactsContract.Intents.Insert.ACTION) para delegar
 * la acción de guardar el contacto a la aplicación nativa del dispositivo.
 * Esto elimina la necesidad del permiso WRITE_CONTACTS en el Android Manifest y en el código.
 */
class AddContactFragment : Fragment() {

    private lateinit var nameInput: EditText
    private lateinit var surnameInput: EditText
    private lateinit var phoneInput: EditText

    private var contactSavedListener: OnContactSavedListener? = null

    // Uso de la API moderna de Activity Result para manejar el retorno del Intent.
    private val saveContactLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // La aplicación nativa de contactos ya guardó (o descartó) el contacto.

            // Notificar a la Activity para que recargue la lista y vuelva a la vista principal
            contactSavedListener?.onContactSaved()

            // Muestra un mensaje de confirmación
            Toast.makeText(context, "Operación de contacto finalizada. Recargando lista.", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La Activity debe implementar el listener para que podamos comunicarnos
        contactSavedListener = activity as? OnContactSavedListener
            ?: throw RuntimeException("La Activity debe implementar OnContactSavedListener")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_contact, container, false)

        // Inicializar vistas
        nameInput = view.findViewById(R.id.input_name)
        surnameInput = view.findViewById(R.id.input_surname)
        phoneInput = view.findViewById(R.id.input_phone)
        val saveButton: Button = view.findViewById(R.id.button_save_contact)

        // El botón llama a la función que lanza el Intent
        saveButton.setOnClickListener { launchSaveContactIntent() }

        return view
    }

    /**
     * Lanza el Intent Implícito para agregar un nuevo contacto.
     * Pre-rellena los campos con los datos ingresados por el usuario.
     */
    private fun launchSaveContactIntent() {
        val name = nameInput.text.toString().trim()
        val surname = surnameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        // El Intent nativo solo acepta un campo NAME que puede contener nombre y apellido.
        val fullName = "$name $surname".trim()

        if (fullName.isEmpty() && phone.isEmpty()) {
            Toast.makeText(context, "Ingrese al menos el nombre o el teléfono.", Toast.LENGTH_LONG).show()
            return
        }

        // Creación del Intent Implícito
        val insertIntent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE

            // Adjuntar los datos como extras
            putExtra(ContactsContract.Intents.Insert.NAME, fullName)
            putExtra(ContactsContract.Intents.Insert.PHONE, phone)
        }

        // Lanzar la Activity nativa del sistema
        saveContactLauncher.launch(insertIntent)
    }

    /**
     * Interface para comunicar a la Activity.
     */
    companion object {
        interface OnContactSavedListener {
            fun onContactSaved()
        }
    }
}