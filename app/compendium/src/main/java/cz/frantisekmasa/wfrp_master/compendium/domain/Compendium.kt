package cz.frantisekmasa.wfrp_master.compendium.domain

import kotlinx.coroutines.flow.Flow
import cz.frantisekmasa.wfrp_master.compendium.domain.exceptions.CompendiumItemNotFound
import cz.frantisekmasa.wfrp_master.core.domain.party.PartyId
import java.util.*

interface Compendium<T : CompendiumItem> {

    fun liveForParty(partyId: PartyId): Flow<List<T>>

    /**
     * @throws CompendiumItemNotFound
     */
    suspend fun getItem(partyId: PartyId, itemId: UUID): T

    /**
     * Updates item if it exists and creates it if it doesn't
     */
    suspend fun saveItems(partyId: PartyId, vararg items: T)

    /**
     * Removes item if it's persisted,
     * otherwise does nothing.
     */
    suspend fun remove(partyId: PartyId, item: T)
}