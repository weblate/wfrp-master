package cz.frantisekmasa.wfrp_master.common.character

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.frantisekmasa.wfrp_master.common.Str
import cz.frantisekmasa.wfrp_master.common.compendium.domain.CompendiumItem
import cz.frantisekmasa.wfrp_master.common.core.CharacterItemScreenModel
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterItem
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.ui.buttons.CloseButton
import cz.frantisekmasa.wfrp_master.common.core.ui.flow.collectWithLifecycle
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.EmptyUI
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.ItemIcon
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.SearchableList
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun <A : CharacterItem<A, B>, B : CompendiumItem<B>> CompendiumItemChooser(
    title: String,
    icon: (@Composable (B) -> Resources.Drawable)? = null,
    customIcon: (@Composable (B) -> Unit)? = null,
    screenModel: CharacterItemScreenModel<A, B>,
    onSelect: suspend (B) -> Unit,
    onCustomItemRequest: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    customItemButtonText: String = "",
    emptyUiIcon: Resources.Drawable,
) {
    val compendiumItems =
        screenModel.notUsedItemsFromCompendium.collectWithLifecycle(null).value
    val totalCompendiumItemCount =
        screenModel.compendiumItemsCount.collectWithLifecycle(null).value

    val data by derivedStateOf {
        if (compendiumItems == null || totalCompendiumItemCount == null)
            SearchableList.Data.Loading
        else SearchableList.Data.Loaded(compendiumItems)
    }

    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        SearchableList(
            modifier = Modifier.weight(1f),
            data = data,
            navigationIcon = { CloseButton(onDismissRequest) },
            title = title,
            emptyUi = {
                EmptyUI(
                    icon = emptyUiIcon,
                    text = stringResource(Str.compendium_messages_no_items),
                    subText = if (totalCompendiumItemCount == 0)
                        stringResource(
                            Str.compendium_messages_no_items_in_compendium_subtext_player
                        )
                    else null,
                )
            },
            searchPlaceholder = stringResource(Str.compendium_search_placeholder),
            key = { it.id },
            searchableValue = { it.name },
        ) { item ->
            ListItem(
                modifier = Modifier.clickable(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) { onSelect(item) }
                    }
                ),
                icon = {
                    if (customIcon != null) {
                        customIcon(item)
                    } else if (icon != null) {
                        ItemIcon(icon(item), ItemIcon.Size.Small)
                    }
                },
                text = { Text(item.name) }
            )
        }

        if (onCustomItemRequest != null && customItemButtonText != "") {
            Surface(elevation = 8.dp) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.bodyPadding),
                    onClick = onCustomItemRequest,
                ) {
                    Text(customItemButtonText)
                }
            }
        }
    }
}
