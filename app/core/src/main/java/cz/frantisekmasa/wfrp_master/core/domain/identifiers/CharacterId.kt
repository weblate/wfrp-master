package cz.frantisekmasa.wfrp_master.core.domain.identifiers

import android.os.Parcelable
import cz.frantisekmasa.wfrp_master.core.domain.party.PartyId
import kotlinx.parcelize.Parcelize

@Parcelize
data class CharacterId(val partyId: PartyId, val id: String) : Parcelable