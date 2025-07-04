package com.lyecdevelopers.worklist.presentation.worklist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.lyecdevelopers.worklist.presentation.patient.StartVisitScreen

@Composable
fun StartVisitDialog(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onStartVisit: () -> Unit,
    viewModel: WorklistViewModel = hiltViewModel(),
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                StartVisitScreen(
                    onStartVisit = { onStartVisit() },
                    onDiscard = {/**/ },
                    viewModel = viewModel
                )
            }
        }

    }
}