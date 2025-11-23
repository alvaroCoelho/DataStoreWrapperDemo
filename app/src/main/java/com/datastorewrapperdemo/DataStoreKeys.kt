package com.datastorewrapperdemo

/**
 * Constantes utilizadas para as chaves dos DataStores na aplicação de demonstração.
 */
object DataStoreKeys {

    // Nomes dos Frontends (DataStore Names)
    const val DATASTORE_NAME_LOGIN = "LOGIN"
    const val DATASTORE_NAME_PROFILE = "PROFILE"
    const val DATASTORE_NAME_SETTINGS = "SETTINGS"

    // Chaves para o DataStore de Login
    const val LOGIN_USERNAME = "username"
    const val LOGIN_COUNT = "login_count"
    const val LOGIN_IS_LOGGED_IN = "is_logged_in"

    // Chaves para o DataStore de Perfil
    const val PROFILE_USER_PROFILE_JSON = "user_profile_json"

    // Chaves para o DataStore de Configurações
    const val SETTINGS_THEME = "app_theme"
    const val SETTINGS_FONT_SIZE = "font_size"
}