package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentEditContactBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import java.text.SimpleDateFormat
import java.util.*

class EditContactFragment : Fragment(R.layout.fragment_edit_contact) {

    private lateinit var viewModel: ContactsViewModel
    private lateinit var binding: FragmentEditContactBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser le ViewModel
        viewModel = ViewModelProvider(requireActivity())[ContactsViewModel::class.java]

        // Initialiser le binding
        binding = FragmentEditContactBinding.bind(view)

        // Restaurer l'état des champs en priorité
        if (savedInstanceState != null) {
            restoreFieldsFromBundle(savedInstanceState)
        } else {
            observeViewModel()
        }

        // Configurer la visibilité du bouton Delete
        configureDeleteButtonVisibility()

        setupListeners()
    }

    private fun restoreFieldsFromBundle(bundle: Bundle) {
        binding.editName.setText(bundle.getString("name", ""))
        binding.editFirstname.setText(bundle.getString("firstname", ""))
        binding.editEmail.setText(bundle.getString("email", ""))
        binding.editBirthday.setText(bundle.getString("birthday", ""))
        binding.editAddress.setText(bundle.getString("address", ""))
        binding.editZip.setText(bundle.getString("zip", ""))
        binding.editCity.setText(bundle.getString("city", ""))
        binding.editPhone.setText(bundle.getString("phone", ""))

        when (bundle.getString("phoneType")) {
            PhoneType.HOME.name -> binding.phoneTypeHome.isChecked = true
            PhoneType.MOBILE.name -> binding.phoneTypeMobile.isChecked = true
            PhoneType.OFFICE.name -> binding.phoneTypeOffice.isChecked = true
            PhoneType.FAX.name -> binding.phoneTypeFax.isChecked = true
        }
    }

    private fun observeViewModel() {
        // Observer le contact sélectionné
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if (contact != null) {
                populateFields(contact) // Remplir les champs pour la modification
            } else {
                clearFields() // Vider les champs pour la création
            }
        }
    }

    private fun configureDeleteButtonVisibility() {
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if (contact != null) {
                binding.deleteButton.visibility = View.VISIBLE // Mode modification : afficher DELETE
            } else {
                binding.deleteButton.visibility = View.GONE // Mode création : masquer DELETE
            }
        }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            saveContact()
        }

        binding.deleteButton.setOnClickListener {
            deleteContact()
        }

        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveContact() {
        val birthday = parseDateFromUserInput(binding.editBirthday.text.toString())

        val newContact = Contact(
            id = viewModel.selectedContact.value?.id,
            name = binding.editName.text.toString(),
            firstname = binding.editFirstname.text.toString(),
            email = binding.editEmail.text.toString(),
            address = binding.editAddress.text.toString(),
            zip = binding.editZip.text.toString(),
            city = binding.editCity.text.toString(),
            birthday = birthday,
            type = when {
                binding.phoneTypeHome.isChecked -> PhoneType.HOME
                binding.phoneTypeMobile.isChecked -> PhoneType.MOBILE
                binding.phoneTypeOffice.isChecked -> PhoneType.OFFICE
                binding.phoneTypeFax.isChecked -> PhoneType.FAX
                else -> null
            },
            phoneNumber = binding.editPhone.text.toString()
        )

        if (viewModel.selectedContact.value == null) {
            viewModel.insertContact(newContact)
        } else {
            viewModel.updateContact(newContact)
        }

        findNavController().navigateUp()
    }

    private fun deleteContact() {
        viewModel.selectedContact.value?.let {
            viewModel.deleteContact(it)
        }
        findNavController().navigateUp()
    }

    private fun populateFields(contact: Contact) {
        binding.editName.setText(contact.name)
        binding.editFirstname.setText(contact.firstname)
        binding.editEmail.setText(contact.email)
        binding.editBirthday.setText(contact.birthday?.let { formatDate(it) } ?: "")
        binding.editAddress.setText(contact.address)
        binding.editZip.setText(contact.zip)
        binding.editCity.setText(contact.city)
        binding.editPhone.setText(contact.phoneNumber)

        when (contact.type) {
            PhoneType.HOME -> binding.phoneTypeHome.isChecked = true
            PhoneType.MOBILE -> binding.phoneTypeMobile.isChecked = true
            PhoneType.OFFICE -> binding.phoneTypeOffice.isChecked = true
            PhoneType.FAX -> binding.phoneTypeFax.isChecked = true
            else -> clearRadioButtons()
        }
    }

    private fun clearFields() {
        binding.editName.text.clear()
        binding.editFirstname.text.clear()
        binding.editEmail.text.clear()
        binding.editBirthday.text.clear()
        binding.editAddress.text.clear()
        binding.editZip.text.clear()
        binding.editCity.text.clear()
        binding.editPhone.text.clear()
        clearRadioButtons()
    }

    private fun clearRadioButtons() {
        binding.phoneTypeHome.isChecked = false
        binding.phoneTypeMobile.isChecked = false
        binding.phoneTypeOffice.isChecked = false
        binding.phoneTypeFax.isChecked = false
    }

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

    private fun parseDateFromUserInput(dateString: String): Calendar? {
        return if (isValidDateFormat(dateString)) {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = sdf.parse(dateString)
            Calendar.getInstance().apply { time = date }
        } else {
            null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Sauvegarder les champs saisis
        outState.putString("name", binding.editName.text.toString())
        outState.putString("firstname", binding.editFirstname.text.toString())
        outState.putString("email", binding.editEmail.text.toString())
        outState.putString("birthday", binding.editBirthday.text.toString())
        outState.putString("address", binding.editAddress.text.toString())
        outState.putString("zip", binding.editZip.text.toString())
        outState.putString("city", binding.editCity.text.toString())
        outState.putString("phone", binding.editPhone.text.toString())

        // Sauvegarder le type de téléphone
        outState.putString(
            "phoneType", when {
                binding.phoneTypeHome.isChecked -> PhoneType.HOME.name
                binding.phoneTypeMobile.isChecked -> PhoneType.MOBILE.name
                binding.phoneTypeOffice.isChecked -> PhoneType.OFFICE.name
                binding.phoneTypeFax.isChecked -> PhoneType.FAX.name
                else -> null
            }
        )
    }
}

