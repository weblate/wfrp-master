package cz.frantisekmasa.wfrp_master.common.core.firebase.repositories

import com.benasher44.uuid.Uuid
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterItem
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterItemRepository
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.common.core.firebase.AggregateMapper
import cz.frantisekmasa.wfrp_master.common.core.firebase.Schema
import cz.frantisekmasa.wfrp_master.common.core.firebase.documents
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.Firestore
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.SetOptions
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

open class FirestoreCharacterItemRepository<T : CharacterItem<T, *>>(
    private val collectionName: String,
    protected val mapper: AggregateMapper<T>,
    private val firestore: Firestore,
) : CharacterItemRepository<T> {

    override fun findAllForCharacter(characterId: CharacterId): Flow<List<T>> =
        itemCollection(characterId)
            .orderBy("name")
            .documents(mapper)

    override suspend fun remove(characterId: CharacterId, itemId: Uuid) {
        itemCollection(characterId)
            .document(itemId.toString())
            .delete()
    }

    override fun remove(transaction: Transaction, characterId: CharacterId, itemId: Uuid) {
        transaction.delete(itemCollection(characterId).document(itemId.toString()))
    }

    override suspend fun save(characterId: CharacterId, item: T) {
        val data = mapper.toDocumentData(item)

        itemCollection(characterId)
            .document(item.id.toString())
            .set(data, SetOptions.mergeFields(data.keys))
    }

    override fun save(
        transaction: Transaction,
        characterId: CharacterId,
        item: T
    ) {
        val data = mapper.toDocumentData(item)

        transaction.set(
            itemCollection(characterId).document(item.id.toString()),
            data,
            SetOptions.mergeFields(data.keys),
        )
    }

    override suspend fun findByCompendiumId(
        partyId: PartyId,
        compendiumItemId: Uuid
    ): List<Pair<CharacterId, T>> {
        return coroutineScope {
            firestore.collection(Schema.Parties)
                .document(partyId.toString())
                .collection(Schema.Characters)
                .whereEqualTo("archived", false)
                .get()
                .documents
                .map { character ->
                    async(Dispatchers.IO) {
                        val characterId = CharacterId(partyId, character.id)

                        itemCollection(characterId)
                            .whereEqualTo("compendiumId", compendiumItemId.toString())
                            .get()
                            .documents
                            .mapNotNull { it.data }
                            .map { characterId to mapper.fromDocumentData(it) }
                    }
                }.awaitAll()
                .flatten()
        }
    }

    protected fun itemCollection(characterId: CharacterId) =
        firestore.collection(Schema.Parties)
            .document(characterId.partyId.toString())
            .collection(Schema.Characters)
            .document(characterId.id)
            .collection(collectionName)
}
