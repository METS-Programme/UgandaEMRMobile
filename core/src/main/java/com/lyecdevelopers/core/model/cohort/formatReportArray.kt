package com.lyecdevelopers.core.model.cohort

fun formatReportArray(selectedItems: List<Indicator>?): List<ReportParamItem> {
    val arrayToReturn = mutableListOf<ReportParamItem>()

    selectedItems?.forEach { item ->
        arrayToReturn.add(
            ReportParamItem(
                label = item.label,
                type = item.type,
                expression = item.id,
                modifier = "",
                extras = emptyList()
            )
        )
    }

    return arrayToReturn
}
