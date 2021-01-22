package cz.muni.fi.rpg.ui.gameMaster

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import cz.frantisekmasa.wfrp_master.combat.ui.ActiveCombatBanner
import cz.muni.fi.rpg.R
import cz.frantisekmasa.wfrp_master.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.core.domain.identifiers.EncounterId
import cz.frantisekmasa.wfrp_master.core.ui.buttons.HamburgerButton
import cz.frantisekmasa.wfrp_master.core.ui.scaffolding.TopBarAction
import cz.frantisekmasa.wfrp_master.core.ui.scaffolding.tabs.TabContent
import cz.frantisekmasa.wfrp_master.core.ui.scaffolding.tabs.TabRow
import cz.frantisekmasa.wfrp_master.core.ui.scaffolding.tabs.TabScreen
import cz.frantisekmasa.wfrp_master.core.viewModel.viewModel
import cz.frantisekmasa.wfrp_master.core.domain.party.Party
import cz.frantisekmasa.wfrp_master.core.ui.scaffolding.tabs.rememberPagerState
import cz.muni.fi.rpg.ui.common.AdManager
import cz.muni.fi.rpg.ui.common.composables.*
import cz.muni.fi.rpg.ui.gameMaster.encounters.EncountersScreen
import cz.frantisekmasa.wfrp_master.navigation.Route
import cz.frantisekmasa.wfrp_master.navigation.Routing
import cz.muni.fi.rpg.viewModels.EncountersViewModel
import cz.muni.fi.rpg.viewModels.GameMasterViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GameMasterScreen(routing: Routing<Route.GameMaster>, adManager: AdManager) {
    val viewModel = ViewModel.GameMaster(routing.route.partyId)
    val party = viewModel.party.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { party?.let { Text(it.getName()) } },
                navigationIcon = { HamburgerButton() },
                actions = {
                    TopBarAction(
                        onClick = {
                            if (party == null) {
                                return@TopBarAction
                            }

                            routing.navigateTo(Route.PartySettings(party.id))
                        },
                    ) {
                        Icon(vectorResource(R.drawable.ic_settings))
                    }
                }
            )
        }
    ) {
        if (party == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            return@Scaffold
        }

        WithConstraints(Modifier.fillMaxSize()) {
            val screens = screens(
                viewModel,
                routing,
                Modifier
                    .width(maxWidth)
                    .padding(top = 6.dp)
            )
            val screenWidth = constraints.maxWidth.toFloat()

            Column(Modifier.fillMaxHeight()) {
                ActiveCombatBanner(partyId = party.id, routing = routing)

                val tabContentState = rememberPagerState(screenWidth, screens.size)

                TabRow(
                    screens,
                    pagerState = tabContentState,
                    fullWidthTabs = true,
                )

                TabContent(
                    item = party,
                    screens = screens,
                    state = tabContentState,
                    modifier = Modifier.weight(1f)
                )

                BannerAd(
                    unitId = stringResource(R.string.game_master_ad_unit_id),
                    adManager = adManager
                )
            }
        }
    }
}

@Composable
private fun screens(
    viewModel: GameMasterViewModel,
    routing: Routing<Route.GameMaster>,
    modifier: Modifier
): Array<TabScreen<Party>> {
    return arrayOf(
        TabScreen(R.string.title_characters) { party ->
            PartySummaryScreen(
                modifier = modifier,
                partyId = party.id,
                viewModel = viewModel,
                routing = routing,
                onCharacterOpenRequest = {
                    routing.navigateTo(Route.CharacterDetail(CharacterId(party.id, it.id)))
                },
                onCharacterCreateRequest = {
                    routing.navigateTo(Route.CharacterCreation(party.id, it))
                },
            )
        },
        TabScreen(R.string.title_calendar) { party ->
            CalendarScreen(
                party,
                modifier = modifier,
                viewModel = viewModel,
            )
        },
        TabScreen(R.string.title_encounters) { party ->
            val encountersViewModel: EncountersViewModel by viewModel { parametersOf(party.id) }
            EncountersScreen(
                partyId = party.id,
                viewModel = encountersViewModel,
                modifier = modifier,
                onEncounterClick = {
                    routing.navigateTo(Route.EncounterDetail(EncounterId(party.id, it.id)))
                },
            )
        },
    )
}