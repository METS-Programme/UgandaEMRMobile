package com.lyecdevelopers.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lyecdevelopers.auth.R
import com.lyecdevelopers.auth.presentation.login.LoginScreen
import com.lyecdevelopers.core.ui.theme.UgandaEMRMobileTheme

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    UgandaEMRMobileTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // UgandaEMR logo
//                    Image(
//                        painter = painterResource(id = ),
//                        contentDescription = "UgandaEMR Logo",
//                        modifier = Modifier
//                            .size(100.dp)
//                            .clip(CircleShape),
//                        contentScale = ContentScale.Crop
//                    )

                    // Title
                    Text(
                        text = "Welcome to UgandaEMR",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    // Login form
                    LoginScreen(onLoginSuccess = onLoginSuccess)
                }
            }
        }
    }
}


