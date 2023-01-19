package se.magello.salesforce.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
        @SerialName("Organisationsnummer__c") val organizationId: String
    ): RecordType()

    @Serializable
    // Avtal
    data class Agreement(
        @SerialName("Name") val fullName: String,
        val attributes: Attributes,
        @SerialName("Avtalspart__r") val relatedAccount: RecordType
    ): RecordType()

    companion object {
        const val QUERY = "SELECT Name, Avtalspart__r.Name, Avtalspart__r.Organisationsnummer__c FROM Avtal__c WHERE Levererar_med_egen_konsult__c = TRUE"
    }
}
