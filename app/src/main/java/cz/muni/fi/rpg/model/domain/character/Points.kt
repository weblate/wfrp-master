package cz.muni.fi.rpg.model.domain.character

import com.google.firebase.firestore.Exclude

data class Points(
   val insanity: Int,
   val fate: Int,
   val fortune: Int,
   val wounds: Int,
   val maxWounds: Int
) {
    init {
        require(insanity >= 0)
        require(fate >= 0)
        require(fortune in 0..fate)
        require(wounds in 0..maxWounds)
        require(maxWounds > 0)
    }

    @Exclude
    fun isHeavilyWounded() = wounds < 2;
}