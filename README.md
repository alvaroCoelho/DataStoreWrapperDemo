# DataStore Wrapper Library

Librería Android que proporciona un wrapper simple y eficiente sobre DataStore de Android para almacenar datos con aislamiento por frontend/pantalla.

## 🌟 Características

- ✅ Wrapper sobre DataStore Preferences de Android
- ✅ Aislamiento de datos por frontend/pantalla
- ✅ Soporte para tipos primitivos (String, Int, Boolean, Float, Long)
- ✅ Soporte para objetos complejos mediante serialización JSON
- ✅ API basada en Coroutines y Flow
- ✅ Patrón Singleton por frontend para optimizar recursos
- ✅ Tests unitarios completos
- ✅ Documentación y ejemplos incluidos

## 📦 Instalación

### 1. Agregar la librería a tu proyecto

En tu archivo `settings.gradle.kts`:
```kotlin
include(":datastore-wrapper")
```

En tu `build.gradle.kts` del módulo app:
```kotlin
dependencies {
    implementation(project(":datastore-wrapper"))
}
```

### 2. Configurar dependencias

La librería requiere las siguientes dependencias (ya incluidas en el módulo):
- DataStore Preferences
- Kotlinx Coroutines
- Kotlinx Serialization

## 🚀 Uso Básico

### Inicialización

```kotlin
// Obtener instancia para una pantalla específica
val dataStore = DataStoreManager.getInstance(context, "login_screen")
```

Cada `frontendId` crea un DataStore aislado. Ejemplos de IDs:
- `"login_screen"`
- `"profile_screen"`
- `"settings_screen"`

### Guardar Datos Simples

```kotlin
// String
dataStore.saveString("username", "juan@example.com")

// Int
dataStore.saveInt("login_count", 5)

// Boolean
dataStore.saveBoolean("is_logged_in", true)

// Float
dataStore.saveFloat("rating", 4.5f)

// Long
dataStore.saveLong("timestamp", System.currentTimeMillis())
```

### Leer Datos Simples

**Con Flow (reactivo):**
```kotlin
// Observar cambios en tiempo real
dataStore.getString("username", "").collect { username ->
    // Se ejecuta cada vez que cambia el valor
    println("Username: $username")
}
```

**De forma síncrona:**
```kotlin
lifecycleScope.launch {
    val username = dataStore.getStringSync("username", "")
    val loginCount = dataStore.getIntSync("login_count", 0)
    val isLoggedIn = dataStore.getBooleanSync("is_logged_in", false)
}
```

### Guardar Objetos Complejos

La librería proporciona funciones de extensión para trabajar con objetos complejos de forma sencilla:

```kotlin
import com.library.datastorewrapper.saveObject
import com.library.datastorewrapper.getObject
import com.library.datastorewrapper.getObjectSync

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String
)

val dataStore = DataStoreManager.getInstance(context, "user_screen")

// Guardar
val user = User(1, "Juan", "juan@example.com")
dataStore.saveObject("user_profile", user, User.serializer())

// Leer con Flow (reactivo)
dataStore.getObject("user_profile", User.serializer()).collect { user ->
    user?.let {
        println("Usuario: ${it.name}")
    }
}

// Leer de forma síncrona
lifecycleScope.launch {
    val user = dataStore.getObjectSync("user_profile", User.serializer())
    user?.let {
        println("Usuario encontrado: ${it.name}")
    }
}
```

**IMPORTANTE:** 
- Los objetos deben tener la anotación `@Serializable`
- Debes importar las funciones de extensión desde `com.library.datastorewrapper`
- La serialización/deserialización JSON se maneja automáticamente

### Eliminar Datos

```kotlin
// Eliminar una clave específica
dataStore.remove("username")

// Eliminar todos los datos de este frontend
dataStore.clear()
```

### Verificar Existencia

```kotlin
// Verificar si existe una clave
val exists = dataStore.contains("username")

// Obtener todas las claves
val allKeys = dataStore.getAllKeys()
```

## 📱 Ejemplo Completo en Compose

```kotlin
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { 
        DataStoreManager.getInstance(context, "login_screen") 
    }
    
    var username by remember { mutableStateOf("") }
    var loginCount by remember { mutableIntStateOf(0) }
    
    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        dataStore.getString("username").collect { username = it }
        dataStore.getInt("login_count").collect { loginCount = it }
    }
    
    Column {
        TextField(
            value = username,
            onValueChange = { username = it }
        )
        
        Button(onClick = {
            scope.launch {
                dataStore.saveString("username", username)
                dataStore.saveInt("login_count", loginCount + 1)
            }
        }) {
            Text("Guardar")
        }
        
        Text("Inicios de sesión: $loginCount")
    }
}
```

## 🔒 Aislamiento de Datos

La librería garantiza que los datos de diferentes frontends estén completamente aislados:

```kotlin
val loginStore = DataStoreManager.getInstance(context, "login_screen")
val profileStore = DataStoreManager.getInstance(context, "profile_screen")

// Estos datos NO se mezclan
loginStore.saveString("user_id", "123")
profileStore.saveString("user_id", "456")

val loginId = loginStore.getStringSync("user_id") // "123"
val profileId = profileStore.getStringSync("user_id") // "456"
```

## 🧪 Testing

La librería incluye tests unitarios completos que se ejecutan con **Robolectric**, sin necesidad de emulador o dispositivo físico.

### Ejecutar Tests Unitarios (rápido, sin emulador)

```bash
./gradlew :datastore-wrapper:test
```

O desde Android Studio:
```
Click derecho en DataStoreManagerTest.kt → Run 'DataStoreManagerTest'
```

### Diferencia entre Tests Unitarios e Instrumentales

**Tests Unitarios** (`test/`):
- ✅ Se ejecutan en la JVM con Robolectric
- ✅ **Muy rápidos** (segundos)
- ✅ No requieren emulador/dispositivo
- ✅ Ideales para CI/CD
- 📁 Ubicación: `src/test/java/`

**Tests Instrumentales** (`androidTest/`):
- ⏱️ Se ejecutan en emulador/dispositivo real
- ⏱️ Más lentos (minutos)
- ⏱️ Requieren Android Runtime completo
- 📁 Ubicación: `src/androidTest/java/`

Esta librería usa **tests unitarios** para mayor velocidad y facilidad de ejecución.

### Ejemplo de Test

```kotlin
@Test
fun testSaveAndGetString() = runTest {
    val dataStore = DataStoreManager.getInstance(context, "test")
    
    dataStore.saveString("key", "value")
    val result = dataStore.getStringSync("key")
    
    assertEquals("value", result)
}
```

## ⚠️ Manejo de Errores

```kotlin
try {
    dataStore.saveObject("user", user)
} catch (e: DataStoreException) {
    println("Error al guardar: ${e.message}")
}
```

## 📋 API Completa

### Operaciones de String
- `saveString(key: String, value: String)`
- `getString(key: String, defaultValue: String = ""): Flow<String>`
- `getStringSync(key: String, defaultValue: String = ""): String`

### Operaciones de Int
- `saveInt(key: String, value: Int)`
- `getInt(key: String, defaultValue: Int = 0): Flow<Int>`
- `getIntSync(key: String, defaultValue: Int = 0): Int`

### Operaciones de Boolean
- `saveBoolean(key: String, value: Boolean)`
- `getBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean>`
- `getBooleanSync(key: String, defaultValue: Boolean = false): Boolean`

### Operaciones de Float
- `saveFloat(key: String, value: Float)`
- `getFloat(key: String, defaultValue: Float = 0f): Flow<Float>`
- `getFloatSync(key: String, defaultValue: Float = 0f): Float`

### Operaciones de Long
- `saveLong(key: String, value: Long)`
- `getLong(key: String, defaultValue: Long = 0L): Flow<Long>`
- `getLongSync(key: String, defaultValue: Long = 0L): Long`

### Operaciones para Objetos Complejos (con extensiones)

**Importar extensiones:**
```kotlin
import com.library.datastorewrapper.saveObject
import com.library.datastorewrapper.getObject
import com.library.datastorewrapper.getObjectSync
```

**Funciones disponibles:**
- `saveObject<T>(key: String, value: T, serializer: KSerializer<T>)` - Guarda objeto serializado
- `getObject<T>(key: String, serializer: KSerializer<T>): Flow<T?>` - Obtiene objeto como Flow
- `getObjectSync<T>(key: String, serializer: KSerializer<T>): T?` - Obtiene objeto de forma síncrona

### Operaciones de bajo nivel (JSON String)
- `saveObjectAsJson(key: String, jsonString: String)` - Guarda JSON como String
- `getObjectAsJson(key: String): Flow<String>` - Obtiene JSON como Flow
- `getObjectAsJsonSync(key: String): String` - Obtiene JSON de forma síncrona

### Operaciones de Eliminación
- `remove(key: String)`
- `clear()`

### Operaciones de Verificación
- `contains(key: String): Boolean`
- `getAllKeys(): List<String>`

## 🏗️ Arquitectura

```
DataStoreManager
├── Singleton por frontendId
├── DataStore Preferences interno
├── Serialización JSON para objetos complejos
└── API basada en Coroutines/Flow
```

## 💡 Mejores Prácticas

1. **Usa IDs descriptivos para frontends:** `"login_screen"` en lugar de `"screen1"`
2. **Aprovecha Flow para UI reactiva:** Los datos se actualizan automáticamente
3. **Marca objetos como @Serializable:** Necesario para serialización
4. **Maneja excepciones:** Especialmente al trabajar con objetos complejos
5. **Usa clear() con cuidado:** Elimina todos los datos del frontend

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor, asegúrate de:
- Mantener el estilo de código existente
- Agregar tests para nuevas funcionalidades
- Actualizar la documentación

## 📞 Soporte

Para reportar bugs o solicitar funcionalidades, por favor abre un issue en el repositorio.

---

Desarrollado con ❤️ para la comunidad Android
