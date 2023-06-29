package cz.frantisekmasa.wfrp_master.common.compendium.spell

import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cz.frantisekmasa.wfrp_master.common.compendium.CompendiumScreen
import cz.frantisekmasa.wfrp_master.common.compendium.VisibilityIcon
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.ui.navigation.LocalNavigationTransaction
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.EmptyUI
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.rememberScreenModel
import cz.frantisekmasa.wfrp_master.common.localization.LocalStrings

class SpellCompendiumScreen(
    private val partyId: PartyId
) : CompendiumScreen() {

    @Composable
    override fun Content() {
        val screenModel: SpellCompendiumScreenModel = rememberScreenModel(arg = partyId)
        var newSpellDialogOpened by rememberSaveable { mutableStateOf(false) }
        val navigation = LocalNavigationTransaction.current

        if (newSpellDialogOpened) {
            SpellDialog(
                spell = null,
                onDismissRequest = { newSpellDialogOpened = false },
                onSaveRequest = {
                    screenModel.createNew(it)
                    navigation.navigate(CompendiumSpellDetailScreen(partyId, it.id))
                },
            )
        }

        ItemsList(
            liveItems = screenModel.items,
            emptyUI = {
                val messages = LocalStrings.current.spells.messages
                EmptyUI(
                    text = messages.noSpellsInCompendium,
                    subText = messages.noSpellsInCompendiumSubtext,
                    icon = Resources.Drawable.Spell
                )
            },
            remover = screenModel::remove,
            newItemSaver = screenModel::createNew,
            onClick = { navigation.navigate(CompendiumSpellDetailScreen(partyId, it.id)) },
            onNewItemRequest = { newSpellDialogOpened = true },
            type = Type.SPELLS,
        ) { spell ->
            ListItem(
                icon = { SpellLoreIcon(spell.lore) },
                text = { Text(spell.name) },
                trailing = { VisibilityIcon(spell) },
            )
            Divider()
        }
    }
}
