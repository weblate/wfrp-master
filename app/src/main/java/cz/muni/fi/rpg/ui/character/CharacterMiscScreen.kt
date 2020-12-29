package cz.muni.fi.rpg.ui.character

import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.model.domain.character.Character
import cz.frantisekmasa.wfrp_master.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.core.ui.viewinterop.fragmentManager
import cz.frantisekmasa.wfrp_master.core.viewModel.viewModel
import cz.muni.fi.rpg.model.domain.character.Points
import cz.muni.fi.rpg.ui.character.dialogs.ExperiencePointsDialog
import cz.muni.fi.rpg.ui.common.ChangeAmbitionsDialog
import cz.muni.fi.rpg.ui.common.composables.*
import cz.muni.fi.rpg.viewModels.CharacterMiscViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun CharacterMiscScreen(
    characterId: CharacterId,
    character: Character,
    modifier: Modifier = Modifier,
) {
    val viewModel: CharacterMiscViewModel by viewModel { parametersOf(characterId) }

    ScrollableColumn(modifier.background(MaterialTheme.colors.background)) {
        MainCard(character)

        ExperiencePointsCard(
            viewModel = viewModel,
            points = character.getPoints(),
        )

        val fragmentManager = fragmentManager()
        val ambitionsDialogTitle = stringResource(R.string.title_character_ambitions)

        AmbitionsCard(
            titleRes = R.string.title_character_ambitions,
            ambitions = character.getAmbitions(),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable(onClick = {
                    ChangeAmbitionsDialog
                        .newInstance(ambitionsDialogTitle, character.getAmbitions())
                        .setOnSaveListener { viewModel.updateCharacterAmbitions(it) }
                        .show(fragmentManager, "ChangeAmbitionsDialog")
                })
        )

        viewModel.party.collectAsState(null).value?.let {
            AmbitionsCard(
                titleRes = R.string.title_party_ambitions,
                ambitions = it.getAmbitions(),
                titleIconRes = R.drawable.ic_group,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(Modifier.padding(bottom = 20.dp))
    }
}

@Composable
private fun MainCard(character: Character) {
    Card {
        Column(Modifier.fillMaxWidth()) {
            CardTitle(character.getName())
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                SingleLineTextValue(
                    R.string.label_race,
                    stringResource(character.getRace().getReadableNameId())
                )
                SingleLineTextValue(R.string.label_career, character.getCareer())
                SingleLineTextValue(R.string.label_social_class, character.getSocialClass())
                MultiLineTextValue(R.string.label_psychology, character.getPsychology())
                MultiLineTextValue(R.string.label_motivation, character.getMotivation())
                MultiLineTextValue(R.string.label_character_note, character.getNote())
            }
        }
    }
}

@Composable
private fun MultiLineTextValue(@StringRes labelRes: Int, value: String) {
    if (value.isBlank()) return

    Column {
        Text(stringResource(labelRes), fontWeight = FontWeight.Bold)
        Text(value)
    }
}

@Composable
private fun SingleLineTextValue(@StringRes labelRes: Int, value: String) {
    if (value.isBlank()) return

    Row {
        Text(
            stringResource(labelRes) + ":",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(value)
    }
}

@Composable
private fun ExperiencePointsCard(points: Points, viewModel: CharacterMiscViewModel) {
    var experiencePointsDialogVisible by savedInstanceState { false }

    if (experiencePointsDialogVisible) {
        ExperiencePointsDialog(
            value = points,
            save = { viewModel.updatePoints(it) },
            onDismissRequest = { experiencePointsDialogVisible = false },
        )
    }

    Card(Modifier.clickable(onClick = { experiencePointsDialogVisible = true })) {
        SingleLineTextValue(R.string.xp_points, points.experience.toString())
    }
}

@Composable
private fun Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    CardContainer(Modifier.fillMaxWidth().padding(horizontal = 8.dp).then(modifier)) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            content()
        }
        R.drawable.common_google_signin_btn_icon_dark
    }
}
