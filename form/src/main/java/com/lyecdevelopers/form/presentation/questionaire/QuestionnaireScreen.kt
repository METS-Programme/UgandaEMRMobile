package com.lyecdevelopers.form.presentation.questionaire

import android.util.Log
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.fhir.datacapture.QuestionnaireFragment

@Composable
fun QuestionnaireScreen(
    formId: String,
    patientId: String,
) {
    val questionnaireViewModel: QuestionnaireViewModel = hiltViewModel()
    val state by questionnaireViewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    QuestionnaireFragment().apply {
                        arguments = bundleOf(
                            "questionnaire-json" to state.questionnaire
                        )
                    }.onCreateView(LayoutInflater.from(ctx), null, null)
                }, modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val response = questionnaireViewModel.getQuestionnaireResponse()

                    Log.d("Response", "" + response)

                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}


