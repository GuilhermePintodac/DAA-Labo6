package ch.heigvd.iict.and.rest.models;

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ContactServer( val id: Long?,
                          val name: String,
                          val firstname: String,
                          val birthday: String,  // ISO format
                          val email: String,
                          val address: String,
                          val zip: String,
                          val city: String,
                          val type: String,
                          val phoneNumber: String) {
}


fun ContactServer.toContact() = Contact(
    id = null,
    name = name,
    firstname = firstname,
    birthday = parseBirthday(birthday),
    email = email,
    address = address,
    zip = zip,
    city = city,
    type = PhoneType.valueOf(type),
    phoneNumber = phoneNumber
)

private fun parseBirthday(dateStr: String): Calendar? = runCatching {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        .parse(dateStr)?.let { date ->
            Calendar.getInstance().apply { time = date }
        }
}.getOrNull()
