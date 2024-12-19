package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
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

        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            nameEditText.setText(contact?.name)
            firstnameEditText.setText(contact?.firstname)
            emailEditText.setText(contact?.email)
        }

        saveButton.setOnClickListener {
            val newContact = Contact(
                id = viewModel.selectedContact.value?.id,
                name = nameEditText.text.toString(),
                firstname = firstnameEditText.text.toString(),
                email = emailEditText.text.toString(),
                address = "Default Address",
                zip = "0000",
                city = "Default City",
                birthday = null,
                type = PhoneType.HOME,
                phoneNumber = "1234567890"
            )
            if (newContact.id == 0L) {
                viewModel.insertContact(newContact)
            } else {
                viewModel.updateContact(newContact)
            }
            findNavController().navigateUp()
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
