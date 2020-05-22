package cz.muni.fi.rpg.model.firestore

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import cz.muni.fi.rpg.model.domain.character.Character
import cz.muni.fi.rpg.model.domain.character.CharacterNotFound
import cz.muni.fi.rpg.model.domain.character.CharacterRepository
import cz.muni.fi.rpg.model.infrastructure.GsonSnapshotParser
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class FirestoreCharacterRepository @Inject constructor(
    gson: Gson,
    firestore: FirebaseFirestore
) : CharacterRepository {
    private val parties = firestore.collection(COLLECTION_PARTIES)
    private val parser = GsonSnapshotParser(Character::class, gson)

    override suspend fun save(partyId: UUID, character: Character) {
        characters(partyId).document(character.userId).set(
            character,
            SetOptions.merge()
        ).await()
    }

    override suspend fun get(partyId: UUID, userId: String): Character {
        try {
            return parser.parseSnapshot(characters(partyId).document(userId).get().await())
        } catch (e: FirebaseFirestoreException) {
            throw CharacterNotFound(userId, partyId, e)
        }
    }

    override fun getLive(
        partyId: UUID,
        userId: String
    ): LiveData<Either<CharacterNotFound, Character>> {
        return DocumentLiveData(characters(partyId).document(userId)) {
            it.bimap({ e -> CharacterNotFound(userId, partyId, e) }, parser::parseSnapshot)
        }
    }

    override suspend fun hasCharacterInParty(userId: String, partyId: UUID): Boolean {
        return characters(partyId).whereEqualTo("userId", userId).get().await().size() != 0
    }

    override fun inParty(partyId: UUID): LiveData<List<Character>> =
        QueryLiveData(characters(partyId), parser)

    private fun characters(partyId: UUID) =
        parties.document(partyId.toString()).collection(COLLECTION_CHARACTERS)
}