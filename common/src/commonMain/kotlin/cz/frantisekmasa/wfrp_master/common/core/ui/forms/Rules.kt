package cz.frantisekmasa.wfrp_master.common.core.ui.forms

import androidx.compose.runtime.Composable
import cz.frantisekmasa.wfrp_master.common.Str
import dev.icerock.moko.resources.compose.stringResource

class Rules(private vararg val rules: Rule) : Rule {
    companion object {
        val NoRules = Rules()

        @Composable
        fun NotBlank() = rule(stringResource(Str.validation_not_blank)) { it.isNotBlank() }

        @Composable
        fun PositiveInteger() = rule(stringResource(Str.validation_positive_integer)) {
            it.toIntOrNull() != null && it.toInt() > 0
        }

        @Composable
        fun NonNegativeInteger() = rule(stringResource(Str.validation_non_negative)) {
            it.toIntOrNull() != null && it.toInt() >= 0
        }

        /**
         * This is useful usually for inputs that:
         * - Have small width that doesn't allow us to show full error message
         * - Do not have *unexpected* validation rules for user
         */
        fun withEmptyMessage(rule: Rule) = Rule { if (rule.errorMessage(it) != null) "" else null }
        fun withEmptyMessage(validate: (String) -> Boolean): Rule = CallbackRule("", validate)

        fun IfNotBlank(rule: Rule): Rule = Rule {
            if (it.isBlank()) {
                null
            } else {
                rule.errorMessage(it)
            }
        }
    }

    override fun errorMessage(value: String): String? {
        for (rule in rules) {
            val message = rule.errorMessage(value)

            if (message != null) {
                return message
            }
        }

        return null
    }
}

fun interface Rule {
    /**
     * Returns error message if text value is invalid or null if it's valid
     */
    fun errorMessage(value: String): String?
}

private data class CallbackRule(
    val errorMessage: String,
    val validate: (String) -> Boolean,
) : Rule {
    override fun errorMessage(value: String) = if (validate(value)) null else errorMessage
}

@Composable
fun rule(errorMessage: String, validate: (String) -> Boolean): Rule = CallbackRule(
    errorMessage,
    validate,
)
