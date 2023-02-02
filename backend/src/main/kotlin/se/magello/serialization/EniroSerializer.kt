package se.magello.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import se.magello.map.Companies
import se.magello.map.EniroPageState

object EniroSerializer: KSerializer<EniroPageState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("EniroPageState")

    override fun deserialize(decoder: Decoder): EniroPageState {
        require(decoder is JsonDecoder) // this class can be decoded only by Json

        val element = decoder.decodeJsonElement()

        if (element !is JsonObject || "pageProps" !in element) {
            throw SerializationException("failed to deserialize to EniroPage object, pageProps where missing")
        }

        val pageProps = element.jsonObject.getValue("pageProps")

        if (pageProps !is JsonObject || "initialState" !in pageProps) {
            throw SerializationException("failed to deserialize to EniroPage object, initialState where missing")
        }

        val initialState = pageProps.jsonObject.getValue("initialState")
        val companyHits = initialState.jsonObject.getValue("companyHits").jsonPrimitive.int
        val companies = initialState.jsonObject.getValue("companies")

        val companiesDecoded =
            decoder.json.decodeFromJsonElement(ListSerializer(Companies.serializer()), companies)

        return EniroPageState(companyHits, companiesDecoded)
    }

    // TODO: Don't need to do this
    override fun serialize(encoder: Encoder, value: EniroPageState) {
        TODO("Not yet implemented")
    }
}