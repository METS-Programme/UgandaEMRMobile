package com.lyecdevelopers.form.presentation.registration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.lyecdevelopers.core.ui.components.ErrorView
import com.lyecdevelopers.core.ui.components.FragmentContainer
import com.lyecdevelopers.core.ui.components.LoadingView
import com.lyecdevelopers.form.presentation.questionnaire.FormQuestionnaireFragment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPatientScreen(
    fragmentManager: FragmentManager = LocalContext.current.let { it as? FragmentActivity }?.supportFragmentManager
        ?: throw IllegalStateException("Not a FragmentActivity"),
) {
    val viewModel: RegisterPatientViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsState()


    // Load questionnaire once
    LaunchedEffect(Unit) {
        viewModel.loadRegisterPatientQuestionnaireFromAssets()
    }


    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView()
                }

                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadRegisterPatientQuestionnaireFromAssets() })
                }

                uiState.questionnaireJson != null -> {

                    val fragment = RegisterPatientFragment().apply {
                        arguments = bundleOf(
                            FormQuestionnaireFragment.ARG_QUESTIONNAIRE_JSON to uiState.questionnaireJson
                        )
                    }
                    FragmentContainer(
                        modifier = Modifier.fillMaxSize(),
                        fragmentManager = fragmentManager,
                        fragment = fragment,
                        tag = RegisterPatientFragment.QUESTIONNAIRE_FRAGMENT_TAG
                    )
                }
            }
        }
    }
}











