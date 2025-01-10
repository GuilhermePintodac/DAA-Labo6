package ch.heigvd.iict.and.rest.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE,
    NONE
}

@Entity
data class Contact(@PrimaryKey(autoGenerate = true) var id: Long? = null,
              var name: String,
              var firstname: String?,
              var birthday : Calendar?,
              var email: String?,
              var address: String?,
              var zip: String?,
              var city: String?,
              var type: PhoneType?,
              var phoneNumber: String?,
              var isDirty: Boolean = false,
              val operationType: OperationType = OperationType.NONE
    )