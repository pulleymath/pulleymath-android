package com.freewheelin.pulley.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KCallable
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.jvmName

class APPreference<T>(private val default: T) {

    companion object {

        private lateinit var sharedPreference: SharedPreferences
        private lateinit var members: Collection<KCallable<*>>
        private lateinit var klass: Any

        fun init(context: Context, klass1: Any) {

            //
            sharedPreference = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

            //
            klass = klass1

            val kclass = Class.forName(klass::class.jvmName).kotlin
            members = kclass.declaredMembers

        }

    }


    @SuppressLint("ApplySharedPref")
    fun set(value: T) {

        val editor = sharedPreference.edit()
        val name = thisFieldName()

        when (default) {
            is Int -> editor.putInt(name, value as Int)
            is Long -> editor.putLong(name, value as Long)
            is Float -> editor.putFloat(name, value as Float)
            is Boolean -> editor.putBoolean(name, value as Boolean)
            is String -> editor.putString(name, value as String)
            else -> throw IllegalArgumentException()
        }

        editor.commit()
    }


    @Suppress("UNCHECKED_CAST")
    fun get(): T {

        val name = thisFieldName()
        val pref = sharedPreference

        return when(default) {
            is Int -> pref.getInt(name, default as Int) as T
            is Long -> pref.getLong(name, default as Long) as T
            is Float -> pref.getFloat(name, default as Float) as T
            is Boolean -> pref.getBoolean(name, default as Boolean) as T
            is String -> pref.getString(name, default as String) as T
            else -> throw IllegalArgumentException()
        }

    }

    private fun thisFieldName(): String {

        for (member in members) {

            if (member.call(klass) == this)
                return member.name

        }

        throw IllegalStateException()
    }


}