package com.library.datastorewrapper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Clase helper para serialización/deserialización de objetos complejos.
 * Usa esta clase para convertir objetos a/desde JSON de forma segura.
 */
object DataStoreSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    /**
     * Serializa un objeto a JSON String.
     *
     * @param serializer Serializer del tipo T
     * @param value Objeto a serializar
     * @return String JSON del objeto
     */
    fun <T> encode(serializer: KSerializer<T>, value: T): String {
        return json.encodeToString(serializer, value)
    }

    /**
     * Deserializa un JSON String a objeto.
     *
     * @param serializer Serializer del tipo T
     * @param jsonString String JSON a deserializar
     * @return Objeto deserializado o null si el string está vacío
     */
    fun <T> decode(serializer: KSerializer<T>, jsonString: String): T? {
        return if (jsonString.isEmpty()) {
            null
        } else {
            json.decodeFromString(serializer, jsonString)
        }
    }
}

/**
 * Extensiones para facilitar el uso de objetos complejos con DataStoreManager.
 */

/**
 * Guarda un objeto complejo en DataStore.
 *
 * Ejemplo:
 * ```
 * val user = User(1, "Juan", "juan@example.com")
 * dataStore.saveObject("user", user, User.serializer())
 * ```
 */
suspend fun <T> DataStoreManager.saveObject(
    key: String,
    value: T,
    serializer: KSerializer<T>
) {
    val jsonString = DataStoreSerializer.encode(serializer, value)
    saveObjectAsJson(key, jsonString)
}

/**
 * Obtiene un objeto complejo desde DataStore.
 *
 * Ejemplo:
 * ```
 * dataStore.getObject("user", User.serializer()).collect { user ->
 *     user?.let { println("Usuario: ${it.name}") }
 * }
 * ```
 */
fun <T> DataStoreManager.getObject(
    key: String,
    serializer: KSerializer<T>
): Flow<T?> {
    return getObjectAsJson(key).map { jsonString ->
        DataStoreSerializer.decode(serializer, jsonString)
    }
}

/**
 * Obtiene un objeto complejo de forma síncrona.
 *
 * Ejemplo:
 * ```
 * val user = dataStore.getObjectSync("user", User.serializer())
 * ```
 */
suspend fun <T> DataStoreManager.getObjectSync(
    key: String,
    serializer: KSerializer<T>
): T? {
    val jsonString = getObjectAsJsonSync(key)
    return DataStoreSerializer.decode(serializer, jsonString)
}