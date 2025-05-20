package com.flashmob.platonus.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flashmob.platonus.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AuthManager(context: Context) {

    private val ds = context.dataStore
    private val keyToken = stringPreferencesKey("token")
    private val keyUser = stringPreferencesKey("user_json")

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(User::class.java)

    var token: String?
        get() = runBlocking {
            ds.data.first()[keyToken]
        }
        set(value) = runBlocking {
            ds.edit { p ->
                if (value == null) {
                    p.remove(keyToken)
                } else {
                    p[keyToken] = value
                }
            }
        }

    var user: User?
        get() = runBlocking {
            ds.data.first()[keyUser]?.let {
                userAdapter.fromJson(it)
            }
        }
        set(value) = runBlocking {
            ds.edit { p ->
                if (value == null) {
                    p.remove(keyUser)
                } else {
                    p[keyUser] = userAdapter.toJson(value)
                }
            }
        }

    fun clear() = runBlocking {
        ds.edit {
            it.clear()
        }
    }
}