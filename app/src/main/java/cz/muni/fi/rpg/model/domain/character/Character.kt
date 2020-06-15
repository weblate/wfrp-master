package cz.muni.fi.rpg.model.domain.character

import cz.muni.fi.rpg.model.domain.common.Ambitions
import cz.muni.fi.rpg.model.domain.common.Money
import java.lang.IllegalArgumentException

data class Character(
    private var name: String,
    val userId: String,
    private var career: String,
    private var socialClass: String,
    private var race: Race,
    private var stats: Stats,
    private var maxStats: Stats,
    private var points: Points,
    private var ambitions: Ambitions = Ambitions("", "")
) {
    companion object {
        const val NAME_MAX_LENGTH = 50
        const val CAREER_MAX_LENGTH = 50
        const val SOCIAL_CLASS_MAX_LENGTH = 50
    }

    private var money: Money = Money.zero()

    init {
        require(listOf(name, userId, career).all { it.isNotBlank() })
        require(name.length <= NAME_MAX_LENGTH) { "Character name is too long" }
        require(career.length <= CAREER_MAX_LENGTH) { "Career is too long" }
        require(socialClass.length <= SOCIAL_CLASS_MAX_LENGTH) { "Social class is too long" }
    }

    fun update(
        name: String,
        career: String,
        socialClass: String,
        race: Race,
        stats: Stats,
        maxStats: Stats,
        points: Points
    ) {
        require(listOf(name, career).all { it.isNotBlank() })
        require(name.length <= NAME_MAX_LENGTH) { "Character name is too long" }
        require(career.length <= CAREER_MAX_LENGTH) { "Career is too long" }
        require(socialClass.length <= SOCIAL_CLASS_MAX_LENGTH) { "Social class is too long" }
        require(stats.allSmallerThan(maxStats)) { "Stats cannot be larger than max stats" }
        this.name = name
        this.career = career
        this.socialClass = socialClass
        this.race = race
        this.stats = stats
        this.maxStats = maxStats
        this.points = points
    }

    fun addMoney(amount: Money) {
        money += amount
    }

    /**
     * @throws NotEnoughMoney
     */
    fun subtractMoney(amount: Money) {
        try {
            money -= amount
        } catch (e: IllegalArgumentException) {
            throw NotEnoughMoney(amount, e)
        }
    }

    fun getName(): String = name

    fun getCareer(): String = career

    fun getRace(): Race = race

    fun getSocialClass(): String = socialClass

    fun getMoney() = money

    fun updatePoints(newPoints: Points) {
        points = newPoints
    }

    fun getPoints(): Points = points

    fun getStats(): Stats = stats

    fun updateAmbitions(ambitions: Ambitions) {
        this.ambitions = ambitions
    }

    fun getAmbitions(): Ambitions = ambitions
}
