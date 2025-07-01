package com.lyecdevelopers.worklist.presentation.patient

import com.lyecdevelopers.core.ui.theme.UgandaEMRMobileTheme
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

class StartVisitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UgandaEMRMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StartVisitScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartVisitScreen() {

    /* ──────────────── state ──────────────── */

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var visitType          by remember { mutableStateOf("") }
    var visitLocation      by remember { mutableStateOf("ART Clinic") }
    var visitStatus        by remember { mutableStateOf("New") }

    var startDate          by remember {
        mutableStateOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        )
    }
    var startTime          by remember {
        mutableStateOf(
            SimpleDateFormat("hh:mm", Locale.getDefault()).format(calendar.time)
        )
    }
    var amPm               by remember { mutableStateOf(
        if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
    ) }

    val visitStatuses  = listOf("New", "Ongoing", "In the past")
    val visitTypes     = listOf("Facility Visit", "Community Visit")
    val visitLocations = listOf("ART Clinic", "OPD", "Maternity", "Outreach")

    var locationMenuExpanded by remember { mutableStateOf(false) }
    var amPmMenuExpanded     by remember { mutableStateOf(false) }

    /* ──────────────── UI ──────────────── */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        /* Title */
        Text(
            "Start a visit",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        /* Visit status tabs */
        Text("The visit is", fontWeight = FontWeight.Medium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            visitStatuses.forEach { status ->
                OutlinedButton(
                    onClick = { visitStatus = status },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (visitStatus == status) Color(0xFFDDEEFF)
                        else Color.Transparent
                    )
                ) {
                    Text(
                        status,
                        color = if (visitStatus == status) Color.Blue else Color.Black
                    )
                }
            }
        }

        /* Start date + time (only for Ongoing / In the past) */
        if (visitStatus != "New") {
            Spacer(Modifier.height(16.dp))
            Text("Visit start date", fontWeight = FontWeight.Medium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment   = Alignment.CenterVertically
            ) {

                /* Date picker */
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                startDate = SimpleDateFormat(
                                    "dd/MM/yyyy", Locale.getDefault()
                                ).format(calendar.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(startDate)
                }

                /* Time picker */
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                calendar.set(Calendar.MINUTE,     minute)

                                // format hh:mm and AM/PM
                                val isAfternoon = hourOfDay >= 12
                                amPm = if (isAfternoon) "PM" else "AM"
                                val hr12 = when {
                                    hourOfDay == 0  -> 12
                                    hourOfDay > 12 -> hourOfDay - 12
                                    else           -> hourOfDay
                                }
                                startTime = "%02d:%02d".format(hr12, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                ) {
                    Text(startTime)
                }

                /* AM/PM dropdown */
                ExposedDropdownMenuBox(
                    expanded = amPmMenuExpanded,
                    onExpandedChange = { amPmMenuExpanded = !amPmMenuExpanded }
                ) {
                    TextField(
                        value = amPm,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(80.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = amPmMenuExpanded,
                        onDismissRequest = { amPmMenuExpanded = false }
                    ) {
                        listOf("AM", "PM").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    amPm = option
                                    amPmMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        /* Visit location */
        Spacer(Modifier.height(16.dp))
        Text("Visit location", fontWeight = FontWeight.Medium)

        ExposedDropdownMenuBox(
            expanded = locationMenuExpanded,
            onExpandedChange = { locationMenuExpanded = !locationMenuExpanded }
        ) {
            TextField(
                value       = visitLocation,
                onValueChange = { },
                readOnly    = true,
                label       = { Text("Select a location") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier    = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = locationMenuExpanded,
                onDismissRequest = { locationMenuExpanded = false }
            ) {
                visitLocations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = {
                            visitLocation = location
                            locationMenuExpanded = false
                        }
                    )
                }
            }
        }

        /* Visit type */
        Spacer(Modifier.height(16.dp))
        Text("Visit Type", fontWeight = FontWeight.Medium)
        visitTypes.forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { visitType = type }
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = visitType == type,
                    onClick = { visitType = type }
                )
                Text(type, modifier = Modifier.padding(start = 8.dp))
            }
        }

        /* Bottom buttons */
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { /* TODO: discard */ }) {
                Text("Discard")
            }
            Button(
                onClick  = { /* TODO: start visit */ },
                enabled  = visitType.isNotEmpty()
            ) {
                Text("Start visit")
            }
        }
    }
}
