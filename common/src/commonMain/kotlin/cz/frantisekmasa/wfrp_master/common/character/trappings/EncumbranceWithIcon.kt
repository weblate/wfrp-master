package cz.frantisekmasa.wfrp_master.common.character.trappings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cz.frantisekmasa.wfrp_master.common.Str
import cz.frantisekmasa.wfrp_master.common.core.domain.trappings.Encumbrance
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.shared.drawableResource
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun EncumbranceWithIcon(encumbrance: Encumbrance) {
    if (encumbrance == Encumbrance.Zero) {
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)
    ) {
        Icon(
            drawableResource(Resources.Drawable.TrappingEncumbrance),
            stringResource(Str.trappings_icon_encumbrance),
            Modifier.size(Spacing.medium),
        )
        Text(encumbrance.toString())
    }
}
