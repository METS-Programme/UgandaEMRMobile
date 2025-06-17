package com.lyecdevelopers.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lyecdevelopers.core.model.cohort.Attribute
import com.lyecdevelopers.core.model.cohort.Indicator


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicatorAttributesScreen(
    selectedIndicator: Indicator?,
    availableParameters: List<Attribute>,
    selectedParameters: List<Attribute>,
    highlightedAvailable: List<Attribute>,
    highlightedSelected: List<Attribute>,
    toggleHighlightAvailable: (Attribute) -> Unit,
    toggleHighlightSelected: (Attribute) -> Unit,
    moveRight: () -> Unit,
    moveLeft: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val buttonSize = 48.dp
    val cardElevation = 8.dp
    val sectionSpacing = 24.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedIndicator?.label ?: "Choose Indicator",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
                actions = {
                    IconButton(onClick = { /* TODO: Collapse logic */ }) {
                        Icon(Icons.Default.KeyboardArrowUp, "Collapse", tint = colors.onSurface)
                    }
                    IconButton(onClick = { /* TODO: Expand logic */ }) {
                        Icon(Icons.Default.KeyboardArrowDown, "Expand", tint = colors.onSurface)
                    }
                })
        }, containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 500.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                // ✅ Scrollable content inside the Card
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
                            verticalAlignment = Alignment.Top // Align items to top inside scroll
                        ) {
                            // Left: Available Parameters
                            ParameterList(
                                title = "Available parameters",
                                parameters = availableParameters,
                                highlighted = highlightedAvailable,
                                onItemClick = toggleHighlightAvailable,
                                labelSelector = { attribute -> attribute.label.replaceFirstChar { it.uppercase() } },
                                modifier = Modifier.weight(1f)
                            )


                            // Middle: Transfer buttons
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                TransferButton(
                                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDesc = "Move to right",
                                    enabled = highlightedAvailable.isNotEmpty(),
                                    onClick = moveRight,
                                    size = buttonSize,
                                    colors = colors
                                )
                                TransferButton(
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDesc = "Move to left",
                                    enabled = highlightedSelected.isNotEmpty(),
                                    onClick = moveLeft,
                                    size = buttonSize,
                                    colors = colors
                                )
                            }

                            // Right: Selected Parameters
                            ParameterList(
                                title = "Selected parameters",
                                parameters = selectedParameters,
                                highlighted = highlightedSelected,
                                onItemClick = toggleHighlightSelected,
                                labelSelector = { attribute -> attribute.label.replaceFirstChar { it.uppercase() } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TransferButton(
    icon: ImageVector,
    contentDesc: String,
    enabled: Boolean,
    onClick: () -> Unit,
    size: Dp,
    colors: ColorScheme,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(size / 2),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp, pressedElevation = 2.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            disabledContainerColor = colors.primary.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            imageVector = icon, contentDescription = contentDesc, tint = colors.onPrimary
        )
    }
}
