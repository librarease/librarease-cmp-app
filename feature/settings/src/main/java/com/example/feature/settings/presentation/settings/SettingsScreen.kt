package com.example.feature.settings.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.R

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    appVersion: String,
    onPushNotificationsChanged: (Boolean) -> Unit,
    onDueDateRemindersChanged: (Boolean) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onConsumeFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var activeDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.feedbackMessage, uiState.feedbackError) {
        val message = uiState.feedbackError ?: uiState.feedbackMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onConsumeFeedback()
        }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        if (uiState.feedbackMessage != null && showChangePasswordDialog) {
            showChangePasswordDialog = false
            newPassword = ""
            confirmPassword = ""
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colorResource(id = R.color.background),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = colorResource(id = R.color.surface_light),
                    contentColor = colorResource(id = R.color.primary)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.background))
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.primary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Manage your account, reminders, and app essentials.",
                fontSize = 14.sp,
                color = colorResource(id = R.color.secondary)
            )

            Spacer(modifier = Modifier.height(24.dp))
            ProfileCard(uiState = uiState)

            Spacer(modifier = Modifier.height(20.dp))
            SettingsSection(title = "Preferences") {
                SettingsToggleRow(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "Push notifications",
                    subtitle = "Get alerts for due dates and account activity.",
                    checked = uiState.pushNotificationsEnabled,
                    enabled = !uiState.isWorking,
                    onCheckedChange = onPushNotificationsChanged
                )
                HorizontalDivider(color = colorResource(id = R.color.border))
                SettingsToggleRow(
                    icon = Icons.Outlined.Timer,
                    title = "Reading reminders",
                    subtitle = "Stay on track with gentle borrowing reminders.",
                    checked = uiState.dueDateRemindersEnabled,
                    enabled = !uiState.isWorking,
                    onCheckedChange = onDueDateRemindersChanged
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "Security") {
                SettingsActionRow(
                    icon = Icons.Outlined.Lock,
                    title = "Change password",
                    subtitle = "Update your password to keep your account secure.",
                    enabled = !uiState.isWorking,
                    onClick = { showChangePasswordDialog = true }
                )
                HorizontalDivider(color = colorResource(id = R.color.border))
                SettingsActionRow(
                    icon = Icons.Outlined.Logout,
                    title = "Log out",
                    subtitle = "Sign out on this device.",
                    enabled = !uiState.isWorking,
                    onClick = { showSignOutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "Legal & support") {
                SettingsActionRow(
                    icon = Icons.Outlined.Policy,
                    title = "Terms & conditions",
                    subtitle = "Review the rules for using Librarease.",
                    enabled = true,
                    onClick = { activeDialog = "terms" }
                )
                HorizontalDivider(color = colorResource(id = R.color.border))
                SettingsActionRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy policy",
                    subtitle = "See how account and reading data are handled.",
                    enabled = true,
                    onClick = { activeDialog = "privacy" }
                )
                HorizontalDivider(color = colorResource(id = R.color.border))
                SettingsActionRow(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help & support",
                    subtitle = "Troubleshooting tips for sign-in and sync issues.",
                    enabled = true,
                    onClick = { activeDialog = "help" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "About") {
                SettingsInfoRow(
                    icon = Icons.Outlined.Info,
                    title = "App version",
                    value = appVersion
                )
                HorizontalDivider(color = colorResource(id = R.color.border))
                SettingsInfoRow(
                    icon = Icons.Outlined.PersonOutline,
                    title = "Theme",
                    value = "Library dark"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            DangerZoneCard(
                enabled = !uiState.isWorking,
                onDeleteClick = { showDeleteDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showChangePasswordDialog) {
        PasswordDialog(
            newPassword = newPassword,
            confirmPassword = confirmPassword,
            isSubmitting = uiState.isWorking,
            onNewPasswordChanged = { newPassword = it },
            onConfirmPasswordChanged = { confirmPassword = it },
            onDismiss = {
                showChangePasswordDialog = false
                newPassword = ""
                confirmPassword = ""
            },
            onConfirm = { onChangePassword(newPassword, confirmPassword) }
        )
    }

    if (showSignOutDialog) {
        ConfirmationDialog(
            title = "Log out?",
            message = "You can sign back in anytime with the same account.",
            confirmLabel = "Log out",
            confirmColor = colorResource(id = R.color.accent),
            isLoading = uiState.isWorking,
            onDismiss = { showSignOutDialog = false },
            onConfirm = {
                showSignOutDialog = false
                onSignOutClick()
            }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete account?",
            message = "This permanently removes your Librarease account from this device and cannot be undone.",
            confirmLabel = "Delete",
            confirmColor = Color(0xFFEF6C6C),
            isLoading = uiState.isWorking,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccountClick()
            }
        )
    }

    if (activeDialog != null) {
        LegalDialog(
            type = activeDialog.orEmpty(),
            onDismiss = { activeDialog = null }
        )
    }
}

@Composable
private fun ProfileCard(uiState: SettingsUiState) {
    val user = uiState.user
    val displayName = user?.name?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore("@")
        ?: "Reader"
    val initials = displayName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "R" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.surface_light),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(colorResource(id = R.color.accent), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = colorResource(id = R.color.primary),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = colorResource(id = R.color.primary),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (uiState.isLoadingUser) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = colorResource(id = R.color.accent),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = user?.email ?: "Signed in account",
                    color = colorResource(id = R.color.secondary),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = colorResource(id = R.color.primary),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.surface_light),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.border),
                    shape = RoundedCornerShape(22.dp)
                )
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = colorResource(id = R.color.accent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = colorResource(id = R.color.primary),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = colorResource(id = R.color.secondary),
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.primary),
                checkedTrackColor = colorResource(id = R.color.accent),
                uncheckedThumbColor = colorResource(id = R.color.secondary),
                uncheckedTrackColor = colorResource(id = R.color.border)
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = colorResource(id = R.color.accent)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colorResource(id = R.color.primary),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = colorResource(id = R.color.secondary),
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colorResource(id = R.color.secondary)
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = colorResource(id = R.color.accent)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = colorResource(id = R.color.primary),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = colorResource(id = R.color.secondary),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun DangerZoneCard(
    enabled: Boolean,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.surface_light),
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0x66EF6C6C),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Danger zone",
            color = Color(0xFFEF9A9A),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Delete your account and remove access on this device.",
            color = colorResource(id = R.color.secondary),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onDeleteClick)
                .background(
                    color = Color(0x22EF6C6C),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete account",
                tint = Color(0xFFEF9A9A)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Delete account",
                    color = Color(0xFFFFD7D7),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "This action cannot be undone.",
                    color = colorResource(id = R.color.secondary),
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFEF9A9A)
            )
        }
    }
}

@Composable
private fun PasswordDialog(
    newPassword: String,
    confirmPassword: String,
    isSubmitting: Boolean,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.surface_light),
        title = {
            Text(
                text = "Change password",
                color = colorResource(id = R.color.primary)
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose a new password with at least 8 characters.",
                    color = colorResource(id = R.color.secondary),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChanged,
                    label = { Text("New password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = dialogTextFieldColors()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChanged,
                    label = { Text("Confirm password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = dialogTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting
            ) {
                Text(if (isSubmitting) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    confirmColor: Color,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.surface_light),
        title = {
            Text(
                text = title,
                color = colorResource(id = R.color.primary)
            )
        },
        text = {
            Text(
                text = message,
                color = colorResource(id = R.color.secondary),
                fontSize = 13.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoading) "Please wait..." else confirmLabel,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", color = confirmColor)
            }
        }
    )
}

@Composable
private fun LegalDialog(
    type: String,
    onDismiss: () -> Unit
) {
    val title: String
    val body: String

    when (type) {
        "terms" -> {
            title = "Terms & conditions"
            body = "Use Librarease responsibly, keep your account details secure, and borrow content only through supported library access. Availability, borrowing limits, and returns may vary by library partner. Misuse, abusive activity, or attempts to interfere with the service can result in access being limited."
        }

        "privacy" -> {
            title = "Privacy policy"
            body = "Librarease stores the basics needed to sign you in and personalize your reading experience, such as your account profile and app preferences. Reading reminders and notification settings stay on your device. Production apps should replace this starter copy with the final policy approved for release."
        }

        else -> {
            title = "Help & support"
            body = "If something looks off, first check your network connection, then refresh the affected screen or sign out and back in. Password changes and account deletion may require a recent sign-in for security. If library content is missing, the issue may come from the library feed rather than your account."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.surface_light),
        title = {
            Text(
                text = title,
                color = colorResource(id = R.color.primary)
            )
        },
        text = {
            Text(
                text = body,
                color = colorResource(id = R.color.secondary),
                fontSize = 13.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = colorResource(id = R.color.accent),
    unfocusedBorderColor = colorResource(id = R.color.border),
    focusedTextColor = colorResource(id = R.color.primary),
    unfocusedTextColor = colorResource(id = R.color.primary),
    focusedLabelColor = colorResource(id = R.color.accent),
    unfocusedLabelColor = colorResource(id = R.color.secondary),
    cursorColor = colorResource(id = R.color.accent)
)
