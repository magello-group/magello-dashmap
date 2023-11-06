package se.magello.salesforce.responses

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se.magello.serialization.InstantDateSerializer
import se.magello.serialization.RecordTypeSerializer

@Serializable
data class QueryResponse(
    val done: Boolean,
    val records: List<RecordType>,
    val totalSize: Int,
    val nextRecordsUrl: String = ""
)

@Serializable
data class Attributes(
    val url: String
)

@Serializable(with = RecordTypeSerializer::class)
sealed class RecordType {
    @Serializable
    data class Account(
        @SerialName("Name") val name: String,
        val attributes: Attributes,
        @SerialName("Organisationsnummer__c") val organisationId: String
    ): RecordType()

    @Serializable
    data class Agreement(
        @SerialName("Name") val fullName: String,
        val attributes: Attributes,
        @Serializable(with = InstantDateSerializer::class) @SerialName("Slutdatum__c") val endDate: Instant,
        @SerialName("Avtalspart__r") val relatedAccount: RecordType
    ): RecordType()

    companion object {
        const val QUERY = "SELECT Name, Slutdatum__c, Avtalspart__r.Name, Avtalspart__r.Organisationsnummer__c FROM Avtal__c WHERE Levererar_med_egen_konsult__c = TRUE"
    }
}
