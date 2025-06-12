package com.lyecdevelopers.form.presentation.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegisterPatientFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the container layout for the nested QuestionnaireFragment
        return inflater.inflate(R.layout.fragment_questionnaire_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Call super.onViewCreated

        val questionnaireJson = requireArguments().getString(ARG_QUESTIONNAIRE_JSON)
            ?: error("Missing questionnaire JSON in RegisterPatientFragment arguments")

        AppLogger.d(TAG, "RegisterPatientFragment received JSON: ${questionnaireJson.take(100)}...")

        // Always ensure the nested QuestionnaireFragment is replaced with the latest JSON.
        // This handles cases where RegisterPatientFragment is recreated or arguments change.
        val questionnaireFragment = QuestionnaireFragment.builder()
            .setQuestionnaire(questionnaireJson) // Always use the latest JSON
            .setShowCancelButton(true).setShowSubmitButton(true).showOptionalText(true)
            .showRequiredText(false).setSubmitButtonText(
                getString(com.google.android.fhir.datacapture.R.string.submit_questionnaire)
            ).build()

        // Use replace instead of add to ensure old instances are removed
        // commitNowAllowingStateLoss is generally safe when dealing with AndroidView
        childFragmentManager.commitNow(allowStateLoss = true) {
            setReorderingAllowed(true)
            replace(
                R.id.fragment_container_view, // This should be the ID of the container in fragment_questionnaire_container.xml
                questionnaireFragment, QUESTIONNAIRE_FRAGMENT_TAG
            )
        }


        // Set up listeners for submit/cancel events.
        // It's good practice to ensure these are set up every onViewCreated
        // as the fragment might be recreated.
        childFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY, viewLifecycleOwner
        ) { _, _ ->
            handleSubmit()
        }

        childFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.CANCEL_REQUEST_KEY, viewLifecycleOwner
        ) { _, _ ->
            handleCancel()
        }
    }

    private fun handleSubmit() {
        Toast.makeText(requireContext(), "Form submitted!", Toast.LENGTH_SHORT).show()
        // Add logic to extract QuestionnaireResponse if needed
    }

    private fun handleCancel() {
        Toast.makeText(requireContext(), "Form cancelled.", Toast.LENGTH_SHORT).show()
        // Optionally navigate back
        // findNavController().navigateUp()
    }

    companion object {
        private const val TAG = "RegisterPatientFragment"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
        const val ARG_QUESTIONNAIRE_JSON = "questionnaire"
    }
}






