package com.lyecdevelopers.worklist.presentation.visit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyecdevelopers.worklist.domain.model.Encounter

@Composable
fun EncounterCard(encounter: Encounter) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(encounter.encounterTypeUuid, style = MaterialTheme.typography.bodyMedium)
            Text(encounter.encounterDatetime.toString(), style = MaterialTheme.typography.bodySmall)

            if (encounter.observations.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                encounter.observations.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

