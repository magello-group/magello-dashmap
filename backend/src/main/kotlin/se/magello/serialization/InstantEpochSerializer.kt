package se.magello.serialization

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = Instant::class)
@OptIn(ExperimentalSerializationApi::class)
object InstantEpochSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantEpoch", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeLong(value.toEpochMilli())
    override fun deserialize(decoder: Decoder): Instant = Instant.ofEpochMilli(decoder.decodeLong())
}

@Serializer(forClass = Instant::class)
@OptIn(ExperimentalSerializationApi::class)
object InstantDateSerializer : KSerializer<Instant> {
    private val format = SimpleDateFormat("yyyy-MM-dd")
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(format.format(Date.from(value)))
    override fun deserialize(decoder: Decoder): Instant = format.parse(decoder.decodeString()).toInstant()
}
