package com.datastorewrapperdemo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.datastorewrapperdemo.ui.theme.DataStoreWrapperDemoTheme
import com.library.datastorewrapper.DataStoreManager
import com.library.datastorewrapper.getObject
import com.library.datastorewrapper.getObjectSync
import com.library.datastorewrapper.saveObject
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

// Objeto de datos complejo para serialización
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class UserProfile(
    val name: String,
    val email: String,
    val age: Int,
    val isPremium: Boolean
)

/**
 * Función auxiliar que lee todas las claves de un DataStore y formatea el contenido.
 * Intenta leer el valor de la clave usando los tipos de datos esperados para una mejor visualización.
 */
suspend fun DataStoreManager.getStoreContents(context: Context, storeName: String): String {
    val allKeys = this.getAllKeys()

    if (allKeys.isEmpty()) {
        return "--- Contenido de $storeName --- \n\n¡No hay claves guardadas!"
    }

    val sb = StringBuilder()
    sb.appendLine("--- Contenido de $storeName (${context.packageName}) ---")
    sb.appendLine()

    for (key in allKeys) {
        val value: Any? = when (key) {
            // Claves conocidas del DataStore de inicio de sesión
            DataStoreKeys.LOGIN_USERNAME -> this.getStringSync(key, "N/A")
            DataStoreKeys.LOGIN_COUNT -> this.getIntSync(key, -1)
            DataStoreKeys.LOGIN_IS_LOGGED_IN -> this.getBooleanSync(key, false)

            // Claves conocidas del DataStore de Perfil (JSON)
            DataStoreKeys.PROFILE_USER_PROFILE_JSON -> {
                // Intenta deserializar como objeto usando la extensión síncrona
                val profileObject = this.getObjectSync(key, UserProfile.serializer())
                if (profileObject != null) {
                    "Objeto (JSON): Nombre=${profileObject.name}, Edad=${profileObject.age}"
                } else {
                    // Si falló, obtiene la cadena JSON bruta
                    val jsonValue = this.getStringSync(key)
                    if (jsonValue.isNotEmpty()) "JSON Bruto: $jsonValue" else "JSON Vacío"
                }
            }

            // Claves conocidas del DataStore de Configuración
            DataStoreKeys.SETTINGS_THEME -> this.getStringSync(key, "N/A")
            DataStoreKeys.SETTINGS_FONT_SIZE -> this.getIntSync(key, -1)

            // Fallback para tipos desconocidos
            else -> {
                val sVal = this.getStringSync(key)
                if (sVal.isNotEmpty()) {
                    sVal
                } else {
                    val iVal = this.getIntSync(key, Int.MIN_VALUE)
                    if (iVal != Int.MIN_VALUE) {
                        iVal.toString()
                    } else {
                        "Valor Desconocido"
                    }
                }
            }
        }
        sb.appendLine(" • Clave '$key': $value")
    }

    return sb.toString()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataStoreWrapperDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DataStoreDemo()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStoreDemo() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val loginStore = remember { DataStoreManager.getInstance(context, DataStoreKeys.DATASTORE_NAME_LOGIN) }
    val profileStore = remember { DataStoreManager.getInstance(context, DataStoreKeys.DATASTORE_NAME_PROFILE) }
    val settingsStore = remember { DataStoreManager.getInstance(context, DataStoreKeys.DATASTORE_NAME_SETTINGS) }

    // Estado para el DataStore de Inicio de Sesión
    var username by remember { mutableStateOf("") }
    var loginCount by remember { mutableIntStateOf(0) }
    var isLoggedIn by remember { mutableStateOf(false) }

    // Estado para el DataStore de Perfil
    var profileName by remember { mutableStateOf("") }
    var profileEmail by remember { mutableStateOf("") }
    var profileAge by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var savedProfile by remember { mutableStateOf<UserProfile?>(null) } // El perfil deserializado

    // Estado para el DataStore de Configuración
    var theme by remember { mutableStateOf("light") }
    var fontSize by remember { mutableIntStateOf(14) }

    // Mensaje de feedback/estado
    var statusMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Cargar datos de Inicio de Sesión
        loginStore.getString(DataStoreKeys.LOGIN_USERNAME).collect { username = it }
        loginStore.getInt(DataStoreKeys.LOGIN_COUNT).collect { loginCount = it }
        loginStore.getBoolean(DataStoreKeys.LOGIN_IS_LOGGED_IN).collect { isLoggedIn = it }

        // Cargar datos de Perfil (Objeto JSON)
        // Usa la función de extensión getObject<UserProfile>
        profileStore.getObject(DataStoreKeys.PROFILE_USER_PROFILE_JSON, UserProfile.serializer()).collect { profile ->
            savedProfile = profile
            // Rellena los campos de entrada con el perfil cargado para edición inmediata
            profile?.let {
                profileName = it.name
                profileEmail = it.email
                profileAge = it.age.toString()
                isPremium = it.isPremium
            }
        }

        // Cargar datos de Configuración
        settingsStore.getString(DataStoreKeys.SETTINGS_THEME).collect { theme = it }
        settingsStore.getInt(DataStoreKeys.SETTINGS_FONT_SIZE).collect { fontSize = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demostración del DataStore Wrapper") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (statusMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = statusMessage,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Pantalla de Inicio de Sesión (Tipos Simples)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Nombre de Usuario") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    loginStore.saveString(DataStoreKeys.LOGIN_USERNAME, username)
                                    val count = loginCount + 1
                                    loginStore.saveInt(DataStoreKeys.LOGIN_COUNT, count)
                                    loginStore.saveBoolean(DataStoreKeys.LOGIN_IS_LOGGED_IN, true)
                                    statusMessage = "✓ Datos de inicio de sesión guardados"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar Inicio de Sesión")
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    loginStore.clear()
                                    username = ""
                                    loginCount = 0
                                    isLoggedIn = false
                                    statusMessage = "✓ Datos de inicio de sesión eliminados"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Limpiar")
                        }
                    }

                    Text(
                        text = "Estado: ${if (isLoggedIn) "Autenticado" else "No Autenticado"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Conteo de Inicios de Sesión: $loginCount",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. Pantalla de Perfil (Objetos Complejos)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = profileEmail,
                        onValueChange = { profileEmail = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = profileAge,
                        onValueChange = { profileAge = it.filter { char -> char.isDigit() } }, // Solo acepta dígitos
                        label = { Text("Edad") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPremium,
                            onCheckedChange = { isPremium = it }
                        )
                        Text("Usuario Premium")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val profile = UserProfile(
                                        name = profileName,
                                        email = profileEmail,
                                        age = profileAge.toIntOrNull() ?: 0,
                                        isPremium = isPremium
                                    )

                                    profileStore.saveObject(
                                        DataStoreKeys.PROFILE_USER_PROFILE_JSON,
                                        profile,
                                        UserProfile.serializer()
                                    )

                                    statusMessage = "✓ Perfil guardado como objeto complejo (JSON)"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = profileName.isNotEmpty() && profileEmail.isNotEmpty()
                        ) {
                            Text("Guardar Perfil")
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    profileStore.clear()
                                    profileName = ""
                                    profileEmail = ""
                                    profileAge = ""
                                    isPremium = false
                                    savedProfile = null
                                    statusMessage = "✓ Perfil eliminado"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Limpiar")
                        }
                    }

                    if (savedProfile != null) {
                        HorizontalDivider()
                        Text(
                            text = "Perfil Guardado:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Nombre: ${savedProfile?.name}")
                        Text("Correo Electrónico: ${savedProfile?.email}")
                        Text("Edad: ${savedProfile?.age}")
                        Text("Premium: ${if (savedProfile?.isPremium == true) "Sí" else "No"}")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. Pantalla de Configuración",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text("Tema: $theme")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    settingsStore.saveString(DataStoreKeys.SETTINGS_THEME, "claro")
                                    statusMessage = "✓ Tema cambiado a claro"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Claro")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    settingsStore.saveString(DataStoreKeys.SETTINGS_THEME, "oscuro")
                                    statusMessage = "✓ Tema cambiado a oscuro"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Oscuro")
                        }
                    }

                    Text("Tamaño de fuente: $fontSize px")
                    Slider(
                        value = fontSize.toFloat(),
                        onValueChange = {
                            fontSize = it.toInt()
                            scope.launch {
                                settingsStore.saveInt(DataStoreKeys.SETTINGS_FONT_SIZE, fontSize)
                            }
                        },
                        valueRange = 10f..30f,
                        steps = 19
                    )

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                settingsStore.clear()
                                theme = "light"
                                fontSize = 14
                                statusMessage = "✓ Configuración restablecida"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restablecer Configuración")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "4. Demostración de Aislamiento (Detallado)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Cada DataStore (identificado por 'frontend') está aislado por el nombre del paquete de la aplicación. El botón de abajo muestra todas las claves y valores almacenados en cada uno de ellos.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                val loginContent = loginStore.getStoreContents(context, DataStoreKeys.DATASTORE_NAME_LOGIN)
                                val profileContent = profileStore.getStoreContents(context, DataStoreKeys.DATASTORE_NAME_PROFILE)
                                val settingsContent = settingsStore.getStoreContents(context, DataStoreKeys.DATASTORE_NAME_SETTINGS)

                                statusMessage = buildString {
                                    append(loginContent)
                                    appendLine("\n--------------------------------")
                                    append(profileContent)
                                    appendLine("\n--------------------------------")
                                    append(settingsContent)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver Claves y Valores por Pantalla")
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                loginStore.clear()
                                profileStore.clear()
                                settingsStore.clear()
                                statusMessage = "✓ Todos los datos eliminados de todos los DataStores"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Limpiar Todo")
                    }
                }
            }
        }
    }
}