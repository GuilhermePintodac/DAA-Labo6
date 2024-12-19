package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel

class EditContactFragment : Fragment(R.layout.fragment_edit_contact) {

    private lateinit var viewModel: ContactsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)

        val nameEditText = view.findViewById<EditText>(R.id.edit_name)
        val firstnameEditText = view.findViewById<EditText>(R.id.edit_firstname)
        val emailEditText = view.findViewById<EditText>(R.id.edit_email)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        // Observer les données du contact sélectionné
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            nameEditText.setText(contact?.name)
            firstnameEditText.setText(contact?.firstname)
            emailEditText.setText(contact?.email)
            // Remplissez les autres champs si nécessaire
        }

        // Sauvegarder ou créer un contact
        saveButton.setOnClickListener {
            val newContact = Contact(
                id = viewModel.selectedContact.value?.id ?: 0,
                name = nameEditText.text.toString(),
                firstname = firstnameEditText.text.toString(),
                email = emailEditText.text.toString()
                // Récupérez les autres champs
            )
            if (newContact.id == 0) {
                viewModel.insertContact(newContact) // Insertion
            } else {
                viewModel.updateContact(newContact) // Mise à jour
            }
            findNavController().navigateUp()
        }

        // Annuler
        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
