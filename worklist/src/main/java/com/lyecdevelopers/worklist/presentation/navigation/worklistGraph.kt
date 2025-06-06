package com.lyecdevelopers.worklist.presentation.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.lyecdevelopers.core.model.BottomNavItem
import com.lyecdevelopers.form.presentation.forms.FormsScreen
import com.lyecdevelopers.form.presentation.questionaire.QuestionnaireScreen
import com.lyecdevelopers.worklist.domain.model.PatientDetails
import com.lyecdevelopers.worklist.presentation.patient.PatientDetailsScreen
import com.lyecdevelopers.worklist.presentation.worklist.WorklistScreen

fun NavGraphBuilder.worklistGraph(navController: NavController) {
    navigation(
        route = BottomNavItem.Worklist.route, startDestination = "worklist_main"
    ) {
        composable("worklist_main") {

            WorklistScreen(patients = emptyList(), onPatientClick = { patient ->
                navController.navigate("patient_details/${patient.id}")
            }, onStartVisit = { /* TODO */ })
        }

        composable("patient_details/{patientId}") { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: return@composable

            PatientDetailsScreen(
                patient = PatientDetails.empty(),
                onStartVisit = { /* TODO */ },
                onStartEncounter = { _, _ ->
                    navController.navigate("patient_details/$patientId/forms")
                })
        }

        composable("patient_details/{patientId}/forms") { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: return@composable

            FormsScreen(
                patientId = patientId, onFormClick = { form ->
                    navController.navigate("patient_details/$patientId/forms/${form.uuid}")
                })
        }

        composable(
            "patient_details/{patientId}/forms/{formId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("formId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val formId = backStackEntry.arguments?.getString("formId")

            if (patientId != null && formId != null) {
                QuestionnaireScreen(formId = formId, patientId = patientId)
            } else {
                Text("Missing patient or form ID")
            }
        }
    }

}
