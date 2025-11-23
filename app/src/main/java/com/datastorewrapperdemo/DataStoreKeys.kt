package com.datastorewrapperdemo

/**
 * Constantes utilizadas para las claves de los DataStores en la aplicación de demostración.
 */
object DataStoreKeys {

    // Nombres de los Frontends (DataStore Names)
    const val DATASTORE_NAME_LOGIN = "LOGIN"
    const val DATASTORE_NAME_PROFILE = "PROFILE"
    const val DATASTORE_NAME_SETTINGS = "SETTINGS"

    // Claves para el DataStore de Inicio de Sesión
    const val LOGIN_USERNAME = "username"
    const val LOGIN_COUNT = "login_count"
    const val LOGIN_IS_LOGGED_IN = "is_logged_in"

    // Claves para el DataStore de Perfil
    const val PROFILE_USER_PROFILE_JSON = "user_profile_json"

    // Claves para el DataStore de Configuración
    const val SETTINGS_THEME = "app_theme"
    const val SETTINGS_FONT_SIZE = "font_size"
}