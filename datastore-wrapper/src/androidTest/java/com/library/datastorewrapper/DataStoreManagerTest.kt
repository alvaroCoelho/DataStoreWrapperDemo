package com.library.datastorewrapper

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int
)

@Serializable
data class Settings(
    val theme: String,
    val notifications: Boolean,
    val language: String
)

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DataStoreManagerTest {

    private lateinit var context: Context
    private lateinit var dataStoreManager: DataStoreManager
    private val testFrontendId = "test_screen"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dataStoreManager = DataStoreManager.getInstance(context, testFrontendId)
    }

    @After
    fun tearDown() = runTest {
        // Limpiamos todos los datos después de cada test
        dataStoreManager.clear()
    }

    // ==================== Tests para String ====================

    @Test
    fun testSaveAndGetString() = runTest {
        val key = "test_string"
        val value = "Hello World"

        dataStoreManager.saveString(key, value)
        val result = dataStoreManager.getStringSync(key)

        assertEquals(value, result)
    }

    @Test
    fun testGetStringWithDefault() = runTest {
        val key = "nonexistent_string"
        val defaultValue = "default"

        val result = dataStoreManager.getStringSync(key, defaultValue)

        assertEquals(defaultValue, result)
    }

    @Test
    fun testUpdateString() = runTest {
        val key = "update_string"
        val firstValue = "First"
        val secondValue = "Second"

        dataStoreManager.saveString(key, firstValue)
        assertEquals(firstValue, dataStoreManager.getStringSync(key))

        dataStoreManager.saveString(key, secondValue)
        assertEquals(secondValue, dataStoreManager.getStringSync(key))
    }

    // ==================== Tests para Int ====================

    @Test
    fun testSaveAndGetInt() = runTest {
        val key = "test_int"
        val value = 42

        dataStoreManager.saveInt(key, value)
        val result = dataStoreManager.getIntSync(key)

        assertEquals(value, result)
    }

    @Test
    fun testGetIntWithDefault() = runTest {
        val key = "nonexistent_int"
        val defaultValue = 100

        val result = dataStoreManager.getIntSync(key, defaultValue)

        assertEquals(defaultValue, result)
    }

    @Test
    fun testNegativeInt() = runTest {
        val key = "negative_int"
        val value = -999

        dataStoreManager.saveInt(key, value)
        val result = dataStoreManager.getIntSync(key)

        assertEquals(value, result)
    }

    // ==================== Tests para Boolean ====================

    @Test
    fun testSaveAndGetBoolean() = runTest {
        val key = "test_boolean"
        val value = true

        dataStoreManager.saveBoolean(key, value)
        val result = dataStoreManager.getBooleanSync(key)

        assertEquals(value, result)
    }

    @Test
    fun testToggleBoolean() = runTest {
        val key = "toggle_boolean"

        dataStoreManager.saveBoolean(key, true)
        assertTrue(dataStoreManager.getBooleanSync(key))

        dataStoreManager.saveBoolean(key, false)
        assertFalse(dataStoreManager.getBooleanSync(key))
    }

    // ==================== Tests para Float ====================

    @Test
    fun testSaveAndGetFloat() = runTest {
        val key = "test_float"
        val value = 3.14159f

        dataStoreManager.saveFloat(key, value)
        val result = dataStoreManager.getFloatSync(key)

        assertEquals(value, result, 0.0001f)
    }

    // ==================== Tests para Long ====================

    @Test
    fun testSaveAndGetLong() = runTest {
        val key = "test_long"
        val value = 9876543210L

        dataStoreManager.saveLong(key, value)
        val result = dataStoreManager.getLongSync(key)

        assertEquals(value, result)
    }

    // ==================== Tests para Objetos Complejos ====================

    @Test
    fun testSaveAndGetComplexObject() = runTest {
        val key = "test_user"
        val user = User(
            id = 1,
            name = "Juan Pérez",
            email = "juan@example.com",
            age = 30
        )

        // Serializar y guardar
        val jsonString = json.encodeToString(user)
        dataStoreManager.saveObjectAsJson(key, jsonString)

        // Leer y deserializar
        val resultJson = dataStoreManager.getObjectAsJsonSync(key)
        val result = json.decodeFromString<User>(resultJson)

        assertNotNull(result)
        assertEquals(user.id, result.id)
        assertEquals(user.name, result.name)
        assertEquals(user.email, result.email)
        assertEquals(user.age, result.age)
    }

    @Test
    fun testSaveAndGetSettings() = runTest {
        val key = "app_settings"
        val settings = Settings(
            theme = "dark",
            notifications = true,
            language = "es"
        )

        // Serializar y guardar
        val jsonString = json.encodeToString(settings)
        dataStoreManager.saveObjectAsJson(key, jsonString)

        // Leer y deserializar
        val resultJson = dataStoreManager.getObjectAsJsonSync(key)
        val result = json.decodeFromString<Settings>(resultJson)

        assertNotNull(result)
        assertEquals(settings.theme, result.theme)
        assertEquals(settings.notifications, result.notifications)
        assertEquals(settings.language, result.language)
    }

    @Test
    fun testGetNonExistentObject() = runTest {
        val key = "nonexistent_object"

        val result = dataStoreManager.getObjectAsJsonSync(key)

        assertTrue(result.isEmpty())
    }

    @Test
    fun testUpdateComplexObject() = runTest {
        val key = "update_user"
        val user1 = User(1, "Alice", "alice@test.com", 25)
        val user2 = User(2, "Bob", "bob@test.com", 30)

        val json1 = json.encodeToString(user1)
        dataStoreManager.saveObjectAsJson(key, json1)
        var resultJson = dataStoreManager.getObjectAsJsonSync(key)
        var result = json.decodeFromString<User>(resultJson)
        assertEquals("Alice", result.name)

        val json2 = json.encodeToString(user2)
        dataStoreManager.saveObjectAsJson(key, json2)
        resultJson = dataStoreManager.getObjectAsJsonSync(key)
        result = json.decodeFromString<User>(resultJson)
        assertEquals("Bob", result.name)
    }

    // ==================== Tests de Eliminación ====================

    @Test
    fun testRemoveKey() = runTest {
        val key = "removable_key"
        val value = "temporary"

        dataStoreManager.saveString(key, value)
        assertTrue(dataStoreManager.contains(key))

        dataStoreManager.remove(key)
        assertFalse(dataStoreManager.contains(key))
    }

    @Test
    fun testClearAll() = runTest {
        dataStoreManager.saveString("key1", "value1")
        dataStoreManager.saveInt("key2", 42)
        dataStoreManager.saveBoolean("key3", true)

        assertTrue(dataStoreManager.contains("key1"))
        assertTrue(dataStoreManager.contains("key2"))
        assertTrue(dataStoreManager.contains("key3"))

        dataStoreManager.clear()

        assertFalse(dataStoreManager.contains("key1"))
        assertFalse(dataStoreManager.contains("key2"))
        assertFalse(dataStoreManager.contains("key3"))
    }

    // ==================== Tests de Verificación ====================

    @Test
    fun testContains() = runTest {
        val key = "exists_key"

        assertFalse(dataStoreManager.contains(key))

        dataStoreManager.saveString(key, "value")
        assertTrue(dataStoreManager.contains(key))
    }

    @Test
    fun testGetAllKeys() = runTest {
        dataStoreManager.saveString("key1", "value1")
        dataStoreManager.saveInt("key2", 123)
        dataStoreManager.saveBoolean("key3", true)

        val keys = dataStoreManager.getAllKeys()

        assertEquals(3, keys.size)
        assertTrue(keys.contains("key1"))
        assertTrue(keys.contains("key2"))
        assertTrue(keys.contains("key3"))
    }

    // ==================== Tests de Aislamiento entre Frontends ====================

    @Test
    fun testFrontendIsolation() = runTest {
        val frontend1 = DataStoreManager.getInstance(context, "screen1")
        val frontend2 = DataStoreManager.getInstance(context, "screen2")

        val key = "shared_key"
        val value1 = "value_screen1"
        val value2 = "value_screen2"

        frontend1.saveString(key, value1)
        frontend2.saveString(key, value2)

        val result1 = frontend1.getStringSync(key)
        val result2 = frontend2.getStringSync(key)

        assertEquals(value1, result1)
        assertEquals(value2, result2)
        assertNotEquals(result1, result2)

        // Limpieza
        frontend1.clear()
        frontend2.clear()
    }

    @Test
    fun testSingletonPerFrontend() {
        val instance1 = DataStoreManager.getInstance(context, "screen_a")
        val instance2 = DataStoreManager.getInstance(context, "screen_a")
        val instance3 = DataStoreManager.getInstance(context, "screen_b")

        assertSame(instance1, instance2)
        assertNotSame(instance1, instance3)
    }

    // ==================== Tests de Flow ====================

    @Test
    fun testFlowUpdates() = runTest {
        val key = "flow_key"

        dataStoreManager.saveString(key, "initial")

        val flow = dataStoreManager.getString(key)
        assertEquals("initial", flow.first())

        dataStoreManager.saveString(key, "updated")
        assertEquals("updated", flow.first())
    }
}