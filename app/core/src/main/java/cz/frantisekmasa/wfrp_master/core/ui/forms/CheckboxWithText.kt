package cz.frantisekmasa.wfrp_master.core.ui.forms

import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckboxWithText(
    text: String,
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable(
            onClick = { onCheckedChange(!checked) },
            interactionState = remember { InteractionState() },
            indication = null
        )
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Text(
            text,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
fun checkboxValue(default: Boolean): MutableState<Boolean> = savedInstanceState { default }