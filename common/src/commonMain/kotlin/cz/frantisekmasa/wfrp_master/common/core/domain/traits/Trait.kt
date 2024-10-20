package cz.frantisekmasa.wfrp_master.common.core.domain.traits

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.benasher44.uuid.Uuid
import cz.frantisekmasa.wfrp_master.common.character.effects.CharacterEffect
import cz.frantisekmasa.wfrp_master.common.character.effects.CharacteristicChange
import cz.frantisekmasa.wfrp_master.common.character.effects.ConstructWoundsModification
import cz.frantisekmasa.wfrp_master.common.character.effects.SizeChange
import cz.frantisekmasa.wfrp_master.common.character.effects.SwarmWoundsModification
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterItem
import cz.frantisekmasa.wfrp_master.common.core.shared.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import cz.frantisekmasa.wfrp_master.common.compendium.domain.Trait as CompendiumTrait

@Parcelize
@Serializable
@Immutable
data class Trait(
    @Contextual override val id: Uuid,
    @Contextual override val compendiumId: Uuid,
    val name: String,
    val specificationValues: Map<String, String>,
    val description: String,
) : CharacterItem<Trait, CompendiumTrait> {

    @Stable
    val evaluatedName get(): String = specificationValues
        .toList()
        .fold(name) { name, (search, replacement) ->
            val lastIndex = name.lastIndexOf(search)

            if (lastIndex != -1)
                name.replaceRange(lastIndex, lastIndex + search.length, replacement)
            else name
        }

    @Stable
    override val effects: List<CharacterEffect> get() {
        val name = evaluatedName.trim()

        return listOfNotNull(
            SizeChange.fromTraitNameOrNull(name),
            CharacteristicChange.fromTraitNameOrNull(name),
            SwarmWoundsModification.fromTraitNameOrNull(name),
            ConstructWoundsModification.fromTraitNameOrNull(name),
        )
    }

    override fun updateFromCompendium(compendiumItem: CompendiumTrait): Trait {
        if (compendiumItem.specifications != specificationValues.keys) {
            return this // TODO: Unlink from compendium item
        }

        return copy(
            name = compendiumItem.name,
            description = compendiumItem.description,
        )
    }

    override fun unlinkFromCompendium() = this // TODO: Unlink from compendium item

    init {
        require(specificationValues.keys.all { name.contains(it) })
        require(name.isNotEmpty())
        require(name.length <= CompendiumTrait.NAME_MAX_LENGTH) {
            "Maximum allowed name length is ${CompendiumTrait.NAME_MAX_LENGTH}"
        }
        require(description.length <= CompendiumTrait.DESCRIPTION_MAX_LENGTH) {
            "Maximum allowed description length is ${CompendiumTrait.DESCRIPTION_MAX_LENGTH}"
        }
    }
}
