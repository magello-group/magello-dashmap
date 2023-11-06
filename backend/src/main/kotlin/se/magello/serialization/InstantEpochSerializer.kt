package se.magello.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

object InstantEpochSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantEpoch", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeLong(value.toEpochMilli())
    override fun deserialize(decoder: Decoder): Instant = Instant.ofEpochMilli(decoder.decodeLong())
}

object InstantDateSerializer : KSerializer<Instant> {
    private val format = SimpleDateFormat("yyyy-MM-dd")
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(format.format(Date.from(value)))
    override fun deserialize(decoder: Decoder): Instant = format.parse(decoder.decodeString()).toInstant()
}
