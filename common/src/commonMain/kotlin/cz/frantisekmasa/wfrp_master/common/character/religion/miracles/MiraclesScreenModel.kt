package cz.frantisekmasa.wfrp_master.common.character.religion.miracles

import cz.frantisekmasa.wfrp_master.common.core.CharacterItemScreenModel
import cz.frantisekmasa.wfrp_master.common.core.auth.UserProvider
import cz.frantisekmasa.wfrp_master.common.core.domain.compendium.Compendium
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyRepository
import cz.frantisekmasa.wfrp_master.common.core.domain.religion.Miracle
import cz.frantisekmasa.wfrp_master.common.core.domain.religion.MiracleRepository
import cz.frantisekmasa.wfrp_master.common.compendium.domain.Miracle as CompendiumMiracle

class MiraclesScreenModel(
    characterId: CharacterId,
    repository: MiracleRepository,
    compendium: Compendium<CompendiumMiracle>,
    userProvider: UserProvider,
    partyRepository: PartyRepository,
) : CharacterItemScreenModel<Miracle, CompendiumMiracle>(
    characterId,
    repository,
    compendium,
    userProvider,
    partyRepository,
)
