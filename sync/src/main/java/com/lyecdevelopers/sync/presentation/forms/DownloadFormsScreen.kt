package com.lyecdevelopers.sync.presentation.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.sync.presentation.SyncViewModel

@Composable
fun DownloadFormsScreen(
    viewModel: SyncViewModel = hiltViewModel(),
    onDownloadSelected: (List<Form>) -> Unit,
) {
    val searchQuery by viewModel.searchQuery
    val selectedFormIds by viewModel.selectedFormIds
    val forms = viewModel.formItems
    val isLoading by viewModel.isLoading


    val filteredForms = forms.filter {
        it.name?.contains(searchQuery, ignoreCase = true) ?: false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Download Forms",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::filterForms,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            label = { Text("Search Forms") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredForms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No forms found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredForms) { form ->
                    val isSelected = selectedFormIds.contains(form.uuid)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleFormSelection(form.uuid) }) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected, onCheckedChange = {
                                    viewModel.toggleFormSelection(form.uuid)
                                })
                            Text(
                                text = form.name ?: "Unnamed Form",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.onDownloadClick()
                },
                enabled = selectedFormIds.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Download Selected (${selectedFormIds.size})")
            }
        }
    }
}


