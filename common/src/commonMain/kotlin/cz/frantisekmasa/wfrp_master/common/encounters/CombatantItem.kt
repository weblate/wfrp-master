package cz.frantisekmasa.wfrp_master.common.encounters

import androidx.compose.runtime.Immutable
import cz.frantisekmasa.wfrp_master.common.core.auth.UserId
import cz.frantisekmasa.wfrp_master.common.core.domain.Stats
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CurrentConditions
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.NpcId
import cz.frantisekmasa.wfrp_master.common.core.domain.party.combat.Combatant
import cz.frantisekmasa.wfrp_master.common.encounters.domain.Wounds
import cz.frantisekmasa.wfrp_master.common.encounters.domain.Npc as NpcEntity

@Immutable
sealed class CombatantItem {
    abstract val combatant: Combatant
    abstract val name: String
    abstract val characteristics: Stats
    abstract val wounds: Wounds
    abstract val conditions: CurrentConditions

    fun areSameEntity(other: CombatantItem): Boolean = combatant.areSameEntity(other.combatant)

    @Immutable
    data class Character(
        val characterId: CharacterId,
        private val character: cz.frantisekmasa.wfrp_master.common.core.domain.character.Character,
        override val combatant: Combatant.Character,
    ) : CombatantItem() {

        val userId: UserId?
            get() = character.userId

        val avatarUrl: String?
            get() = character.avatarUrl

        override val name
            get() = combatant.name ?: character.name

        override val characteristics
            get() = character.characteristics

        override val wounds
            get() = combatant.wounds ?: character.wounds

        override val conditions: CurrentConditions
            get() = combatant.conditions ?: character.conditions

        val note: String get() = character.note
    }

    @Immutable
    data class Npc(
        val npcId: NpcId,
        private val npc: NpcEntity,
        override val combatant: Combatant.Npc,
    ) : CombatantItem() {
        override val name
            get() = combatant.name ?: npc.name

        override val characteristics
            get() = npc.stats

        override val wounds
            get() = combatant.wounds ?: npc.wounds

        override val conditions: CurrentConditions
            get() = combatant.conditions
    }
}
