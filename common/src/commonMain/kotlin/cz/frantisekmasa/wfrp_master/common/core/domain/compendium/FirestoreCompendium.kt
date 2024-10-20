package cz.frantisekmasa.wfrp_master.common.core.domain.compendium

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.benasher44.uuid.Uuid
import cz.frantisekmasa.wfrp_master.common.compendium.domain.CompendiumItem
import cz.frantisekmasa.wfrp_master.common.compendium.domain.exceptions.CompendiumItemNotFound
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.common.core.firebase.AggregateMapper
import cz.frantisekmasa.wfrp_master.common.core.firebase.Schema
import cz.frantisekmasa.wfrp_master.common.core.firebase.documents
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.Firestore
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.FirestoreException
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.SetOptions
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.Transaction
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreCompendium<T : CompendiumItem<T>>(
    private val collectionName: String,
    private val firestore: Firestore,
    private val mapper: AggregateMapper<T>,
) : Compendium<T>, CoroutineScope by CoroutineScope(Dispatchers.IO) {

    override fun liveForParty(partyId: PartyId): Flow<List<T>> =
        collection(partyId)
            .orderBy("name")
            .documents(mapper)

    override suspend fun getItem(partyId: PartyId, itemId: Uuid): T {
        try {
            val snapshot = collection(partyId).document(itemId.toString()).get()

            return snapshot.data?.let(mapper::fromDocumentData)
                ?: throw CompendiumItemNotFound(
                    "Compendium item $itemId was not found in collection $collectionName"
                )
        } catch (e: FirestoreException) {
            throw CompendiumItemNotFound(
                "Compendium item $itemId was not found in collection $collectionName",
                e
            )
        }
    }

    override fun getLive(partyId: PartyId, itemId: Uuid): Flow<Either<CompendiumItemNotFound, T>> {
        return collection(partyId).document(itemId.toString())
            .snapshots
            .map {
                it.fold(
                    { snapshot ->
                        snapshot.data?.let(mapper::fromDocumentData)?.right()
                            ?: CompendiumItemNotFound(null).left()
                    },
                    { error -> CompendiumItemNotFound(null, error).left() },
                )
            }
    }

    override suspend fun saveItems(partyId: PartyId, items: List<T>) {
        val itemsData = coroutineScope {
            items.map { it.id to async { mapper.toDocumentData(it) } }
                .map { (id, data) -> id to data.await() }
        }

        itemsData.chunked(MAX_BATCH_SIZE).forEach { chunk ->
            firestore.runTransaction { transaction ->
                chunk.forEach { (id, data) ->
                    Napier.d("Saving Compendium item $data to $collectionName compendium of party $partyId")

                    transaction.set(
                        collection(partyId).document(id.toString()),
                        data,
                        SetOptions.mergeFields(data.keys)
                    )
                }
            }
        }
    }

    override fun save(transaction: Transaction, partyId: PartyId, item: T) {
        val data = mapper.toDocumentData(item)

        transaction.set(
            collection(partyId).document(item.id.toString()),
            data,
            SetOptions.mergeFields(data.keys)
        )
    }

    override suspend fun remove(transaction: Transaction, partyId: PartyId, item: T) {
        transaction.delete(
            collection(partyId).document(item.id.toString()),
        )
    }

    private fun collection(partyId: PartyId) =
        firestore.collection(Schema.Parties)
            .document(partyId.toString())
            .collection(collectionName)

    companion object {
        private val MAX_BATCH_SIZE = 500
    }
}
