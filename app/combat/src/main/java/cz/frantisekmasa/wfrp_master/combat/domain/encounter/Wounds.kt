package cz.frantisekmasa.wfrp_master.combat.domain.encounter

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

@Serializable
data class Wounds(
    val current: Int,
    val max: Int
) {
    fun restore(restored: Int): Wounds = copy(current = min(max, current + restored))
    fun lose(lost: Int): Wounds = copy(current = max(0, current - lost))

    companion object {
        fun fromMax(max: Int) = Wounds(max, max)
    }

    init {
        require(current >= 0)
        require(max > 0)
        require(current <= max)
    }
}
