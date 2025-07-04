package com.lyecdevelopers.worklist.presentation.visit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyecdevelopers.core.data.local.entity.VisitEntity

@Composable
fun VisitCard(
    visit: VisitEntity,
    isCurrent: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (isCurrent) 4.dp else 1.dp,
        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("${visit.type} • ${visit.date}", style = MaterialTheme.typography.bodyLarge)
            Text("Status: ${visit.status}", style = MaterialTheme.typography.bodyMedium)
//            if (visit.notes.isNotBlank()) {
//                Spacer(Modifier.height(8.dp))
//                Text("Notes: ${visit.notes}", style = MaterialTheme.typography.bodySmall)
//            }
        }
    }
}
