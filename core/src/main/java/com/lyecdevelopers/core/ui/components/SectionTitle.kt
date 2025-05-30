package com.lyecdevelopers.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    textDecoration: TextDecoration? = null
) {
    Text(
        text = text,
        style = style.merge(
            textAlign?.let {
                TextStyle(
                    color = color,
                    textAlign = it,
                    fontWeight = fontWeight,
                    textDecoration = textDecoration
                )
            }
        ),
        modifier = modifier
    )
}

