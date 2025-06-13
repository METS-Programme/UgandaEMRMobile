package com.lyecdevelopers.sync.presentation.patients

//@Composable
//fun PatientFilterDialog(
//    visible: Boolean,
//    onDismiss: () -> Unit,
//    cohortOptions: List<Cohort>,
//    selectedCohort: Cohort?,
//    onSelectedCohortChanged: (Cohort) -> Unit,
//    indicatorOptions: List<Indicator>,
//    selectedIndicator: Indicator?,
//    onIndicatorSelected: (Indicator) -> Unit,
//    selectedDate: LocalDate?,
//    onDateSelected: (LocalDate) -> Unit,
//    availableParameters: List<String>,
//    selectedParameters: List<String>,
//    highlightedAvailable: List<String>,
//    highlightedSelected: List<String>,
//    onHighlightAvailableToggle: (String) -> Unit,
//    onHighlightSelectedToggle: (String) -> Unit,
//    onMoveRight: () -> Unit,
//    onMoveLeft: () -> Unit,
//    onFilter: () -> Unit,
//) {
//    if (visible) {
//        AlertDialog(
//            onDismissRequest = onDismiss, text = {
//                PatientFilterSectionContent(
//                    cohortOptions = cohortOptions,
//                    selectedCohort = selectedCohort,
//                    onSelectedCohortChanged = onSelectedCohortChanged,
//                    indicatorOptions = indicatorOptions,
//                    selectedIndicator = selectedIndicator,
//                    onIndicatorSelected = onIndicatorSelected,
//                    selectedDate = selectedDate,
//                    onDateSelected = onDateSelected,
//                    availableParameters = availableParameters,
//                    selectedParameters = selectedParameters,
//                    highlightedAvailable = highlightedAvailable,
//                    highlightedSelected = highlightedSelected,
//                    onHighlightAvailableToggle = onHighlightAvailableToggle,
//                    onHighlightSelectedToggle = onHighlightSelectedToggle,
//                    onMoveRight = onMoveRight,
//                    onMoveLeft = onMoveLeft,
//                    onFilter = {
//                        onFilter()
//                        onDismiss()
//                    })
//            }, confirmButton = {}, // No extra confirm, handled inside PatientFilterSectionContent
//            dismissButton = {
//                TextButton(onClick = onDismiss) {
//                    Text("Cancel")
//                }
//            }, modifier = Modifier.fillMaxWidth()
//        )
//    }
//}
