package cz.muni.fi.rpg.model.domain.skills

import android.os.Parcelable
import cz.frantisekmasa.wfrp_master.core.domain.Characteristic
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Skill(
    val id: UUID,
    val compendiumId: UUID? = null,
    val advanced: Boolean,
    val characteristic: Characteristic,
    val name: String,
    val description: String,
    val advances: Int = 0
) : Parcelable {
    companion object {
        const val NAME_MAX_LENGTH = 50
        const val DESCRIPTION_MAX_LENGTH = 2000
    }

    init {
        require(name.isNotEmpty())
        require(advances >= 0)
        require(name.length <= NAME_MAX_LENGTH) { "Maximum allowed name length is $NAME_MAX_LENGTH" }
        require(description.length <= DESCRIPTION_MAX_LENGTH) { "Maximum allowed description length is $DESCRIPTION_MAX_LENGTH" }
    }
}
