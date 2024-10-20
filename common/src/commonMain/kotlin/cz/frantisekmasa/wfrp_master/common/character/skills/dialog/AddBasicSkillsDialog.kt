package cz.frantisekmasa.wfrp_master.common.character.skills.dialog

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cz.frantisekmasa.wfrp_master.common.Str
import cz.frantisekmasa.wfrp_master.common.character.skills.SkillsScreenModel
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.LocalPersistentSnackbarHolder
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import cz.frantisekmasa.wfrp_master.common.compendium.domain.Skill as CompendiumSkill

@Composable
fun AddBasicSkillsDialog(
    skillsScreenModel: SkillsScreenModel,
    onDismissRequest: () -> Unit,
) {
    var saving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val (skills, setSkills) = remember {
        mutableStateOf<List<CompendiumSkill>?>(null)
    }

    LaunchedEffect(Unit) {
        setSkills(skillsScreenModel.getBasicSkillsToImport())
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val snackbarHolder = LocalPersistentSnackbarHolder.current
            val basicSkillsAddedMessage = stringResource(Str.skills_messages_basic_skills_added)

            TextButton(
                enabled = !skills.isNullOrEmpty() && !saving,
                onClick = {
                    if (skills == null) {
                        return@TextButton
                    }

                    saving = true

                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            skillsScreenModel.addBasicSkills(skills)

                            snackbarHolder.showSnackbar(basicSkillsAddedMessage)
                            onDismissRequest()
                        }
                    }
                }
            ) {
                Text(stringResource(Str.common_ui_button_ok).uppercase())
            }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismissRequest,
            ) {
                Text(stringResource(Str.common_ui_button_cancel).uppercase())
            }
        },
        title = { Text(stringResource(Str.skills_add_basic_skills_dialog_title)) },
        text = {
            Box(Modifier.animateContentSize()) {
                if (skills == null || saving) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else if (skills.isEmpty()) {
                    Text(stringResource(Str.skills_messages_no_basic_skills_to_add))
                } else {
                    Text(stringResource(Str.skills_messages_add_basic_skills_explanation, skills.size))
                }
            }
        }
    )
}
