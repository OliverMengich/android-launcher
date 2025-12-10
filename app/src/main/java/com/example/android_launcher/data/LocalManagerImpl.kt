package com.example.android_launcher.data

import androidx.datastore.core.Serializer
import com.example.android_launcher.domain.manager.LocalManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object LocalManagerImpl: Serializer<LocalManager> {
    override val defaultValue: LocalManager
        get() = LocalManager()

    override suspend fun readFrom(input: InputStream): LocalManager {
        return try {
            Json.decodeFromString(
                deserializer = LocalManager.serializer(),
                string = input.readBytes().decodeToString()
            )
        }catch (e: Exception){
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: LocalManager, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = LocalManager.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}