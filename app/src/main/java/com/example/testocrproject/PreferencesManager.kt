package com.example.testocrproject

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages application preferences for storing configuration data like base URL and theme.
 * Uses SharedPreferences to persist data across app sessions.
 */
class PreferencesManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "ocr_app_preferences"
        private const val KEY_BASE_IP = "base_ip_address"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val DEFAULT_IP = "192.168.103.82"
        private const val HTTP_PREFIX = "http://"
        private const val PORT = ":5000"
        
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        /**
         * Gets singleton instance of PreferencesManager
         */
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Saves the IP address portion of the base URL
     * @param ip The IP address without http:// prefix or port
     */
    fun saveIpAddress(ip: String) {
        sharedPreferences.edit().putString(KEY_BASE_IP, ip).apply()
    }
    
    /**
     * Gets the stored IP address, or default if not set
     * @return The IP address without http:// prefix or port
     */
    fun getIpAddress(): String {
        return sharedPreferences.getString(KEY_BASE_IP, DEFAULT_IP) ?: DEFAULT_IP
    }
    
    /**
     * Gets the complete base URL with http:// prefix and port
     * Format: http://[IP_ADDRESS]:5000/
     * @return Complete base URL ready to use with Retrofit
     */
    fun getBaseUrl(): String {
        val ip = getIpAddress()
        return "$HTTP_PREFIX$ip$PORT/"
    }
    
    /**
     * Checks if the base URL has been configured (different from default)
     * @return true if custom IP is set, false if using default
     */
    fun isCustomIpConfigured(): Boolean {
        val currentIp = getIpAddress()
        return currentIp != DEFAULT_IP && currentIp.isNotEmpty()
    }
    
    /**
     * Resets the IP address to default value
     */
    fun resetToDefault() {
        saveIpAddress(DEFAULT_IP)
    }
    
    /**
     * Validates if the provided string is a valid IP address format
     * @param ip The IP address string to validate
     * @return true if valid IP format, false otherwise
     */
    fun isValidIpAddress(ip: String): Boolean {
        if (ip.isBlank()) return false
        
        // Check for basic IP format (xxx.xxx.xxx.xxx)
        val ipPattern = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""")
        val matchResult = ipPattern.matchEntire(ip) ?: return false
        
        // Validate each octet is in range 0-255
        return matchResult.groupValues.drop(1).all { octet ->
            val value = octet.toIntOrNull() ?: return false
            value in 0..255
        }
    }
    
    // ========== Theme Management ==========
    
    /**
     * Saves the theme mode preference
     * @param themeMode The theme mode to save
     */
    fun saveThemeMode(themeMode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
    }
    
    /**
     * Gets the stored theme mode, or SYSTEM as default
     * @return The stored theme mode
     */
    fun getThemeMode(): ThemeMode {
        val themeName = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(themeName ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
}

/**
 * Enum representing available theme modes
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

