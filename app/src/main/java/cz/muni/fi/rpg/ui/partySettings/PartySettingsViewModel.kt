package cz.muni.fi.rpg.ui.partySettings

import cz.frantisekmasa.wfrp_master.core.domain.party.Party
import cz.frantisekmasa.wfrp_master.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.core.domain.party.PartyRepository
import cz.frantisekmasa.wfrp_master.core.domain.party.settings.Settings
import cz.frantisekmasa.wfrp_master.core.utils.right
import kotlinx.coroutines.flow.Flow

class PartySettingsViewModel(
    private val partyId: PartyId,
    private val parties: PartyRepository
) {
    val party: Flow<Party> = parties.getLive(partyId).right()

    suspend fun updateSettings(change: (Settings) -> Settings) {
        val party = parties.get(partyId)

        party.updateSettings(change(party.getSettings()))

        parties.save(party)
    }
}