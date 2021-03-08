package cz.muni.fi.rpg.ui.gameMaster

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cz.frantisekmasa.wfrp_master.core.domain.party.Party
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.model.domain.common.CouldNotConnectToBackend
import cz.frantisekmasa.wfrp_master.core.ui.buttons.CloseButton
import cz.frantisekmasa.wfrp_master.core.ui.dialogs.FullScreenDialog
import cz.frantisekmasa.wfrp_master.core.ui.forms.Rules
import cz.frantisekmasa.wfrp_master.core.ui.forms.TextInput
import cz.frantisekmasa.wfrp_master.core.ui.forms.inputValue
import cz.frantisekmasa.wfrp_master.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.core.ui.primitives.longToast
import cz.frantisekmasa.wfrp_master.core.ui.scaffolding.SaveAction
import cz.muni.fi.rpg.ui.partySettings.PartySettingsViewModel
import kotlinx.coroutines.*
import timber.log.Timber

@Composable
fun RenamePartyDialog(
    currentName: String,
    viewModel: PartySettingsViewModel,
    onDismissRequest: () -> Unit,
) {
    FullScreenDialog(onDismissRequest = onDismissRequest) {
        var validate by remember { mutableStateOf(false) }
        val newName = inputValue(currentName, Rules.NotBlank())

        Scaffold(
            topBar = {
                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current

                var saving by remember { mutableStateOf(false) }

                TopAppBar(
                    navigationIcon = { CloseButton(onClick = onDismissRequest) },
                    title = { Text(stringResource(R.string.title_party_rename)) },
                    actions = {
                        SaveAction(
                            enabled = !saving,
                            onClick = {
                                if (!newName.isValid()) {
                                    validate = true
                                    return@SaveAction
                                }

                                saving = true

                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        viewModel.renameParty(newName.value)
                                        longToast(context, R.string.message_party_updated)
                                        Timber.d("Party was renamed")

                                        withContext(Dispatchers.Main) { onDismissRequest() }

                                        return@launch
                                    } catch (e: CouldNotConnectToBackend) {
                                        Timber.i(
                                            e,
                                            "User could not rename party, because (s)he is offline"
                                        )
                                        longToast(context, R.string.error_party_update_no_connection)
                                    } catch (e: Throwable) {
                                        longToast(context, R.string.error_unkown)
                                        Timber.e(e)
                                    }

                                    saving = false
                                }
                            }
                        )
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.bodyPadding),
            ) {
                TextInput(
                    label = stringResource(R.string.label_party_name),
                    value = newName,
                    validate = validate,
                    maxLength = Party.NAME_MAX_LENGTH,
                )
            }
        }
    }
}
