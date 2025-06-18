package com.lyecdevelopers.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    confirmText: String = "Confirm",
    dismissText: String = "Dismiss",
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = "$dialogTitle Icon",
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = dialogTitle, style = MaterialTheme.typography.titleLarge, color = color,
            )
        },
        text = {
            Text(
                text = dialogText, style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmation) {
                Text(text = confirmText.uppercase())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = dismissText.uppercase())
            }
        },
    )
}


