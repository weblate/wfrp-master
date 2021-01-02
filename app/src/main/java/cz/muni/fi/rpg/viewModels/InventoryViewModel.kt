package cz.muni.fi.rpg.viewModels

import androidx.lifecycle.ViewModel
import cz.frantisekmasa.wfrp_master.core.domain.Armor
import cz.frantisekmasa.wfrp_master.core.domain.character.CharacterFeatureRepository
import cz.frantisekmasa.wfrp_master.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.core.domain.character.CharacterRepository
import cz.frantisekmasa.wfrp_master.core.domain.character.NotEnoughMoney
import cz.frantisekmasa.wfrp_master.core.domain.Money
import cz.muni.fi.rpg.model.domain.inventory.InventoryItem
import cz.muni.fi.rpg.model.domain.inventory.InventoryItemRepository
import cz.frantisekmasa.wfrp_master.core.utils.right
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val characterId: CharacterId,
    private val inventoryItems: InventoryItemRepository,
    private val armorRepository: CharacterFeatureRepository<Armor>,
    private val characters: CharacterRepository
) : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.IO) {
    val inventory: Flow<List<InventoryItem>> = inventoryItems.findAllForCharacter(characterId)

    val armor: Flow<Armor> = armorRepository.getLive(characterId).right()

    val money: Flow<Money> = characters.getLive(characterId).right().map { it.getMoney() }

    suspend fun addMoney(amount: Money) {
        val character = characters.get(characterId)
        try {
            character.addMoney(amount)
            characters.save(characterId.partyId, character)
        } catch (e: IllegalArgumentException) {
        }
    }

    /**
     * @throws NotEnoughMoney
     */
    suspend fun subtractMoney(amount: Money) {
        val character = characters.get(characterId)
        character.subtractMoney(amount)
        characters.save(characterId.partyId, character)
    }

    suspend fun saveInventoryItem(inventoryItem: InventoryItem) {
        inventoryItems.save(characterId, inventoryItem)
    }

    fun removeInventoryItem(inventoryItem: InventoryItem) = launch {
        inventoryItems.remove(characterId, inventoryItem.id)
    }

    fun updateArmor(armor: Armor) = launch {
        armorRepository.save(characterId, armor)
    }
}