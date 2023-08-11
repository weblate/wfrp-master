package cz.frantisekmasa.wfrp_master.common.character.trappings.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.frantisekmasa.wfrp_master.common.Str
import cz.frantisekmasa.wfrp_master.common.core.domain.trappings.InventoryItem
import cz.frantisekmasa.wfrp_master.common.core.domain.trappings.TrappingType
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.common.core.ui.text.SingleLineTextValue
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ClothingOrAccessoryDetail(
    trapping: InventoryItem,
    clothingOrAccessory: TrappingType.ClothingOrAccessory,
    onSaveRequest: suspend (InventoryItem) -> Unit,
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        WornBar(trapping, clothingOrAccessory, onSaveRequest)

        Column(Modifier.padding(Spacing.bodyPadding)) {
            SingleLineTextValue(
                stringResource(Str.trappings_label_type),
                stringResource(Str.trappings_types_clothing_or_accessory),
            )

            EncumbranceBox(trapping)

            TrappingDescription(trapping)
        }
    }
}
