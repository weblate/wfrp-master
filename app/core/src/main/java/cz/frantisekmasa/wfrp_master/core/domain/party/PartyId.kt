package cz.frantisekmasa.wfrp_master.core.domain.party

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
@Serializable
@Parcelize
value class PartyId(@Contextual private val value: UUID) : Parcelable {
    companion object {
        fun fromString(id: String): PartyId = PartyId(UUID.fromString(id))
        fun generate(): PartyId = PartyId(UUID.randomUUID())
    }

    override fun toString() = value.toString()
}
