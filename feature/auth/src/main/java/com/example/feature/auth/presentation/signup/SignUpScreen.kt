package com.example.feature.auth.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.presentation.bottomGlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = koinViewModel(),
    onGoogleSignUpClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = com.example.core.R.color.background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .bottomGlow(16.dp)
                    .background(
                        color = colorResource(id = com.example.core.R.color.accent),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📚",
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Librarease",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = com.example.core.R.color.primary)
            )

            Text(
                text = "Your gateway to endless stories",
                fontSize = 14.sp,
                color = colorResource(id = com.example.core.R.color.secondary),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(colorResource(id = com.example.core.R.color.surface_light))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = com.example.core.R.color.primary),
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sign up to start your reading journey",
                        fontSize = 14.sp,
                        color = colorResource(id = com.example.core.R.color.secondary)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = com.example.core.R.color.primary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.onNameChanged()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = uiState.nameError != null,
                        supportingText = {
                            if (uiState.nameError != null) {
                                Text(text = uiState.nameError ?: "")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = com.example.core.R.color.accent),
                            unfocusedBorderColor = colorResource(id = com.example.core.R.color.surface_light)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = com.example.core.R.color.primary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.onEmailChanged()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = uiState.emailError != null,
                        supportingText = {
                            if (uiState.emailError != null) {
                                Text(text = uiState.emailError ?: "")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = com.example.core.R.color.accent),
                            unfocusedBorderColor = colorResource(id = com.example.core.R.color.surface_light)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = com.example.core.R.color.primary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.onPasswordChanged()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = uiState.passwordError != null,
                        supportingText = {
                            if (uiState.passwordError != null) {
                                Text(text = uiState.passwordError ?: "")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = com.example.core.R.color.accent),
                            unfocusedBorderColor = colorResource(id = com.example.core.R.color.surface_light)
                        )
                    )

                    if (uiState.generalError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.generalError ?: "",
                            color = Color(0xFFB00020),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.signUp(name, email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bottomGlow(16.dp, shape = RoundedCornerShape(16.dp))
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = com.example.core.R.color.accent)
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = colorResource(id = com.example.core.R.color.border)
                        )
                        Text(
                            text = "OR",
                            fontSize = 12.sp,
                            color = colorResource(id = com.example.core.R.color.secondary),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = colorResource(id = com.example.core.R.color.border)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 14.sp,
                            color = colorResource(id = com.example.core.R.color.secondary)
                        )
                        Text(
                            text = "Sign In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = com.example.core.R.color.primary),
                            modifier = Modifier.clickable { onSignInClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenPreview() {
    SignUpScreen()
}
