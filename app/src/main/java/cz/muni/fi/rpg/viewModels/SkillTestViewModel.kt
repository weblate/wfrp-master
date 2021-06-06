package cz.muni.fi.rpg.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import cz.frantisekmasa.wfrp_master.core.domain.compendium.Compendium
import cz.frantisekmasa.wfrp_master.core.domain.Stats
import cz.frantisekmasa.wfrp_master.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.core.domain.rolls.Dice
import cz.frantisekmasa.wfrp_master.core.domain.rolls.TestResult
import cz.frantisekmasa.wfrp_master.core.domain.character.Character
import cz.frantisekmasa.wfrp_master.core.domain.character.CharacterRepository
import cz.frantisekmasa.wfrp_master.core.domain.party.PartyId
import cz.muni.fi.rpg.model.domain.skills.Skill
import cz.muni.fi.rpg.model.domain.skills.SkillRepository
import cz.frantisekmasa.wfrp_master.compendium.domain.Skill as CompendiumSkill

class SkillTestViewModel(
    private val partyId: PartyId,
    skillCompendium: Compendium<CompendiumSkill>,
    characterRepository: CharacterRepository,
    private val characterSkills: SkillRepository,
): ViewModel() {
    val characters: LiveData<List<Character>> = characterRepository.inParty(partyId).asLiveData()
    val skills: LiveData<List<CompendiumSkill>> = skillCompendium.liveForParty(partyId).asLiveData()

    suspend fun performSkillTest(
        character: Character,
        compendiumSkill: CompendiumSkill,
        testModifier: Int
    ): TestResult? {
        val skill = characterSkills.findByCompendiumId(
            CharacterId(partyId, character.id),
            compendiumSkill.id
        )

        return basicTestedValue(compendiumSkill, skill, character.getCharacteristics())
            ?.let {
                TestResult(
                    rollValue = Dice(100).roll(),
                    testedValue = it + testModifier
                )
            }
    }

    private fun basicTestedValue(
        compendiumSkill: CompendiumSkill,
        characterSkill: Skill?,
        characteristics: Stats
    ): Int? {
        val characteristic = compendiumSkill.characteristic.characteristicValue(characteristics)

        // Character has at least one skill advance
        if (characterSkill != null) {
            return characteristic + characterSkill.advances
        }

        // Characters cannot use advanced skills that they don't have at least one advance in
        // see Basic and Advanced Skills on page 117 of Rulebook
        if (compendiumSkill.advanced) {
            return null
        }

        return characteristic
    }
}