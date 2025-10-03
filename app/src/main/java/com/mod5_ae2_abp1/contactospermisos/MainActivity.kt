package com.mod5_ae2_abp1.contactospermisos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.FragmentContainerView

/**
 * MainActivity: Muestra la lista de contactos y gestiona los permisos de lectura.
 * Se han agregado correcciones para asegurar la correcta visibilidad del contenedor de fragmentos
 * y la implementación del mensaje de lista vacía.
 */
class MainActivity : AppCompatActivity(), AddContactFragment.Companion.OnContactSavedListener {

    private val TAG = "MainActivity_Kotlin"

    // Inicialización de vistas
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddContact: FloatingActionButton // el signo más que se ve sobre la pantalla
    private lateinit var fragmentContainer: FragmentContainerView
    private lateinit var emptyListTextView: TextView
    private lateinit var adapter: ContactAdapter
    private var contactList: MutableList<Contact> = mutableListOf()

    // Aquí gestionamos los permisos
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadContacts()
            } else {
                Toast.makeText(this,
                    "Permiso de lectura denegado. No se puede cargar la lista.",
                    Toast.LENGTH_LONG).show()
                // Si falla, se muestra el mensaje de lista vacía
                emptyListTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización y configuración de vistas
        recyclerView      = findViewById(R.id.recycler_view)
        fabAddContact     = findViewById(R.id.fab_add_contact)
        fragmentContainer = findViewById(R.id.fragment_container)
        emptyListTextView = findViewById(R.id.text_empty_list)

        adapter                    = ContactAdapter(contactList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter       = adapter

        // Gestionamos los elementos que no deben verse al inicio
        fragmentContainer.visibility = View.GONE
        emptyListTextView.visibility = View.GONE

        // Usamos un lambda para el click listener sobre el signo "+" flotante
        fabAddContact.setOnClickListener { showAddContactFragment() }

        // 2. Revisión de permisos y carga de datos.
        checkReadContactsPermission()
    }

    /**
     * Función que comprueba si el permiso 'READ_CONTACTS' está concedido y, en caso contrario, lo
     * solicita.
     */
    private fun checkReadContactsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    /**
     * Función que carga la lista de contactos del Content Provider.
     */
    private fun loadContacts() {
        contactList.clear()
        val uri        = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        try {
            contentResolver.query(uri, projection, null, null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC")?.use { cursor ->
                if (cursor.count > 0) {
                    val idIndex       = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex     = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    while (cursor.moveToNext()) {
                        val contactId      = cursor.getString(idIndex)
                        val name           = cursor.getString(nameIndex)
                        val hasPhoneNumber = cursor.getInt(hasPhoneIndex)
                        var phone          = "N/A"

                        if (hasPhoneNumber > 0) {
                            contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(contactId),
                                null
                            )?.use { phoneCursor ->
                                if (phoneCursor.moveToFirst()) {
                                    val phoneIndex = phoneCursor.
                                    getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    phone = phoneCursor.getString(phoneIndex)
                                }
                            }
                        }

                        val parts     = name.split(" ", limit = 2)
                        val firstName = parts.getOrNull(0) ?: name
                        val lastName  = parts.getOrNull(1) ?: ""

                        contactList.add(Contact(firstName, lastName, phone))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al leer contactos: ${e.message}")
            Toast.makeText(this,
                "Error de Content Provider: ${e.message}",
                Toast.LENGTH_LONG).show()
        }

        adapter.setContactList(contactList)

        // Qué pasa si la lista estás vacía?
        if (contactList.isEmpty()) {
            recyclerView.visibility      = View.GONE
            emptyListTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility      = View.VISIBLE
            emptyListTextView.visibility = View.GONE
        }
    }

    // Método que muestra el fragmento para añadir un nuevo contacto.
    private fun showAddContactFragment() {
        // Oculta los elementos innecesarios de la Activity principal, incluido el msj de "lista vacía"
        recyclerView.visibility      = View.GONE
        fabAddContact.visibility     = View.GONE
        emptyListTextView.visibility = View.GONE

        // Hacer visible el contenedor del Fragmento e iniciar la transacción
        fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.commit {
            replace(R.id.fragment_container, AddContactFragment())
            addToBackStack(null)
        }
    }

    // Función que gatilla la actualización después de guardar un contacto.
    override fun onContactSaved() {
        // Recarga la lista de contactos
        checkReadContactsPermission()

        // Volver al Fragment principal
        supportFragmentManager.popBackStack()

        // Visibilidad de la Activity principal
        // La visibilidad de recyclerView y emptyListTextView se maneja dentro de checkReadContactsPermission/loadContacts
        fabAddContact.visibility     = View.VISIBLE // Hacemos el botón agregar visible
        fragmentContainer.visibility = View.GONE
    }
}