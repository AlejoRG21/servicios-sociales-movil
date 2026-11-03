package com.example.serviciosocialapp // <- Tu package

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            var currentStudentName = "Jordan"
            var currentStudentEmail = "jordan@universidad.edu.co"
            var currentCareer = "Ingeniería de Software"
            var currentTargetHours = 40
            var selectedRecord: ServiceRecord? = null

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { name, email, target ->
                            currentStudentName = name
                            currentStudentEmail = email
                            currentTargetHours = target
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onGoToRegister = { navController.navigate("register") }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = { navController.navigate("login") },
                        onBackToLogin = { navController.popBackStack() }
                    )
                }

                // DASHBOARD PRINCIPAL
                composable("home") {
                    HomeScreen(
                        studentName = currentStudentName,
                        targetHours = currentTargetHours,
                        onNavigateToAdd = { navController.navigate("add_hours") },
                        onNavigateToRecords = { navController.navigate("records") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onSelectRecord = { record ->
                            selectedRecord = record
                            navController.navigate("detail")
                        },
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }

                // VISTA: PERFIL (VISUALIZAR Y EDITAR)
                composable("profile") {
                    ProfileScreen(
                        currentName = currentStudentName,
                        currentEmail = currentStudentEmail,
                        currentCareer = currentCareer,
                        currentTargetHours = currentTargetHours,
                        onSaveProfile = { newName, newCareer, newTarget ->
                            currentStudentName = newName
                            currentCareer = newCareer
                            currentTargetHours = newTarget
                            navController.popBackStack() // Regresar tras guardar
                        },
                        onBack = { navController.popBackStack() },
                        onNavigateToRecords = { navController.navigate("records") },
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }

                // VISTA: AGREGAR HORAS
                composable("add_hours") {
                    AddServiceScreen(
                        studentName = currentStudentName,
                        onBack = { navController.popBackStack() },
                        onServiceCreated = { navController.popBackStack() }
                    )
                }

                // VISTA: REGISTROS DE SERVICIO (BÚSQUEDA Y FILTROS)
                composable("records") {
                    ServiceRecordsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToAdd = { navController.navigate("add_hours") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onSelectRecord = { record ->
                            selectedRecord = record
                            navController.navigate("detail")
                        }
                    )
                }

                // VISTA: DETALLES DEL SERVICIO
                composable("detail") {
                    selectedRecord?.let { record ->
                        ServiceDetailScreen(
                            record = record,
                            onBack = { navController.popBackStack() }
                        )
                    } ?: navController.popBackStack()
                }
            }
        }
    }
}