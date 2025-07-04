package com.lyecdevelopers.worklist.presentation.patient

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lyecdevelopers.core.ui.components.SubmitButton

@Composable
fun RecordVitalScreen(navController: NavController) {

    var temperature by remember { mutableStateOf("") }
    var bloodPressureSystolic by remember { mutableStateOf("") }
    var bloodPressureDiastolic by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var respirationRate by remember { mutableStateOf("") }
    var spo2 by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var bmi by remember { mutableStateOf("") }
    var muac by remember { mutableStateOf("") }

    LaunchedEffect(weight, height) {
        bmi = calculateBmi(weight, height)
    }

    var showValidationError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Record Vitals and Biometrics", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Record vitals")
        RowPair("Temperature", "DEG C", temperature) { temperature = it }
        RowPairTwoFields(
            label = "Blood Pressure",
            unit = "mmHg",
            first = bloodPressureSystolic,
            second = bloodPressureDiastolic,
            onFirstChange = { bloodPressureSystolic = it },
            onSecondChange = { bloodPressureDiastolic = it }
        )
        RowPair("Heart rate", "beats/min", heartRate) { heartRate = it }
        RowPair("Respiration rate", "breaths/min", respirationRate) { respirationRate = it }
        RowPair("SpO2", "%", spo2) { spo2 = it }

        Text("Notes", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        TextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text("Type any additional notes here") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Record biometrics")
        RowPair("Weight", "kg", weight) { weight = it }
        RowPair("Height", "cm", height) { height = it }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("BMI (auto)", fontSize = 14.sp)
                TextField(
                    value = bmi,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("---") },
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("kg / m²", fontSize = 12.sp)
        }

        MuacRow(muac = muac) { muac = it }

        if (showValidationError) {
            Text(
                text = "Temperature, Weight, and Height must not be empty.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        ActionButtons(
            navController = navController,
            onValidateAndSave = {
                if (temperature.isNotBlank() && weight.isNotBlank() && height.isNotBlank()) {
                    showValidationError = false
                    // TODO: Persist data here
                    navController.popBackStack()
                } else {
                    showValidationError = true
                }
            }
        )
    }
}

@Composable
fun MuacRow(muac: String, onValueChange: (String) -> Unit) {
    val (label, colour) = remember(muac) {
        val value = muac.toFloatOrNull()
        when {
            value == null -> "" to Color.Transparent
            value < 19f -> "Red < 19.0cm" to Color.Red
            value in 19f..22f -> "Yellow 19.0–22.0cm" to Color.Yellow
            value > 22f -> "Green > 22.0cm" to Color.Green
            else -> "" to Color.Transparent
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text("MUAC", fontSize = 14.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = muac,
                onValueChange = onValueChange,
                placeholder = { Text("---") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("cm", fontSize = 12.sp)
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .background(colour, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    navController: NavController,
    onValidateAndSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SubmitButton(
            text = "Discard",
            onClick = { navController.popBackStack() },
            iconContentDescription = "Discard",
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        )

        SubmitButton(
            text = "Save",
            onClick = { onValidateAndSave() },
            iconContentDescription = "Save",
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun RowPair(label: String, unit: String, value: String, onValueChange: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 14.sp)
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("---") },
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(unit, fontSize = 12.sp)
    }
}

@Composable
fun RowPairTwoFields(
    label: String,
    unit: String,
    first: String,
    second: String,
    onFirstChange: (String) -> Unit,
    onSecondChange: (String) -> Unit
) {
    Text(text = label, fontSize = 14.sp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        TextField(
            value = first,
            onValueChange = onFirstChange,
            placeholder = { Text("---") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Text("/", modifier = Modifier.padding(horizontal = 8.dp))
        TextField(
            value = second,
            onValueChange = onSecondChange,
            placeholder = { Text("---") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(unit, fontSize = 12.sp)
    }
}

@SuppressLint("DefaultLocale")
fun calculateBmi(weightStr: String, heightStr: String): String {
    return try {
        val weight = weightStr.toDouble()
        val heightCm = heightStr.toDouble()
        val heightM = heightCm / 100
        val bmi = weight / (heightM * heightM)
        String.format("%.1f", bmi)
    } catch (e: Exception) {
        ""
    }
}
