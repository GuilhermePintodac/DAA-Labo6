package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import java.text.SimpleDateFormat
import java.util.*

class EditContactFragment : Fragment(R.layout.fragment_edit_contact) {

    private lateinit var viewModel: ContactsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)

        // Récupérez toutes les vues nécessaires
        val nameEditText = view.findViewById<EditText>(R.id.edit_name)
        val firstnameEditText = view.findViewById<EditText>(R.id.edit_firstname)
        val emailEditText = view.findViewById<EditText>(R.id.edit_email)
        val birthdayEditText = view.findViewById<EditText>(R.id.edit_birthday)
        val addressEditText = view.findViewById<EditText>(R.id.edit_address)
        val zipEditText = view.findViewById<EditText>(R.id.edit_zip)
        val cityEditText = view.findViewById<EditText>(R.id.edit_city)
        val phoneEditText = view.findViewById<EditText>(R.id.edit_phone)

        val homeRadioButton = view.findViewById<RadioButton>(R.id.phone_type_home)
        val mobileRadioButton = view.findViewById<RadioButton>(R.id.phone_type_mobile)
        val officeRadioButton = view.findViewById<RadioButton>(R.id.phone_type_office)
        val faxRadioButton = view.findViewById<RadioButton>(R.id.phone_type_fax)

        val saveButton = view.findViewById<Button>(R.id.save_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        // Observer le contact sélectionné
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if (contact != null) {
                // Mode modification : pré-remplir les champs
                nameEditText.setText(contact.name)
                firstnameEditText.setText(contact.firstname)
                emailEditText.setText(contact.email)
                birthdayEditText.setText(contact.birthday?.let { formatDate(it) } ?: "")
                addressEditText.setText(contact.address)
                zipEditText.setText(contact.zip)
                cityEditText.setText(contact.city)
                phoneEditText.setText(contact.phoneNumber)

                // Sélectionner le bon RadioButton
                when (contact.type) {
                    PhoneType.HOME -> homeRadioButton.isChecked = true
                    PhoneType.MOBILE -> mobileRadioButton.isChecked = true
                    PhoneType.OFFICE -> officeRadioButton.isChecked = true
                    PhoneType.FAX -> faxRadioButton.isChecked = true
                    else -> {
                        homeRadioButton.isChecked = false
                        mobileRadioButton.isChecked = false
                        officeRadioButton.isChecked = false
                        faxRadioButton.isChecked = false
                    }
                }
            } else {
                // Mode création : vider les champs
                nameEditText.setText("")
                firstnameEditText.setText("")
                emailEditText.setText("")
                birthdayEditText.setText("")
                addressEditText.setText("")
                zipEditText.setText("")
                cityEditText.setText("")
                phoneEditText.setText("")

                // Désélectionner les RadioButtons
                homeRadioButton.isChecked = false
                mobileRadioButton.isChecked = false
                officeRadioButton.isChecked = false
                faxRadioButton.isChecked = false
            }
        }

        // Ajouter la logique pour les boutons "Save" et "Cancel"
        saveButton.setOnClickListener {
            val birthday = parseDateFromUserInput(birthdayEditText.text.toString())

            val newContact = Contact(
                id = viewModel.selectedContact.value?.id,
                name = nameEditText.text.toString(),
                firstname = firstnameEditText.text.toString(),
                email = emailEditText.text.toString(),
                address = addressEditText.text.toString(),
                zip = zipEditText.text.toString(),
                city = cityEditText.text.toString(),
                birthday = birthday,
                type = when {
                    homeRadioButton.isChecked -> PhoneType.HOME
                    mobileRadioButton.isChecked -> PhoneType.MOBILE
                    officeRadioButton.isChecked -> PhoneType.OFFICE
                    faxRadioButton.isChecked -> PhoneType.FAX
                    else -> null
                },
                phoneNumber = phoneEditText.text.toString()
            )

            if (viewModel.selectedContact.value == null) {
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

    // Convertir Calendar → String
    private fun formatDate(calendar: Calendar): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun isValidDateFormat(dateString: String): Boolean {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        sdf.isLenient = false // Assure une validation stricte
        return try {
            sdf.parse(dateString) != null
        } catch (e: Exception) {
            false
        }
    }

    // Convertir String → Calendar
    private fun parseDateFromUserInput(dateString: String): Calendar? {
        return if (isValidDateFormat(dateString)) {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = sdf.parse(dateString)
            Calendar.getInstance().apply {
                if (date != null) {
                    time = date
                }
            }
        } else {
            null // Retourne null si le format est invalide
        }
    }

}
