package com.shabinder.common.providers.placeholders

import com.russhwolf.settings.Settings
import com.shabinder.common.core_components.preference_manager.PreferenceManager

private val settings = object : Settings {
    override val keys: Set<String> = setOf()
    override val size: Int = 0
    override fun clear() {}
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = false
    override fun getBooleanOrNull(key: String): Boolean? = null
    override fun getDouble(key: String, defaultValue: Double): Double = 0.0
    override fun getDoubleOrNull(key: String): Double? = null
    override fun getFloat(key: String, defaultValue: Float): Float = 0f
    override fun getFloatOrNull(key: String): Float? = null
    override fun getInt(key: String, defaultValue: Int): Int = 0
    override fun getIntOrNull(key: String): Int? = null
    override fun getLong(key: String, defaultValue: Long): Long = 0L
    override fun getLongOrNull(key: String): Long? = null
    override fun getString(key: String, defaultValue: String): String = ""
    override fun getStringOrNull(key: String): String? = null
    override fun hasKey(key: String): Boolean = false
    override fun putBoolean(key: String, value: Boolean) {}
    override fun putDouble(key: String, value: Double) {}
    override fun putFloat(key: String, value: Float) {}
    override fun putInt(key: String, value: Int) {}
    override fun putLong(key: String, value: Long) {}
    override fun putString(key: String, value: String) {}
    override fun remove(key: String) {}
}
val PreferenceManagerPlaceholder = PreferenceManager(settings)
