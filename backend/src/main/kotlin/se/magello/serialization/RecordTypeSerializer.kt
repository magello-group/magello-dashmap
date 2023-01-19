package se.magello.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import se.magello.salesforce.responses.RecordType

object RecordTypeSerializer : KSerializer<RecordType> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("RecordType")

    override fun deserialize(decoder: Decoder): RecordType {
        require(decoder is JsonDecoder) // this class can be decoded only by Json

        val element = decoder.decodeJsonElement()

        if (element !is JsonObject || "attributes" !in element) {
            throw SerializationException("unknown object")
        }

        val attribute = element.jsonObject.getValue("attributes")

        return when (attribute.jsonObject["type"]?.jsonPrimitive?.content) {
            "Avtal__c" -> decoder.json.decodeFromJsonElement(RecordType.Agreement.serializer(), element)
            "Account" -> decoder.json.decodeFromJsonElement(RecordType.Account.serializer(), element)
            else -> throw SerializationException("Unknown type ${attribute.jsonObject["type"]}")
        }
    }

    // TODO: Do we really want to support this?
    override fun serialize(encoder: Encoder, value: RecordType) {
        require(encoder is JsonEncoder)

        when (value) {
            is RecordType.Agreement -> encoder.encodeSerializableValue(RecordType.Agreement.serializer(), value)
            is RecordType.Account -> encoder.encodeSerializableValue(RecordType.Account.serializer(), value)
        }
    }
}