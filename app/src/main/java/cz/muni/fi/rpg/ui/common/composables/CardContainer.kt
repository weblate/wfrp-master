package cz.muni.fi.rpg.ui.common.composables

import androidx.compose.foundation.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(Modifier.padding(vertical = 6.dp).then(modifier)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            shape = RoundedCornerShape(4.dp)
        ) {
            Box(Modifier.padding(vertical = 16.dp, horizontal = 8.dp), children = content)
        }
    }
}