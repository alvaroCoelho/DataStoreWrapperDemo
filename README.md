# DataStore Wrapper Library

[![](https://jitpack.io/v/TU-USUARIO/TU-REPO.svg)](https://jitpack.io/#alvaroCoelho/DataStoreWrapperDemo)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)


Librería Android que proporciona un wrapper simple y eficiente sobre DataStore de Android para almacenar datos con aislamiento por app/frontend/pantalla.

---

## 📁 Estructura del Proyecto

Este repositorio contiene:

```
DataStoreWrapper/
├── datastore-wrapper/        # 📦 Módulo de la librería (publicado en JitPack)
│   ├── src/main/            # Código fuente
│   └── src/test/            # Tests unitarios con Robolectric
├── app/                      # 📱 Aplicación de demostración
│   └── src/main/            # Ejemplos de uso
├── jitpack.yml              # Configuración de JitPack
├── LICENSE                   # Licencia MIT
└── README.md                # Este archivo
```

La librería (`datastore-wrapper`) puede ser usada en cualquier proyecto Android mediante JitPack.

---

## 🌟 Características

- ✅ Wrapper sobre DataStore Preferences de Android
- ✅ Aislamiento de datos por app/frontend/pantalla.
- ✅ Soporte para tipos primitivos (String, Int, Boolean, Float, Long)
- ✅ Soporte para objetos complejos mediante serialización JSON
- ✅ API basada en Coroutines y Flow
- ✅ Patrón Singleton por frontend para optimizar recursos
- ✅ Tests unitarios completos con Robolectric (sin emulador)
- ✅ Documentación y ejemplos incluidos
- ✅ Fácil de usar y extensible

---

## 📦 Instalación

### Paso 1: Agregar repositorio JitPack

En tu archivo **`settings.gradle.kts`** (nivel raíz del proyecto):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // ← Agregar esta línea
    }
}
```

### Paso 2: Agregar dependencia

En tu **`app/build.gradle.kts`**:

```kotlin
dependencies {
    implementation("com.github.TU-USUARIO:datastore-wrapper:1.0.0")
    
    // También necesitas kotlinx-serialization para objetos complejos
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
```

### Paso 3: Agregar plugin de serialización (si usas objetos complejos)

En tu **`app/build.gradle.kts`**:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization")  // ← Agregar esta línea
}
```

Y en el **`build.gradle.kts`** raíz:

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0" apply false
}
```

---

## 🚀 Inicio Rápido

### Ejemplo Básico

```kotlin
import com.library.datastorewrapper.DataStoreManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener instancia para una pantalla específica
        val dataStore = DataStoreManager.getInstance(this, "login_screen")
        
        lifecycleScope.launch {
            // Guardar datos
            dataStore.saveString("username", "juan@example.com")
            dataStore.saveInt("login_count", 5)
            dataStore.saveBoolean("is_logged_in", true)
            
            // Leer datos
            val username = dataStore.getStringSync("username")
            val loginCount = dataStore.getIntSync("login_count")
            val isLoggedIn = dataStore.getBooleanSync("is_logged_in")
        }
    }
}
```

---

## 📚 Uso Detallado

### Inicialización

```kotlin
// Obtener instancia para una pantalla específica (Singleton)
val dataStore = DataStoreManager.getInstance(context, "login_screen")
```

Cada `frontendId` crea un DataStore aislado. Ejemplos:
- `"login_screen"`
- `"profile_screen"`
- `"settings_screen"`

### Guardar Datos Simples

```kotlin
lifecycleScope.launch {
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
}
```

### Leer Datos Simples

**Con Flow (reactivo):**
```kotlin
// Se actualiza automáticamente cuando cambia el valor
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

```kotlin
import com.library.datastorewrapper.saveObject
import com.library.datastorewrapper.getObject
import com.library.datastorewrapper.getObjectSync
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String,
    val email: String,
    val age: Int,
    val isPremium: Boolean
)

// Guardar
lifecycleScope.launch {
    val profile = UserProfile("Juan", "juan@example.com", 30, true)
    dataStore.saveObject("user_profile", profile, UserProfile.serializer())
}

// Leer con Flow
dataStore.getObject("user_profile", UserProfile.serializer()).collect { profile ->
    profile?.let {
        println("Usuario: ${it.name}, Edad: ${it.age}")
    }
}

// Leer de forma síncrona
lifecycleScope.launch {
    val profile = dataStore.getObjectSync("user_profile", UserProfile.serializer())
    profile?.let {
        println("Usuario encontrado: ${it.name}")
    }
}
```

**IMPORTANTE:** 
- Los objetos deben tener la anotación `@Serializable`
- Debes importar las funciones de extensión: `saveObject`, `getObject`, `getObjectSync`

### Eliminar Datos

```kotlin
lifecycleScope.launch {
    // Eliminar una clave específica
    dataStore.remove("username")
    
    // Eliminar todos los datos de este frontend
    dataStore.clear()
}
```

### Verificar Existencia

```kotlin
lifecycleScope.launch {
    // Verificar si existe una clave
    val exists = dataStore.contains("username")
    
    // Obtener todas las claves almacenadas
    val allKeys = dataStore.getAllKeys()
    println("Claves: $allKeys")
}
```

---

## 🎨 Ejemplo Completo en Compose

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

---

## 🔒 Aislamiento de Datos

La librería garantiza que los datos de diferentes frontends estén completamente aislados:

```kotlin
val loginStore = DataStoreManager.getInstance(context, "login_screen")
val profileStore = DataStoreManager.getInstance(context, "profile_screen")

// Estos datos NO se mezclan
loginStore.saveString("user_id", "123")
profileStore.saveString("user_id", "456")

val loginId = loginStore.getStringSync("user_id")    // "123"
val profileId = profileStore.getStringSync("user_id")  // "456"
```

---

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
- `saveObject<T>(key: String, value: T, serializer: KSerializer<T>)`
- `getObject<T>(key: String, serializer: KSerializer<T>): Flow<T?>`
- `getObjectSync<T>(key: String, serializer: KSerializer<T>): T?`

### Operaciones de Eliminación
- `remove(key: String)` - Elimina una clave específica
- `clear()` - Elimina todos los datos del frontend

### Operaciones de Verificación
- `contains(key: String): Boolean` - Verifica si existe una clave
- `getAllKeys(): List<String>` - Obtiene todas las claves almacenadas

---

## 🧪 Testing

La librería incluye tests unitarios completos que se ejecutan con **Robolectric**, sin necesidad de emulador.

### Ejecutar Tests

```bash
# Tests unitarios (rápido, sin emulador)
./gradlew :datastore-wrapper:test

# Ver reporte HTML
open datastore-wrapper/build/reports/tests/testDebugUnitTest/index.html
```

### Ventajas de Tests Unitarios con Robolectric

- ✅ **Muy rápidos** (1-5 segundos)
- ✅ **Sin emulador** - No necesitas dispositivo Android
- ✅ **Perfecto para CI/CD** - Integración continua
- ✅ **Context de Android** - Simula el entorno Android completo

---

## 🏗️ Arquitectura

```
DataStoreManager
├── Singleton por frontendId
├── DataStore Preferences interno
├── Serialización JSON para objetos complejos (kotlinx.serialization)
├── API basada en Coroutines/Flow
└── Funciones de extensión para objetos complejos
```

---

## 💡 Mejores Prácticas

1. **Usa IDs descriptivos para frontends:** `"login_screen"` en lugar de `"screen1"`
2. **Aprovecha Flow para UI reactiva:** Los datos se actualizan automáticamente
3. **Marca objetos como @Serializable:** Necesario para objetos complejos
4. **Maneja excepciones:** Especialmente al trabajar con objetos complejos
5. **Usa clear() con cuidado:** Elimina todos los datos del frontend
6. **Organiza las claves:** Usa constantes para evitar errores de tipeo

### Ejemplo de Constantes

```kotlin
object DataStoreKeys {
    // Nombres de DataStores
    const val LOGIN_SCREEN = "login_screen"
    const val PROFILE_SCREEN = "profile_screen"
    
    // Claves
    const val USERNAME = "username"
    const val LOGIN_COUNT = "login_count"
    const val USER_PROFILE = "user_profile"
}
```

---

## 📖 Documentación Adicional

- **[PUBLICAR_JITPACK.md](PUBLICAR_JITPACK.md)** - Guía completa para publicar en JitPack
- **[CHECKLIST_JITPACK.md](CHECKLIST_JITPACK.md)** - Lista de verificación rápida
- **[GUÍA_REORGANIZACIÓN.md](GUÍA_REORGANIZACIÓN.md)** - Opciones de estructura del proyecto

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
- Mantén el estilo de código existente
- Agrega tests para nuevas funcionalidades
- Actualiza la documentación

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

```
MIT License

Copyright (c) 2024 [Tu Nombre]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 🙏 Agradecimientos

- Basado en [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) de Android
- Usa [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) para objetos complejos
- Tests con [Robolectric](http://robolectric.org/)

---

## 📞 Soporte

- 🐛 **Reportar bugs**: [Issues](https://github.com/TU-USUARIO/TU-REPO/issues)
- 💡 **Solicitar features**: [Issues](https://github.com/TU-USUARIO/TU-REPO/issues)
- 📧 **Contacto**: tu-email@example.com

---

## 📊 Versiones

### v1.0.0 (Actual)
- ✅ Wrapper completo de DataStore
- ✅ Soporte para tipos primitivos
- ✅ Soporte para objetos complejos
- ✅ Aislamiento por frontend
- ✅ Tests unitarios con Robolectric
- ✅ Documentación completa

---

## 🔗 Enlaces Útiles

- [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [JitPack](https://jitpack.io)

---

**Desarrollado con ❤️ para la comunidad Android**

[![Made with Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-0095D5.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg)](https://android.com)

## 📁 Estructura del Proyecto

```
DataStoreWrapperDemo/
├── datastore-wrapper/        # 📦 Módulo de la librería (reutilizable)
│   ├── src/main/            # Código fuente de la librería
│   └── src/test/            # Tests unitarios
├── app/                      # 📱 Aplicación de demostración
│   └── src/main/            # Código de ejemplo usando la librería
└── README.md                # Este archivo
```

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

### Paso 1: Agregar repositorio JitPack

En tu archivo `settings.gradle.kts` (raíz del proyecto):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // ← Agregar esta línea
    }
}
```

### Paso 2: Agregar dependencia

En tu `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.tu-usuario:datastore-wrapper:1.0.0")
}
```

**Nota**: Reemplaza `tu-usuario` con tu usuario de GitHub y `1.0.0` con la versión deseada.

### Para este Proyecto de Demostración

Si estás trabajando con el código fuente, la librería ya está incluida:

```kotlin
dependencies {
    implementation(project(":datastore-wrapper"))
}
```

## 🚀 Inicio Rápido

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

## 🎓 Notas sobre Arquitectura

### ¿Por qué Librería y Demo en el Mismo Proyecto?

Esta estructura (librería + demo juntos) es común para:
- ✅ **Proyectos educativos** - Facilita la demostración y evaluación
- ✅ **Desarrollo inicial** - Permite iterar rápidamente
- ✅ **Documentación viva** - La app de demo sirve como ejemplos

### Para Producción

En un entorno profesional, se recomienda:
1. **Repositorio separado** para la librería
2. **Publicación en JitPack/Maven Central**
3. **Versionado semántico** (1.0.0, 1.1.0, etc.)
4. **CI/CD** para publicación automática
5. **Changelog** documentando cambios

Ver `GUÍA_REORGANIZACIÓN.md` para más detalles sobre cómo migrar a esta estructura.

---

Desarrollado con ❤️ para la comunidad Android
