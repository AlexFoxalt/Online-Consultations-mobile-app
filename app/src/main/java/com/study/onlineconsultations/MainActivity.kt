package com.study.onlineconsultations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.study.onlineconsultations.auth.AuthUiState
import com.study.onlineconsultations.auth.AuthViewModel
import com.study.onlineconsultations.auth.AuthViewModelFactory
import com.study.onlineconsultations.data.AppDatabase
import com.study.onlineconsultations.data.AuthRepository

data class Consultation(
    val id: Int,
    val title: String,
    val consultantName: String,
    val specialization: String,
    val priceUsd: Int,
    val durationMinutes: Int,
    val availableSlots: List<String>
)

data class BookedConsultation(
    val selectedSlot: String,
    val paidAmountUsd: Int
)

private val sampleConsultations = listOf(
    Consultation(
        id = 1,
        title = "General Health Check Consultation",
        consultantName = "Dr. Emily Clark",
        specialization = "General Practitioner",
        priceUsd = 35,
        durationMinutes = 30,
        availableSlots = listOf("09:00", "11:30", "14:00")
    ),
    Consultation(
        id = 2,
        title = "Nutrition and Meal Planning",
        consultantName = "Anna Brown",
        specialization = "Nutritionist",
        priceUsd = 45,
        durationMinutes = 40,
        availableSlots = listOf("10:00", "13:00", "16:30")
    ),
    Consultation(
        id = 3,
        title = "Career Mentoring Session",
        consultantName = "Michael Lewis",
        specialization = "Career Consultant",
        priceUsd = 50,
        durationMinutes = 45,
        availableSlots = listOf("08:30", "12:30", "18:00")
    ),
    Consultation(
        id = 4,
        title = "Stress Management Consultation",
        consultantName = "Dr. Sophie Hall",
        specialization = "Psychologist",
        priceUsd = 60,
        durationMinutes = 50,
        availableSlots = listOf("09:30", "15:00", "19:00")
    ),
    Consultation(
        id = 5,
        title = "Personal Finance Consultation",
        consultantName = "John Miller",
        specialization = "Financial Advisor",
        priceUsd = 55,
        durationMinutes = 45,
        availableSlots = listOf("10:30", "14:30", "17:30")
    )
)

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        AuthViewModelFactory(AuthRepository(database.userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by authViewModel.uiState.collectAsStateWithLifecycle()
                    AppContent(
                        state = state,
                        onSetRegisterMode = authViewModel::setRegisterMode,
                        onFullNameChanged = authViewModel::onFullNameChanged,
                        onEmailChanged = authViewModel::onEmailChanged,
                        onPasswordChanged = authViewModel::onPasswordChanged,
                        onConfirmPasswordChanged = authViewModel::onConfirmPasswordChanged,
                        onSubmit = authViewModel::submit,
                        onLogout = authViewModel::logout
                    )
                }
            }
        }
    }
}

@Composable
private fun AppContent(
    state: AuthUiState,
    onSetRegisterMode: (Boolean) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onLogout: () -> Unit
) {
    if (state.currentUser == null) {
        AuthScreen(
            state = state,
            onSetRegisterMode = onSetRegisterMode,
            onFullNameChanged = onFullNameChanged,
            onEmailChanged = onEmailChanged,
            onPasswordChanged = onPasswordChanged,
            onConfirmPasswordChanged = onConfirmPasswordChanged,
            onSubmit = onSubmit
        )
    } else {
        HomeScreen(
            fullName = state.currentUser.fullName,
            consultations = sampleConsultations,
            onLogout = onLogout
        )
    }
}

@Composable
private fun AuthScreen(
    state: AuthUiState,
    onSetRegisterMode: (Boolean) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = screenHorizontalPadding(maxWidth)
        val contentMaxWidth = 520.dp

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = contentMaxWidth)
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (state.isRegisterMode) "Create account" else "Sign in",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isRegisterMode) {
                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = onFullNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full name") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            if (state.isRegisterMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }

            state.message?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = if (state.isRegisterMode) "Register" else "Login")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { onSetRegisterMode(!state.isRegisterMode) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (state.isRegisterMode) {
                        "Already have an account? Login"
                    } else {
                        "No account yet? Register"
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    fullName: String,
    consultations: List<Consultation>,
    onLogout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecialization by remember { mutableStateOf("All") }
    var selectedSlotsByConsultation by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var bookedConsultations by remember { mutableStateOf<Map<Int, BookedConsultation>>(emptyMap()) }
    var bookingMessage by remember { mutableStateOf<String?>(null) }
    val specializationOptions = remember(consultations) {
        listOf("All") + consultations.map { it.specialization }.distinct().sorted()
    }

    val keyword = searchQuery.trim()
    val filteredConsultations = consultations.filter { consultation ->
        val matchesKeyword = keyword.isBlank() ||
            consultation.title.contains(keyword, ignoreCase = true) ||
            consultation.consultantName.contains(keyword, ignoreCase = true) ||
            consultation.specialization.contains(keyword, ignoreCase = true)
        val matchesSpecialization = selectedSpecialization == "All" ||
            consultation.specialization == selectedSpecialization
        matchesKeyword && matchesSpecialization
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = screenHorizontalPadding(maxWidth)
        val contentMaxWidth = screenContentMaxWidth(maxWidth)

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = contentMaxWidth)
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hello, $fullName",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(onClick = onLogout) {
                    Text("Logout")
                }
            }

            Text(
                text = "Available consultations",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search consultations or specialists") },
                singleLine = true
            )

            Text(
                text = "Filter by specialization",
                style = MaterialTheme.typography.labelLarge
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(specializationOptions) { option ->
                    FilterChip(
                        selected = selectedSpecialization == option,
                        onClick = { selectedSpecialization = option },
                        label = { Text(option) }
                    )
                }
            }

            bookingMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (filteredConsultations.isEmpty()) {
                Text(
                    text = "No consultations found for current search/filter",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredConsultations, key = { it.id }) { consultation ->
                        ConsultationCard(
                            consultation = consultation,
                            selectedSlot = selectedSlotsByConsultation[consultation.id],
                            bookedConsultation = bookedConsultations[consultation.id],
                            onSlotSelected = { slot ->
                                selectedSlotsByConsultation =
                                    selectedSlotsByConsultation + (consultation.id to slot)
                                bookingMessage = null
                            },
                            onBookRequested = {
                                val selectedSlot = selectedSlotsByConsultation[consultation.id]
                                if (selectedSlot == null) {
                                    bookingMessage = "Please select a time slot before booking."
                                    return@ConsultationCard
                                }

                                if (bookedConsultations[consultation.id] != null) {
                                    bookingMessage = "This consultation is already booked."
                                    return@ConsultationCard
                                }

                                val booking = BookedConsultation(
                                    selectedSlot = selectedSlot,
                                    paidAmountUsd = consultation.priceUsd
                                )
                                bookedConsultations =
                                    bookedConsultations + (consultation.id to booking)
                                bookingMessage =
                                    "Booked and paid: ${consultation.title} at $selectedSlot."
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun screenHorizontalPadding(maxWidth: Dp): Dp {
    return when {
        maxWidth >= 840.dp -> 48.dp
        maxWidth >= 600.dp -> 32.dp
        else -> 16.dp
    }
}

private fun screenContentMaxWidth(maxWidth: Dp): Dp {
    return when {
        maxWidth >= 840.dp -> 920.dp
        maxWidth >= 600.dp -> 720.dp
        else -> 600.dp
    }
}

@Composable
private fun ConsultationCard(
    consultation: Consultation,
    selectedSlot: String?,
    bookedConsultation: BookedConsultation?,
    onSlotSelected: (String) -> Unit,
    onBookRequested: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = consultation.title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "${consultation.consultantName} • ${consultation.specialization}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${consultation.priceUsd}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${consultation.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Available time slots",
                style = MaterialTheme.typography.labelLarge
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(consultation.availableSlots) { slot ->
                    FilterChip(
                        selected = selectedSlot == slot,
                        onClick = { onSlotSelected(slot) },
                        enabled = bookedConsultation == null,
                        label = { Text(slot) }
                    )
                }
            }

            if (selectedSlot != null) {
                Text(
                    text = "Selected slot: $selectedSlot",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (bookedConsultation == null) {
                Button(
                    onClick = onBookRequested,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedSlot != null
                ) {
                    Text("Book and pay $${consultation.priceUsd}")
                }
            } else {
                Text(
                    text = "Booked slot: ${bookedConsultation.selectedSlot}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Payment received: $${bookedConsultation.paidAmountUsd}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
