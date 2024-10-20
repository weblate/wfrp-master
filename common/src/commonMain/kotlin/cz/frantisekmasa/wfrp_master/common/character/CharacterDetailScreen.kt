package cz.frantisekmasa.wfrp_master.common.character

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cz.frantisekmasa.wfrp_master.common.Str
import cz.frantisekmasa.wfrp_master.common.character.characteristics.CharacteristicsScreen
import cz.frantisekmasa.wfrp_master.common.character.combat.CharacterCombatScreen
import cz.frantisekmasa.wfrp_master.common.character.conditions.ConditionsScreen
import cz.frantisekmasa.wfrp_master.common.character.notes.NotesScreen
import cz.frantisekmasa.wfrp_master.common.character.religion.ReligionScreen
import cz.frantisekmasa.wfrp_master.common.character.skills.SkillsScreen
import cz.frantisekmasa.wfrp_master.common.character.spells.CharacterSpellsScreen
import cz.frantisekmasa.wfrp_master.common.character.trappings.TrappingsScreen
import cz.frantisekmasa.wfrp_master.common.characterCreation.CharacterCreationScreen
import cz.frantisekmasa.wfrp_master.common.characterEdit.CharacterEditScreen
import cz.frantisekmasa.wfrp_master.common.combat.ActiveCombatBanner
import cz.frantisekmasa.wfrp_master.common.core.LocalStaticConfiguration
import cz.frantisekmasa.wfrp_master.common.core.PartyScreenModel
import cz.frantisekmasa.wfrp_master.common.core.auth.LocalUser
import cz.frantisekmasa.wfrp_master.common.core.config.Platform
import cz.frantisekmasa.wfrp_master.common.core.domain.character.Character
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterTab
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterType
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.common.core.domain.localizedName
import cz.frantisekmasa.wfrp_master.common.core.domain.party.Party
import cz.frantisekmasa.wfrp_master.common.core.ui.buttons.HamburgerButton
import cz.frantisekmasa.wfrp_master.common.core.ui.flow.collectWithLifecycle
import cz.frantisekmasa.wfrp_master.common.core.ui.menu.DropdownMenu
import cz.frantisekmasa.wfrp_master.common.core.ui.menu.DropdownMenuItem
import cz.frantisekmasa.wfrp_master.common.core.ui.navigation.LocalNavigationTransaction
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.FullScreenProgress
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.rememberScreenModel
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.Breadcrumbs
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.IconAction
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.Subtitle
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.tabs.TabPager
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.tabs.TabPagerScope
import cz.frantisekmasa.wfrp_master.common.gameMaster.GameMasterScreen
import cz.frantisekmasa.wfrp_master.common.partyList.PartyListScreen
import dev.icerock.moko.resources.compose.stringResource

data class CharacterDetailScreen(
    private val characterId: CharacterId,
    private val comingFromCombat: Boolean = false,
    private val initialTab: CharacterTab = CharacterTab.values().first(),
) : Screen {

    override val key = "parties/$characterId"

    @Composable
    override fun Content() {
        val screenModel: CharacterScreenModel = rememberScreenModel(arg = characterId)
        val partyScreenModel: PartyScreenModel = rememberScreenModel(arg = characterId.partyId)

        val character = screenModel.character.collectWithLifecycle(null).value
        val party = partyScreenModel.party.collectWithLifecycle(null).value

        val navigation = LocalNavigationTransaction.current

        if (party == null || character == null) {
            SkeletonScaffold()
            return
        }

        val hiddenTabs = character.hiddenTabs
        val tabs = remember(hiddenTabs) { CharacterTab.values().filterNot { it in hiddenTabs } }

        var currentTab by rememberSaveable(tabs) {
            mutableStateOf(
                if (initialTab in tabs)
                    initialTab
                else tabs.firstOrNull()
            )
        }

        val userId = LocalUser.current.id
        val isGameMaster = party.gameMasterId == null || party.gameMasterId == userId

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { HamburgerButton() },
                    title = {
                        CharacterTitle(
                            party = party,
                            character = character,
                            screenModel = screenModel,
                            currentTab = currentTab,
                            isGameMaster = isGameMaster,
                        )
                    },
                    actions = {
                        IconAction(
                            Icons.Rounded.Edit,
                            stringResource(Str.character_title_edit),
                            onClick = { navigation.navigate(CharacterEditScreen(characterId)) },
                        )
                    }
                )
            }
        ) {
            MainContainer(
                character = character,
                party = party,
                isGameMaster = isGameMaster,
                screenModel = screenModel,
                tabs = tabs,
                onTabChange = { currentTab = it },
            )
        }
    }

    @Composable
    private fun CharacterTitle(
        party: Party,
        character: Character,
        screenModel: CharacterScreenModel,
        currentTab: CharacterTab?,
        isGameMaster: Boolean,
    ) {
        val characterPickerScreenModel: CharacterPickerScreenModel =
            rememberScreenModel(arg = party.id)
        val userId = LocalUser.current.id
        val canAddCharacters = !isGameMaster
        val navigation = LocalNavigationTransaction.current

        val allCharacters = remember {
            if (isGameMaster)
                screenModel.allCharacters
            else characterPickerScreenModel.allUserCharacters(userId)
        }.collectWithLifecycle(null).value

        val unassignedCharacters = characterPickerScreenModel.unassignedPlayerCharacters
            .collectWithLifecycle(null).value

        if (
            allCharacters != null &&
            unassignedCharacters != null &&
            (allCharacters.isNotEmpty() || canAddCharacters)
        ) {
            var unassignedCharactersDialogOpened by remember { mutableStateOf(false) }

            if (unassignedCharactersDialogOpened) {
                UnassignedCharacterPickerDialog(
                    partyId = party.id,
                    unassignedCharacters = unassignedCharacters,
                    screenModel = characterPickerScreenModel,
                    onDismissRequest = { unassignedCharactersDialogOpened = false },
                    onAssigned = {
                        navigation.replace(
                            CharacterDetailScreen(
                                characterId = it,
                                comingFromCombat = comingFromCombat,
                                initialTab = currentTab ?: CharacterTab.values().first(),
                            )
                        )
                        unassignedCharactersDialogOpened = false
                    }
                )
            }

            var dropdownOpened by remember { mutableStateOf(false) }

            Row(
                modifier = if (allCharacters.size > 1 || canAddCharacters)
                    Modifier.clickable { dropdownOpened = true }
                else Modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(character.name)
                    Subtitle(party.name)
                }

                if (allCharacters.size > 1 || canAddCharacters) {
                    Icon(Icons.Rounded.ExpandMore, null)
                }
            }

            DropdownMenu(
                dropdownOpened,
                onDismissRequest = { dropdownOpened = false }
            ) {
                allCharacters.forEach { otherCharacter ->
                    key(otherCharacter.id) {
                        DropdownMenuItem(
                            onClick = {
                                if (otherCharacter.id != character.id) {
                                    navigation.replace(
                                        CharacterDetailScreen(
                                            characterId = CharacterId(party.id, otherCharacter.id),
                                            comingFromCombat = comingFromCombat,
                                            initialTab = currentTab ?: CharacterTab.values()
                                                .first(),
                                        )
                                    )
                                } else {
                                    dropdownOpened = false
                                }
                            },
                        ) {
                            Text(otherCharacter.name)
                        }
                    }
                }

                if (canAddCharacters) {
                    if (unassignedCharacters.isNotEmpty()) {
                        DropdownMenuItem(
                            onClick = {
                                dropdownOpened = false
                                unassignedCharactersDialogOpened = true
                            },
                        ) {
                            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(24.dp))
                            Text(stringResource(Str.character_button_link))
                        }
                    }

                    DropdownMenuItem(
                        onClick = {
                            navigation.navigate(
                                CharacterCreationScreen(
                                    party.id,
                                    CharacterType.PLAYER_CHARACTER,
                                    userId,
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(24.dp))
                        Text(stringResource(Str.character_button_add))
                    }
                }
            }
        } else {
            Column {
                Text(character.name)
                Subtitle(party.name)
            }
        }
    }

    @Composable
    private fun MainContainer(
        character: Character?,
        party: Party?,
        isGameMaster: Boolean,
        screenModel: CharacterScreenModel,
        tabs: List<CharacterTab>,
        onTabChange: (CharacterTab) -> Unit,
    ) {
        if (character == null || party == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            return
        }

        Column(Modifier.fillMaxSize()) {
            if (LocalStaticConfiguration.current.platform == Platform.Desktop) {
                val userId = LocalUser.current.id

                val titleParties = stringResource(Str.parties_title_parties)
                Breadcrumbs {
                    level(titleParties) { PartyListScreen }

                    if (party.gameMasterId == userId) {
                        level(party.name) { GameMasterScreen(party.id) }
                    }

                    level(character.name)
                }
            }

            if (!comingFromCombat) {
                // Prevent long and confusing back stack when user goes i.e.
                // combat -> character detail -> combat
                ActiveCombatBanner(party)
            }

            if (tabs.isEmpty()) {
                val navigation = LocalNavigationTransaction.current

                LaunchedEffect(Unit) {
                    navigation.navigate(
                        CharacterEditScreen(characterId, CharacterEditScreen.Section.VISIBLE_TABS)
                    )
                }
                return@Column
            }

            TabPager(
                Modifier.weight(1f),
                initialPage = remember(tabs) {
                    tabs.indices.firstOrNull { tabs[it] == initialTab } ?: 0
                },
                onPageChange = { onTabChange(tabs[it]) },
            ) {
                val modifier = Modifier.width(screenWidth)

                tabs.forEach {
                    tab(it, character, party, modifier, screenModel)
                }
            }
        }
    }

    private fun TabPagerScope.tab(
        tab: CharacterTab,
        character: Character,
        party: Party,
        modifier: Modifier,
        screenModel: CharacterScreenModel,
    ) {
        tab(name = { tab.localizedName }) {
            when (tab) {
                CharacterTab.ATTRIBUTES -> {
                    CharacteristicsScreen(
                        character = character,
                        screenModel = rememberScreenModel(arg = characterId),
                        characterScreenModel = screenModel,
                        modifier = modifier,
                        characterId = characterId,
                        party = party,
                    )
                }
                CharacterTab.COMBAT -> {
                    CharacterCombatScreen(
                        characterId = characterId,
                        screenModel = rememberScreenModel(arg = characterId),
                        modifier = modifier,
                    )
                }
                CharacterTab.CONDITIONS -> {
                    ConditionsScreen(
                        character = character,
                        screenModel = screenModel,
                        modifier = modifier,
                    )
                }
                CharacterTab.SKILLS_AND_TALENTS -> {
                    SkillsScreen(
                        screenModel = screenModel,
                        skillsScreenModel = rememberScreenModel(arg = characterId),
                        talentsScreenModel = rememberScreenModel(arg = characterId),
                        traitsScreenModel = rememberScreenModel(arg = characterId),
                        modifier = modifier,
                    )
                }
                CharacterTab.SPELLS -> {
                    CharacterSpellsScreen(
                        screenModel = rememberScreenModel(arg = characterId),
                        modifier = modifier,
                    )
                }
                CharacterTab.RELIGION -> {
                    ReligionScreen(
                        modifier = modifier,
                        character = character,
                        updateCharacter = screenModel::update,
                        blessingsScreenModel = rememberScreenModel(arg = characterId),
                        miraclesScreenModel = rememberScreenModel(arg = characterId)
                    )
                }
                CharacterTab.TRAPPINGS -> {
                    TrappingsScreen(
                        characterId = characterId,
                        screenModel = rememberScreenModel(arg = characterId),
                        modifier = modifier,
                    )
                }
                CharacterTab.NOTES -> {
                    NotesScreen(
                        character = character,
                        screenModel = screenModel,
                        party = party,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonScaffold() {
    Scaffold(topBar = { TopAppBar { } }) {
        FullScreenProgress()
    }
}
