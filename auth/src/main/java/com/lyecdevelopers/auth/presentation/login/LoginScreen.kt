package com.lyecdevelopers.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lyecdevelopers.auth.presentation.event.LoginEvent
import com.lyecdevelopers.auth.presentation.event.LoginUIEvent
import com.lyecdevelopers.core.ui.components.SectionTitle
import com.lyecdevelopers.core.ui.components.SubmitButton
import com.lyecdevelopers.core.ui.components.TextInputField
import com.lyecdevelopers.core.ui.theme.UgandaEMRMobileTheme


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val (passwordFocusRequester) = remember { FocusRequester.createRefs() }

    // Collect events like success or error
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUIEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is LoginUIEvent.ShowSuccess -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
            }
        }
    }

    // Helper function to update individual fields
    fun updateLoginField(username: String? = null, password: String? = null) {
        viewModel.onEvent(
            LoginEvent.Login(
                username = username ?: state.username,
                password = password ?: state.password
            )
        )
    }

    UgandaEMRMobileTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                SectionTitle(
                    text = "Login".uppercase(),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )


                Spacer(modifier = Modifier.height(24.dp))

                // Username
                TextInputField(
                    value = state.username,
                    onValueChange = { updateLoginField(username = it) },
                    label = "Username",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth(),
                    error = if (state.hasSubmitted && state.username.isBlank()) "Username required" else null,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                TextInputField(
                    value = state.password,
                    onValueChange = { updateLoginField(password = it) },
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    error = if (state.hasSubmitted && state.password.isBlank()) "Password required" else null,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.onEvent(LoginEvent.Submit) }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Optional: Forgot password
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            // TODO: navigate to password reset screen
                        }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit button
                SubmitButton(
                    text = "Submit",
                    onClick = { viewModel.onEvent(LoginEvent.Submit) },
                    isLoading = state.isLoading,
                    iconContentDescription = "Login icon",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    loadingIndicatorSize = 24.dp
                )
            }
        }
    }
}







