/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.ui.theme.AdPreviewAppTheme
import com.chartboost.adpreviewapp.ui.theme.Dimens

@Composable
fun LoginScreen(
    email: String,
    loginState: LoginState,
    isEmailValid: Boolean,
    isLoginEnabled: Boolean,
    onEmailValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    errorType: LoginErrorType?,
    password: String,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val logoRes =
        if (isDarkTheme) R.drawable.chartboost_logo_dark_theme else R.drawable.chartboost_logo_light_theme

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(Dimens.paddingXL),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = stringResource(R.string.chartboost_logo),
                modifier =
                    Modifier
                        .fillMaxWidth(0.7f)
                        .padding(bottom = Dimens.paddingXL),
            )

            val isLoading = loginState == LoginState.InProgress
            val errorMessage =
                when (errorType) {
                    LoginErrorType.Network -> stringResource(R.string.error_network)
                    LoginErrorType.InvalidCredentials -> stringResource(R.string.error_wrong_credentials)
                    LoginErrorType.Unknown -> stringResource(R.string.error_unknown)
                    null -> null
                }

            EmailField(
                email = email,
                isEmailValid = isEmailValid,
                isLoading = isLoading,
                onEmailChange = onEmailValueChange,
            )

            Spacer(Modifier.height(Dimens.spacerM))

            PasswordField(
                password = password,
                isLoading = isLoading,
                errorMessage = errorMessage,
                isLoginEnabled = isLoginEnabled,
                onPasswordChange = onPasswordValueChange,
                onLoginClick = onLoginClick,
            )

            Spacer(Modifier.height(Dimens.spacerL))

            Button(
                onClick = onLoginClick,
                enabled = isLoginEnabled && loginState != LoginState.InProgress,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(Dimens.paddingXXXL),
            ) {
                if (loginState == LoginState.InProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimens.indicatorSizeS),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimens.paddingXS,
                    )
                } else {
                    Text(
                        stringResource(R.string.login_button),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.paddingL))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.signup_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.width(Dimens.spacerS))

                val signupText = stringResource(R.string.signup_link_text)
                val signupUrl = stringResource(R.string.signup_link_url)

                val signupAnnotated =
                    buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                signupUrl,
                                TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
                            ),
                        ) {
                            append(signupText)
                        }
                    }

                Text(
                    text = signupAnnotated,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        TermsOfServices(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars),
        )
    }
}

@Composable
private fun EmailField(
    email: String,
    isEmailValid: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val emailError = email.isNotBlank() && !isEmailValid

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(R.string.email_label)) },
        singleLine = true,
        enabled = !isLoading,
        isError = emailError,
        supportingText = {
            if (emailError) {
                Text(stringResource(R.string.invalid_email))
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions =
            KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordField(
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    isLoginEnabled: Boolean,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(R.string.password_label)) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = stringResource(R.string.toggle_password_visibility),
                )
            }
        },
        singleLine = true,
        enabled = !isLoading,
        isError = errorMessage != null,
        supportingText = {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = { if (isLoginEnabled) onLoginClick() },
            ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TermsOfServices(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
            text =
                buildAnnotatedString {
                    withLink(
                        LinkAnnotation.Url(
                            stringResource(R.string.privacy_policy_url),
                            TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
                        ),
                    ) {
                        append(stringResource(R.string.privacy_policy_text))
                    }
                },
        )

        Text(
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
            text =
                buildAnnotatedString {
                    withLink(
                        LinkAnnotation.Url(
                            stringResource(R.string.terms_conditions_url),
                            TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
                        ),
                    ) {
                        append(stringResource(R.string.terms_conditions_text))
                    }
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLoginScreen() {
    AdPreviewAppTheme {
        LoginScreen(
            email = "",
            loginState = LoginState.LoggedOut,
            isEmailValid = false,
            isLoginEnabled = false,
            onEmailValueChange = {},
            onPasswordValueChange = {},
            onLoginClick = {},
            errorType = LoginErrorType.Unknown,
            password = "password",
        )
    }
}

@Preview(showBackground = true, name = "Loading (InProgress)")
@Composable
private fun PreviewLoginScreenInProgress() {
    AdPreviewAppTheme {
        LoginScreen(
            email = "user@example.com",
            loginState = LoginState.InProgress,
            isEmailValid = true,
            isLoginEnabled = false,
            onEmailValueChange = {},
            onPasswordValueChange = {},
            onLoginClick = {},
            errorType = null,
            password = "password",
        )
    }
}
