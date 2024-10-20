package cz.frantisekmasa.wfrp_master.common.npcs

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.screen.Screen
import cz.frantisekmasa.wfrp_master.common.Str
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.EncounterId
import cz.frantisekmasa.wfrp_master.common.core.shared.IO
import cz.frantisekmasa.wfrp_master.common.core.ui.buttons.BackButton
import cz.frantisekmasa.wfrp_master.common.core.ui.navigation.LocalNavigationTransaction
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.rememberScreenModel
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.SaveAction
import cz.frantisekmasa.wfrp_master.common.encounters.EncounterDetailScreenModel
import cz.frantisekmasa.wfrp_master.common.encounters.domain.Wounds
import cz.frantisekmasa.wfrp_master.common.npcs.form.FormData
import cz.frantisekmasa.wfrp_master.common.npcs.form.NpcForm
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NpcCreationScreen(
    private val encounterId: EncounterId,
) : Screen {
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val viewModel: EncounterDetailScreenModel = rememberScreenModel(arg = encounterId)

        val data = FormData.empty()
        val validate = remember { mutableStateOf(false) }
        val submitEnabled = remember { mutableStateOf(true) }

        Scaffold(
            topBar = {
                val navigation = LocalNavigationTransaction.current

                TopBar(
                    stringResource(Str.npcs_title_add),
                    onSave = {
                        if (!data.isValid()) {
                            validate.value = true
                        } else {
                            submitEnabled.value = false
                            coroutineScope.launch(Dispatchers.IO) {
                                coroutineScope.launch {
                                    viewModel.addNpc(
                                        name = data.name.value,
                                        note = data.note.value,
                                        wounds = Wounds.fromMax(data.wounds.value.toInt()),
                                        stats = data.characteristics.toCharacteristics(),
                                        armor = data.armor.toArmor(),
                                        enemy = data.enemy.value,
                                        alive = data.alive.value,
                                        traits = emptyList(),
                                        trappings = emptyList(),
                                    )

                                    navigation.goBack()
                                }
                            }
                        }
                    },
                    actionsEnabled = submitEnabled.value,
                )
            }
        ) {
            NpcForm(data, validate = validate.value)
        }
    }

    @Composable
    private fun TopBar(
        title: String,
        onSave: () -> Unit,
        actionsEnabled: Boolean,
    ) {
        TopAppBar(
            navigationIcon = { BackButton() },
            title = { Text(title) },
            actions = { SaveAction(onClick = onSave, enabled = actionsEnabled) }
        )
    }
}
