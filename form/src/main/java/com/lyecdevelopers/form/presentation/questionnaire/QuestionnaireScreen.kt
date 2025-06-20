package com.lyecdevelopers.form.presentation.questionnaire

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.lyecdevelopers.core.ui.components.ErrorView
import com.lyecdevelopers.core.ui.components.FragmentContainer
import com.lyecdevelopers.core.ui.components.LoadingView

@Composable
fun QuestionnaireScreen(
    fragmentManager: FragmentManager = LocalContext.current.let { it as? FragmentActivity }?.supportFragmentManager
        ?: throw IllegalStateException("Not a FragmentActivity"),
    formId: String,
) {
    val viewModel: QuestionnaireViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val currentState by rememberUpdatedState(state)

    // Prevent re-triggering when recomposing
    LaunchedEffect(key1 = formId) {
        viewModel.loadQuestionnaireByUuid(formId)
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                currentState.isLoading -> {
                    LoadingView()
                }

                currentState.error != null -> ErrorView(
                    message = currentState.error!!,
                    onRetry = { viewModel.loadQuestionnaireByUuid(formId) })

                currentState.questionnaireJson != null -> {
                    val fragment = FormQuestionnaireFragment().apply {
                        arguments = bundleOf(
                            FormQuestionnaireFragment.ARG_QUESTIONNAIRE_JSON to currentState.questionnaireJson
                        )
                    }
                    FragmentContainer(
                        modifier = Modifier.fillMaxSize(),
                        fragmentManager = fragmentManager,
                        fragment = fragment,
                        tag = FormQuestionnaireFragment.QUESTIONNAIRE_FRAGMENT_TAG
                    )
                }
            }
        }
    }
}




