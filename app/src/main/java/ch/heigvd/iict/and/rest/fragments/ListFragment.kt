/*
Auteur :  Shyshmarov Alexandre / Guilherme Pinto
 */
package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentListBinding
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

class ListFragment : Fragment() {

    private lateinit var binding : FragmentListBinding

    private val contactsViewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(((requireActivity().application as ContactsApplication).repository))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ContactsAdapter(emptyList()) { _, _, _, id ->
            // we locate the contact to edit
            if (contactsViewModel.visibleContacts.value != null) { // Utilise visibleContacts
                val selectedContact = contactsViewModel.visibleContacts.value!!.find { it.id == id }
                if (selectedContact != null) {
                    contactsViewModel.selectedContact.value = selectedContact
                    findNavController().navigate(R.id.action_listFragment_to_editContactFragment)
                }
            }
        }
        binding.listRecycler.adapter = adapter
        binding.listRecycler.layoutManager = LinearLayoutManager(requireContext())

        contactsViewModel.visibleContacts.observe(viewLifecycleOwner) { updatedContacts ->
            adapter.contacts = updatedContacts
            // we display an "empty view" when adapter contains no contact
            if(updatedContacts.isEmpty()) {
                binding.listRecycler.visibility = View.GONE
                binding.listContentEmpty.visibility = View.VISIBLE
            }
            else {
                binding.listContentEmpty.visibility = View.GONE
                binding.listRecycler.visibility = View.VISIBLE
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ListFragment()

        private val TAG = ListFragment::class.java.simpleName
    }

}