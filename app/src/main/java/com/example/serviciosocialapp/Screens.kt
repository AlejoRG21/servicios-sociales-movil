package com.example.serviciosocialapp // <- Verifica tu package

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.border

// Paleta de Figma
val PrimaryPurple = Color(0xFF4E46E5)
val CardBackground = Color(0xFF4838D4)
val SoftLavender = Color(0xFFEEF2FF)
val BackgroundGray = Color(0xFFF8FAFC)
val DarkText = Color(0xFF0F172A)
val GrayText = Color(0xFF64748B)
val LightBorder = Color(0xFFE2E8F0)

// -------------------------------------------------------------------------------------
// 1. DASHBOARD PRINCIPAL (HOME)
// -------------------------------------------------------------------------------------
@Composable
fun HomeScreen(
    studentName: String,
    targetHours: Int,
    onNavigateToAdd: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectRecord: (ServiceRecord) -> Unit,
    onLogout: () -> Unit
) {
    var records by remember { mutableStateOf<List<ServiceRecord>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = RetrofitClient.api.getMyRecords()
                if (res.isSuccessful) records = res.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    val approvedHours = records.filter { it.status == "APPROVED" }.sumOf { it.hours }
    val remainingHours = (targetHours - approvedHours).coerceAtLeast(0.0)
    val progressFraction = if (targetHours > 0) (approvedHours / targetHours).toFloat().coerceIn(0f, 1f) else 0f
    val progressPercentage = (progressFraction * 100).toInt()

    val currentDateStr = remember {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        val formatted = sdf.format(Date())
        formatted.replaceFirstChar { it.uppercase() }
    }

    val initials = remember(studentName) {
        val parts = studentName.trim().split(" ")
        if (parts.size >= 2) "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
        else studentName.take(2).uppercase()
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = 0,
                onSelectTab = { index ->
                    if (index == 1) onNavigateToRecords()
                    if (index == 2) onNavigateToProfile()
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(currentDateStr, color = GrayText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Hola, $studentName", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFFE0E7FF), CircleShape)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Tarjeta Morada Figma
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Horas aprobadas", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                                Text(
                                    if (approvedHours % 1.0 == 0.0) "${approvedHours.toInt()}" else "$approvedHours",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(62.dp)) {
                                CircularProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.White,
                                    strokeWidth = 4.5.dp,
                                    trackColor = Color.White.copy(alpha = 0.25f)
                                )
                                Text("$progressPercentage%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Faltan ${if (remainingHours % 1.0 == 0.0) remainingHours.toInt() else remainingHours} horas para alcanzar tu meta de $targetHours",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Botón Agregar Horas
            item {
                Button(
                    onClick = onNavigateToAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar nuevas horas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Actividad Reciente Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Actividad reciente", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    Text(
                        "Ver todo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryPurple,
                        modifier = Modifier.clickable { onNavigateToRecords() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (records.isEmpty()) {
                item {
                    Text("No hay actividades registradas aún.", color = GrayText, fontSize = 13.sp, modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            items(records) { record ->
                ActividadFigmaRow(record = record, onClick = { onSelectRecord(record) })
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 2. NUEVA VISTA: PERFIL DEL ESTUDIANTE (VISUALIZAR Y EDITAR)
// -------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentName: String,
    currentEmail: String,
    currentCareer: String,
    currentTargetHours: Int,
    onSaveProfile: (String, String, Int) -> Unit,
    onBack: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onLogout: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var career by remember { mutableStateOf(currentCareer) }
    var targetHours by remember { mutableStateOf(currentTargetHours.toString()) }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val initials = remember(name) {
        val parts = name.trim().split(" ")
        if (parts.size >= 2) "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
        else name.take(2).uppercase()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = 2,
                onSelectTab = { index ->
                    if (index == 0) onBack()
                    if (index == 1) onNavigateToRecords()
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // Avatar Header
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .background(SoftLavender, CircleShape)
                            .border(3.dp, PrimaryPurple.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = SoftLavender, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = "Estudiante Activo",
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(26.dp))
            }

            // Formulario de Edición
            item {
                Text("Datos Personales y Académicos", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                Spacer(modifier = Modifier.height(14.dp))

                // Campo Nombre
                Text("Nombre Completo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GrayText)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = PrimaryPurple) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = LightBorder)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo Correo
                Text("Correo Institucional", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GrayText)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Outlined.Email, null, tint = PrimaryPurple) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = LightBorder)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo Carrera / Programa
                Text("Programa Académico", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GrayText)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = career,
                    onValueChange = { career = it },
                    placeholder = { Text("Ej: Ingeniería de Software") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Outlined.School, null, tint = PrimaryPurple) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = LightBorder)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo Meta de Horas
                Text("Meta de Horas Requeridas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GrayText)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Outlined.AccessTime, null, tint = PrimaryPurple) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = LightBorder)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Botón Guardar Cambios
                Button(
                    onClick = {
                        val hours = targetHours.toIntOrNull() ?: 40
                        if (name.isBlank() || email.isBlank() || hours <= 0) {
                            Toast.makeText(context, "Ingresa datos válidos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        onSaveProfile(name.trim(), career.trim(), hours)
                        isSaving = false
                        Toast.makeText(context, "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text("Guardar Cambios", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botón Cerrar Sesión
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 3. VISTA 04: AGREGAR HORAS
// -------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    studentName: String,
    onBack: () -> Unit,
    onServiceCreated: () -> Unit
) {
    var organization by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val institutions = listOf(
        "Banco de Alimentos Greenwood",
        "Refugio de Animales Riverside",
        "Biblioteca Pública Municipal",
        "Construcción Comunitaria Hábitat"
    )
    var expandedInst by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val cal = Calendar.getInstance().apply { set(y, m, d) }
            selectedDate = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault()).format(cal.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar horas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SoftLavender
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Registra tu impacto", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkText)
                        Text("Agrega los detalles de tu actividad de servicio.", fontSize = 12.sp, color = GrayText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Institución aliada", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))

            Box {
                OutlinedTextField(
                    value = organization.ifEmpty { "Selecciona una institución" },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedInst = true },
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        Icon(Icons.Default.ChevronRight, null, tint = GrayText, modifier = Modifier.clickable { expandedInst = true })
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightBorder,
                        unfocusedBorderColor = LightBorder,
                        focusedTextColor = if (organization.isEmpty()) GrayText else DarkText,
                        unfocusedTextColor = if (organization.isEmpty()) GrayText else DarkText
                    )
                )

                DropdownMenu(expanded = expandedInst, onDismissRequest = { expandedInst = false }) {
                    institutions.forEach { inst ->
                        DropdownMenuItem(
                            text = { Text(inst, fontSize = 14.sp) },
                            onClick = {
                                organization = inst
                                expandedInst = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text("Fecha", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkText)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("DD / MM / AAAA", color = GrayText, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Outlined.CalendarToday, null, tint = GrayText, modifier = Modifier.size(20.dp))
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LightBorder, unfocusedBorderColor = LightBorder)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Horas realizadas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkText)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        placeholder = { Text("0.0", color = GrayText, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = LightBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Descripción de la actividad", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 300) description = it },
                placeholder = { Text("Describe qué hiciste y cómo ayudó...", color = GrayText, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = LightBorder)
            )

            Text(
                text = "${description.length} / 300",
                fontSize = 11.sp,
                color = GrayText,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = BackgroundGray,
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Revisa los datos antes de enviarlos para aprobación.", fontSize = 11.sp, color = GrayText)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val h = hours.toDoubleOrNull() ?: 0.0
                    if (organization.isBlank() || selectedDate.isBlank() || h <= 0 || description.isBlank()) {
                        Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            RetrofitClient.api.createServiceRecord(
                                ServiceRecord(
                                    studentName = studentName,
                                    organization = organization,
                                    hours = h,
                                    date = selectedDate,
                                    description = description,
                                    status = "PENDING"
                                )
                            )
                            Toast.makeText(context, "¡Horas enviadas para aprobación!", Toast.LENGTH_SHORT).show()
                            onServiceCreated()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else Text("Enviar para aprobación", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// -------------------------------------------------------------------------------------
// 4. VISTA 05: REGISTROS DE SERVICIO (BÚSQUEDA Y FILTROS)
// -------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRecordsScreen(
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectRecord: (ServiceRecord) -> Unit
) {
    var records by remember { mutableStateOf<List<ServiceRecord>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = RetrofitClient.api.getMyRecords()
                if (res.isSuccessful) records = res.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    val pendingCount = records.count { it.status == "PENDING" }
    val approvedCount = records.count { it.status == "APPROVED" }

    val filteredRecords = records.filter { record ->
        val matchesFilter = when (selectedFilter) {
            "PENDING" -> record.status == "PENDING"
            "APPROVED" -> record.status == "APPROVED"
            else -> true
        }
        val matchesSearch = record.organization.contains(searchQuery, ignoreCase = true) ||
                record.description.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    val totalHoursFiltered = filteredRecords.sumOf { it.hours }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar horas", modifier = Modifier.size(28.dp))
            }
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = 1,
                onSelectTab = { index ->
                    if (index == 0) onBack()
                    if (index == 2) onNavigateToProfile()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Registros de servicio", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkText)
                IconButton(onClick = {}) { Icon(Icons.Default.Tune, null, tint = DarkText) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar instituciones...", color = GrayText, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = GrayText) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BackgroundGray,
                    unfocusedContainerColor = BackgroundGray,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPillChip(label = "Todos", isSelected = selectedFilter == "TODOS", onClick = { selectedFilter = "TODOS" })
                FilterPillChip(label = "Pendiente", count = pendingCount, isSelected = selectedFilter == "PENDING", onClick = { selectedFilter = "PENDING" })
                FilterPillChip(label = "Aprobado", count = approvedCount, isSelected = selectedFilter == "APPROVED", onClick = { selectedFilter = "APPROVED" })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SoftLavender
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${filteredRecords.size} registros", color = PrimaryPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        "${if (totalHoursFiltered % 1.0 == 0.0) totalHoursFiltered.toInt() else totalHoursFiltered} horas totales",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("OCTUBRE DE 2024", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayText, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredRecords) { record ->
                    ActividadFigmaRow(record = record, onClick = { onSelectRecord(record) })
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 5. VISTA: DETALLES DEL SERVICIO
// -------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(record: ServiceRecord, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del servicio", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkText) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = DarkText) }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = DarkText) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(SoftLavender, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.School, null, tint = PrimaryPurple, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    StatusPillFigma(record.status)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(record.organization, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    Text(record.category, fontSize = 13.sp, color = GrayText)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp), color = BackgroundGray) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccessTime, null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Total de horas", fontSize = 11.sp, color = GrayText)
                            Text("${record.hours}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        }
                    }
                }

                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp), color = BackgroundGray) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Fecha del servicio", fontSize = 11.sp, color = GrayText)
                            Text(record.date.take(6), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text("Actividad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = record.description.ifEmpty { "Organicé alimentos donados, preparé cajas para familias y ayudé a distribuir suministros durante el turno de la tarde." },
                fontSize = 13.sp,
                color = GrayText,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            Text("Detalles de la institución", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, null, tint = PrimaryPurple, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(record.organization, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                    Text("Institución aliada", fontSize = 12.sp, color = GrayText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, null, tint = PrimaryPurple, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(record.coordinator, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                    Text("Coordinadora de servicio", fontSize = 12.sp, color = GrayText)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (record.status == "APPROVED") {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color(0xFFE8F7F0)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF0F9D58), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Aprobado por la ${record.approvedBy}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F9D58))
                            Text(record.approvedDate ?: "26 oct 2024, 10:32 a. m.", fontSize = 11.sp, color = Color(0xFF0F9D58).copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// COMPONENTES AUXILIARES
// -------------------------------------------------------------------------------------
@Composable
fun FilterPillChip(label: String, count: Int? = null, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryPurple else Color.Transparent,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else GrayText)
            if (count != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "$count", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else GrayText)
            }
        }
    }
}

@Composable
fun ActividadFigmaRow(record: ServiceRecord, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(46.dp).background(SoftLavender, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                val icon = if (record.organization.contains("Biblioteca", ignoreCase = true)) Icons.Default.Article else Icons.Default.School
                Icon(icon, null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(record.organization, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                Text(record.category, color = GrayText, fontSize = 12.sp)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${record.hours} h", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
            Spacer(modifier = Modifier.height(4.dp))
            StatusPillFigma(record.status)
        }
    }
}

@Composable
fun StatusPillFigma(status: String) {
    val isApproved = status == "APPROVED"
    val isRejected = status == "REJECTED"

    val (bg, textColor, dotColor, label) = when {
        isApproved -> Quad(Color(0xFFE8F7F0), Color(0xFF0F9D58), Color(0xFF0F9D58), "Aprobado")
        isRejected -> Quad(Color(0xFFFDE8E8), Color(0xFFD93025), Color(0xFFD93025), "Rechazado")
        else -> Quad(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFF59E0B), "Pendiente")
    }

    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)) {
            Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
            Spacer(modifier = Modifier.width(5.dp))
            Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BottomNavBar(selectedTab: Int, onSelectTab: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onSelectTab(0) },
            icon = { Icon(if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home, "Inicio") },
            label = { Text("Inicio", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple, selectedTextColor = PrimaryPurple,
                indicatorColor = SoftLavender, unselectedIconColor = GrayText, unselectedTextColor = GrayText
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onSelectTab(1) },
            icon = { Icon(if (selectedTab == 1) Icons.Filled.Article else Icons.Outlined.Article, "Registros") },
            label = { Text("Registros", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple, selectedTextColor = PrimaryPurple,
                indicatorColor = SoftLavender, unselectedIconColor = GrayText, unselectedTextColor = GrayText
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onSelectTab(2) },
            icon = { Icon(if (selectedTab == 2) Icons.Filled.Person else Icons.Outlined.Person, "Perfil") },
            label = { Text("Perfil", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple, selectedTextColor = PrimaryPurple,
                indicatorColor = SoftLavender, unselectedIconColor = GrayText, unselectedTextColor = GrayText
            )
        )
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// Login y Registro
@Composable
fun LoginScreen(onLoginSuccess: (String, String, Int) -> Unit, onGoToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(28.dp), verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(60.dp).background(SoftLavender, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.HourglassTop, null, tint = PrimaryPurple, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Service Tracker", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Text("Ingresa tus credenciales de estudiante", fontSize = 14.sp, color = GrayText)
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Institucional") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                scope.launch {
                    try {
                        val res = RetrofitClient.api.login(LoginRequest(email.trim(), password))
                        if (res.isSuccessful && res.body() != null) {
                            val user = res.body()!!.user
                            onLoginSuccess(user.name, user.email, user.targetHours)
                        } else {
                            Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally { isLoading = false }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            else Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onGoToRegister, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)) {
            Text("¿No tienes cuenta? Regístrate aquí", color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var targetHours by remember { mutableStateOf("40") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text("Crear Cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Text("Comienza el control de tus horas de servicio", fontSize = 14.sp, color = GrayText)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Institucional") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = targetHours, onValueChange = { targetHours = it }, label = { Text("Meta de Horas (ej: 40)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val hours = targetHours.toIntOrNull() ?: 40
                isLoading = true
                scope.launch {
                    try {
                        val res = RetrofitClient.api.registerStudent(RegisterStudentRequest(name.trim(), email.trim(), password, hours))
                        if (res.isSuccessful) {
                            Toast.makeText(context, "¡Registro completado!", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        } else {
                            Toast.makeText(context, "Error en el registro", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally { isLoading = false }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            enabled = !isLoading
        ) {
            Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp)) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = PrimaryPurple)
        }
    }
}