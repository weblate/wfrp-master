package cz.frantisekmasa.wfrp_master.desktop.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cz.frantisekmasa.wfrp_master.common.Str
import cz.frantisekmasa.wfrp_master.common.auth.AuthenticationManager
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.ErrorMessage
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.Rules
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.TextInput
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.inputValue
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.FullScreenProgress
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.common.shell.SplashScreen
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.instance

@Composable
fun AuthenticationScreen() {
    val email = inputValue("", Rules.NotBlank())
    val password = inputValue("", Rules.NotBlank())

    val coroutineScope = rememberCoroutineScope()

    var validate by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    if (processing) {
        FullScreenProgress()
        return
    }

    Box {
        SplashScreen()

        Card(
            Modifier
                .width(400.dp)
                .align(Alignment.Center)
        ) {
            Column(Modifier.padding(Spacing.large)) {
                error?.let {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorMessage(it)
                    }
                }
                TextInput(
                    label = stringResource(Str.authentication_label_email),
                    value = email,
                    validate = validate
                )

                TextInput(
                    label = stringResource(Str.authentication_label_password),
                    value = password,
                    validate = validate,
                    visualTransformation = PasswordVisualTransformation(),
                )

                val auth: AuthenticationManager by localDI().instance()

                val errorInvalidEmail = stringResource(Str.authentication_messages_invalid_email)
                val errorEmailNotFound = stringResource(Str.authentication_messages_email_not_found)
                val errorInvalidPassword = stringResource(Str.authentication_messages_invalid_password)
                val errorUnknown = stringResource(Str.authentication_messages_unknown_error)

                Button(
                    onClick = {
                        validate = true

                        if (!email.isValid() || !password.isValid()) {
                            return@Button
                        }

                        processing = true

                        coroutineScope.launch(Dispatchers.IO) {
                            val result = auth.signIn(email.value, password.value)

                            if (result is AuthenticationManager.SignInResponse.Failure) {
                                error = when {
                                    result.isInvalidEmail -> errorInvalidEmail
                                    result.isEmailNotFound -> errorEmailNotFound
                                    result.isInvalidPassword -> errorInvalidPassword
                                    else -> errorUnknown
                                }

                                processing = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(Str.authentication_button_sign_in))
                }
            }
        }
    }
}
