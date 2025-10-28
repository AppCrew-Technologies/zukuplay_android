package dev.anilbeesetti.nextplayer.core.datastore.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import dev.anilbeesetti.nextplayer.core.model.StreamHistory
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object StreamHistorySerializer : Serializer<StreamHistory> {

    private val jsonFormat = Json { ignoreUnknownKeys = true }
    
    override val defaultValue: StreamHistory
        get() = StreamHistory()

    override suspend fun readFrom(input: InputStream): StreamHistory {
        try {
            return jsonFormat.decodeFromString(
                deserializer = StreamHistory.serializer(),
                string = input.readBytes().decodeToString(),
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Cannot read stream history datastore", exception)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: StreamHistory, output: OutputStream) {
        output.write(
            jsonFormat.encodeToString(
                serializer = StreamHistory.serializer(),
                value = t,
            ).encodeToByteArray(),
        )
    }
} 