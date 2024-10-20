package cz.frantisekmasa.wfrp_master.common.core.firebase.repositories

import arrow.core.left
import arrow.core.right
import com.benasher44.uuid.Uuid
import cz.frantisekmasa.wfrp_master.common.core.domain.character.Character
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterNotFound
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterRepository
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterType
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.common.core.firebase.AggregateMapper
import cz.frantisekmasa.wfrp_master.common.core.firebase.Schema
import cz.frantisekmasa.wfrp_master.common.core.firebase.documents
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.Firestore
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.FirestoreException
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.SetOptions
import cz.frantisekmasa.wfrp_master.common.firebase.firestore.Transaction
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreCharacterRepository(
    firestore: Firestore,
    private val mapper: AggregateMapper<Character>
) : CharacterRepository {
    private val parties = firestore.collection(Schema.Parties)

    override suspend fun save(partyId: PartyId, character: Character) {
        val data = mapper.toDocumentData(character)

        Napier.d("Saving character $data in party $partyId to firestore")
        characters(partyId)
            .document(character.id)
            .set(data, SetOptions.mergeFields(data.keys))
    }

    override fun save(
        transaction: Transaction,
        partyId: PartyId,
        character: Character
    ) {
        val data = mapper.toDocumentData(character)

        Napier.d("Saving character $data in party $partyId to firestore")

        transaction.set(
            characters(partyId).document(character.id),
            data,
            SetOptions.mergeFields(data.keys),
        )
    }

    override suspend fun get(characterId: CharacterId): Character {
        try {
            val snapshot = characters(characterId.partyId)
                .document(characterId.id)
                .get()

            if (snapshot.data == null) {
                throw CharacterNotFound(characterId)
            }

            return mapper.fromDocumentSnapshot(snapshot)
        } catch (e: FirestoreException) {
            throw CharacterNotFound(characterId, e)
        }
    }

    override fun getLive(characterId: CharacterId) =
        characters(characterId.partyId)
            .document(characterId.id)
            .snapshots
            .map {
                it.fold(
                    { snapshot ->
                        snapshot.data?.let(mapper::fromDocumentData)?.right()
                            ?: CharacterNotFound(characterId).left()
                    },
                    { error -> CharacterNotFound(characterId, error).left() },
                )
            }

    override suspend fun hasCharacterInParty(userId: String, partyId: PartyId): Boolean {
        return characters(partyId)
            .whereEqualTo("userId", userId)
            .get()
            .documents
            .isNotEmpty()
    }

    override suspend fun findByCompendiumCareer(
        partyId: PartyId,
        careerId: Uuid,
    ): List<Character> {
        return characters(partyId)
            .whereEqualTo("compendiumCareer.careerId", careerId.toString())
            .get()
            .documents
            .mapNotNull { it.data }
            .map(mapper::fromDocumentData)
    }

    override fun inParty(partyId: PartyId, types: Set<CharacterType>): Flow<List<Character>> {
        return characters(partyId)
            .whereEqualTo("archived", false)
            .whereIn("type", types.map { it.name })
            .orderBy("name")
            .documents(mapper)
    }

    private fun characters(partyId: PartyId) =
        parties.document(partyId.toString())
            .collection(Schema.Characters)
}
