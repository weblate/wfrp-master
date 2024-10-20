package cz.frantisekmasa.wfrp_master.common.character.skills

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.frantisekmasa.wfrp_master.common.Str
import cz.frantisekmasa.wfrp_master.common.character.CharacterScreenModel
import cz.frantisekmasa.wfrp_master.common.character.skills.dialog.AddBasicSkillsDialog
import cz.frantisekmasa.wfrp_master.common.character.skills.dialog.AddSkillDialog
import cz.frantisekmasa.wfrp_master.common.core.domain.Stats
import cz.frantisekmasa.wfrp_master.common.core.domain.skills.Skill
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.ui.buttons.CardButton
import cz.frantisekmasa.wfrp_master.common.core.ui.cards.CardContainer
import cz.frantisekmasa.wfrp_master.common.core.ui.cards.CardItem
import cz.frantisekmasa.wfrp_master.common.core.ui.cards.CardTitle
import cz.frantisekmasa.wfrp_master.common.core.ui.flow.collectWithLifecycle
import cz.frantisekmasa.wfrp_master.common.core.ui.menu.DropdownMenu
import cz.frantisekmasa.wfrp_master.common.core.ui.navigation.LocalNavigationTransaction
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.ContextMenu
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.EmptyUI
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun SkillsCard(
    characterScreenModel: CharacterScreenModel,
    skillsScreenModel: SkillsScreenModel,
    onRemove: (Skill) -> Unit,
) {
    val skills = skillsScreenModel.items.collectWithLifecycle(null).value ?: return
    val characteristics = characterScreenModel.character
        .collectWithLifecycle(null).value?.characteristics ?: return

    CardContainer(Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp)) {
        Column(Modifier.padding(horizontal = 6.dp)) {
            var basicSkillsDialogOpened by remember { mutableStateOf(false) }

            if (basicSkillsDialogOpened) {
                AddBasicSkillsDialog(
                    skillsScreenModel,
                    onDismissRequest = { basicSkillsDialogOpened = false }
                )
            }

            CardTitle(
                stringResource(Str.skills_title_skills),
                actions = {
                    var contextMenuExpanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { contextMenuExpanded = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            stringResource(Str.common_ui_label_open_context_menu),
                        )
                    }

                    DropdownMenu(
                        expanded = contextMenuExpanded,
                        onDismissRequest = { contextMenuExpanded = false },
                    ) {

                        DropdownMenuItem(
                            onClick = {
                                contextMenuExpanded = false
                                basicSkillsDialogOpened = true
                            },
                        ) {
                            Text(stringResource(Str.skills_button_import_basic_skills))
                        }
                    }
                },
            )

            if (skills.isEmpty()) {
                EmptyUI(
                    text = stringResource(Str.skills_messages_character_has_no_skills),
                    icon = Resources.Drawable.Skill,
                    size = EmptyUI.Size.Small
                )
            } else {
                val navigation = LocalNavigationTransaction.current

                for (skill in skills) {
                    SkillItem(
                        skill,
                        characteristics,
                        onClick = {
                            navigation.navigate(
                                CharacterSkillDetailScreen(
                                    skillsScreenModel.characterId,
                                    skill.id,
                                )
                            )
                        },
                        onRemove = { onRemove(skill) },
                    )
                }
            }

            var showAddSkillDialog by rememberSaveable { mutableStateOf(false) }

            CardButton(
                stringResource(Str.skills_title_add),
                onClick = { showAddSkillDialog = true },
            )

            if (showAddSkillDialog) {
                AddSkillDialog(
                    screenModel = skillsScreenModel,
                    characteristics = characteristics,
                    onDismissRequest = { showAddSkillDialog = false }
                )
            }
        }
    }
}

@Composable
private fun SkillItem(
    skill: Skill,
    characteristics: Stats,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    CardItem(
        skill.name,
        onClick = onClick,
        contextMenuItems = listOf(
            ContextMenu.Item(stringResource(Str.common_ui_button_remove), onClick = { onRemove() })
        ),
        badge = { TestNumber(skill, characteristics) }
    )
}

@Composable
private fun TestNumber(skill: Skill, characteristics: Stats) {
    val testNumber = skill.advances + skill.characteristic.characteristicValue(characteristics)

    Row {
        Text(testNumber.toString(), Modifier.padding(start = 4.dp))
    }
}
