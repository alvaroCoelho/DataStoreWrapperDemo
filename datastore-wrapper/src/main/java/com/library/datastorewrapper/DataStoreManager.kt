package com.library.datastorewrapper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


/**
 * Clase principal que gestiona el almacenamiento de datos usando DataStore.
 * Garantiza el aislamiento de datos por:
 * 1. Nombre del Paquete de la aplicación (packageName), para evitar colisiones entre diferentes builds o apps.
 * 2. Identificador modular (frontendId), para agrupar datos internamente (ej: "login_screen", "profile_screen").
 *
 * @param context Contexto de la aplicación
 * @param frontendId Identificador modular para agrupar datos (ej: "login_screen", "profile_screen")
 */
class DataStoreManager(
    private val context: Context,
    private val frontendId: String
) {
    // 1. CONSTRUCCIÓN DEL NOMBRE ÚNICO DEL ALMACÉN (storeName)
    // Combina el PackageName (ej: com.my.app) con el frontendId (ej: PROFILE_SETTINGS)
    private val storeName = "${context.packageName}_datastore_$frontendId"

    // Extensión para crear una instancia de DataStore única por storeName (que incluye el packageName)
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = storeName
    )

    private val dataStore = context.dataStore

    companion object {
        // Cache de instancias. La clave debe ser única para (PackageName + frontendId)
        private val instances = mutableMapOf<String, DataStoreManager>()

        /**
         * Obtiene una instancia de DataStoreManager para un frontend específico.
         * La instancia y el DataStore subyacente están aislados por el nombre del paquete de la aplicación (packageName).
         * Usa el patrón Singleton por (PackageName + frontendId) para optimizar recursos.
         */
        fun getInstance(context: Context, frontendId: String): DataStoreManager {
            // Genera una clave de caché única que incluye el nombre del paquete para el aislamiento
            val uniqueKey = "${context.packageName}__$frontendId"

            return instances.getOrPut(uniqueKey) {
                // El constructor usará internamente context.packageName para el nombre del archivo.
                DataStoreManager(context.applicationContext, frontendId)
            }
        }
    }

    // ==================== Operaciones para String ====================

    /**
     * Guarda un valor String en el DataStore.
     * @param key Clave única para identificar el dato
     * @param value Valor String a guardar
     */
    suspend fun saveString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    /**
     * Obtiene un valor String del DataStore.
     * @param key Clave del dato a recuperar
     * @param defaultValue Valor por defecto si no existe la clave
     * @return Flow que emite el valor almacenado o el valor por defecto
     */
    fun getString(key: String, defaultValue: String = ""): Flow<String> {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

    /**
     * Obtiene un valor String de forma síncrona (suspendible).
     * Útil para casos donde necesitas el valor inmediatamente.
     */
    suspend fun getStringSync(key: String, defaultValue: String = ""): String {
        return getString(key, defaultValue).first()
    }

    // ==================== Operaciones para Int ====================

    /**
     * Guarda un valor Int en el DataStore.
     * @param key Clave única para identificar el dato
     * @param value Valor Int a guardar
     */
    suspend fun saveInt(key: String, value: Int) {
        val prefKey = intPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    /**
     * Obtiene un valor Int del DataStore.
     * @param key Clave del dato a recuperar
     * @param defaultValue Valor por defecto si no existe la clave
     * @return Flow que emite el valor almacenado o el valor por defecto
     */
    fun getInt(key: String, defaultValue: Int = 0): Flow<Int> {
        val prefKey = intPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

    /**
     * Obtiene un valor Int de forma síncrona (suspendible).
     */
    suspend fun getIntSync(key: String, defaultValue: Int = 0): Int {
        return getInt(key, defaultValue).first()
    }

    // ==================== Operaciones para Boolean ====================

    suspend fun saveBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        val prefKey = booleanPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

    suspend fun getBooleanSync(key: String, defaultValue: Boolean = false): Boolean {
        return getBoolean(key, defaultValue).first()
    }

    // ==================== Operaciones para Float ====================

    suspend fun saveFloat(key: String, value: Float) {
        val prefKey = floatPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Flow<Float> {
        val prefKey = floatPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

    suspend fun getFloatSync(key: String, defaultValue: Float = 0f): Float {
        return getFloat(key, defaultValue).first()
    }

    // ==================== Operaciones para Long ====================

    suspend fun saveLong(key: String, value: Long) {
        val prefKey = longPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Flow<Long> {
        val prefKey = longPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

    suspend fun getLongSync(key: String, defaultValue: Long = 0L): Long {
        return getLong(key, defaultValue).first()
    }

    // ==================== Operaciones para Objetos Complejos ====================

    /**
     * Guarda un objeto complejo serializándolo a JSON como String.
     * El objeto debe ser serializable con kotlinx.serialization.
     *
     * @param key Clave única para identificar el dato
     * @param jsonString String JSON del objeto serializado
     *
     * Ejemplo de uso:
     * val user = User(1, "Juan", "juan@example.com")
     * val json = Json.encodeToString(User.serializer(), user)
     * dataStore.saveObjectAsJson("user", json)
     */
    suspend fun saveObjectAsJson(key: String, jsonString: String) {
        saveString(key, jsonString)
    }

    /**
     * Obtiene un objeto complejo como String JSON.
     *
     * @param key Clave del dato a recuperar
     * @return Flow que emite el String JSON o vacío si no existe
     */
    fun getObjectAsJson(key: String): Flow<String> {
        return getString(key, "")
    }

    /**
     * Obtiene un objeto complejo como String JSON de forma síncrona.
     */
    suspend fun getObjectAsJsonSync(key: String): String {
        return getStringSync(key, "")
    }

    // ==================== Operaciones de Eliminación ====================

    /**
     * Elimina un dato específico del DataStore.
     * @param key Clave del dato a eliminar
     */
    suspend fun remove(key: String) {
        dataStore.edit { preferences ->
            // Intentamos eliminar con todos los tipos posibles
            preferences.remove(stringPreferencesKey(key))
            preferences.remove(intPreferencesKey(key))
            preferences.remove(booleanPreferencesKey(key))
            preferences.remove(floatPreferencesKey(key))
            preferences.remove(longPreferencesKey(key))
        }
    }

    /**
     * Elimina todos los datos almacenados en este frontend.
     * Usar con precaución.
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // ==================== Operaciones de Verificación ====================

    /**
     * Verifica si existe una clave en el DataStore.
     * @param key Clave a verificar
     * @return true si la clave existe, false en caso contrario
     */
    suspend fun contains(key: String): Boolean {
        val preferences = dataStore.data.first()
        return preferences.asMap().keys.any { it.name == key }
    }

    /**
     * Obtiene todas las claves almacenadas en este frontend.
     * @return Lista de nombres de claves
     */
    suspend fun getAllKeys(): List<String> {
        val preferences = dataStore.data.first()
        return preferences.asMap().keys.map { it.name }
    }
}

/**
 * Excepción personalizada para errores de DataStore.
 */
class DataStoreException(message: String, cause: Throwable? = null) : Exception(message, cause)