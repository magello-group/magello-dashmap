package se.magello.salesforce.requests

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Resource("/services/data/")
@Serializable
class SalesForceRequest {
    @Resource("{version}")
    @Serializable
    data class APIVersion(val parent: SalesForceRequest = SalesForceRequest(), val version: String) {
        @Resource("/query")
        @Serializable
        data class Query(val parent: APIVersion, val q: String)
    }
}