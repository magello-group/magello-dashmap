package se.magello.map

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
import se.magello.serialization.EniroSerializer

@Serializable(with = EniroSerializer::class)
data class EniroPageState(val companyHits: Int, val companies: List<Companies>)

@Serializable
data class Companies(val name: String, val organisationNumber: String, val addresses: List<Address>)

@Serializable
data class Address(
    val streetName: String,
    val streetNumber: String,
    val postalCode: String,
    val municipality: String?,
    val coordinates: List<Coordinates>
)

@Serializable
data class Coordinates(val lon: Double, val lat: Double, val type: String)

@Serializable
@Resource("/_next/data/mYQ9CY8tN8N-rFJV1gRc5/sv/search")
class EniroRequest {
    @Serializable
    @Resource("/{organisationNumber}/companies/1.json")
    data class CompanySearch(val parent: EniroRequest = EniroRequest(), val organisationNumber: String)
}