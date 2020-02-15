package info.anodsplace.playstore

import finsky.api.model.DfeModel

/**
 * @author Alex Gavrishev
 * *
 * @date 27/08/2016.
 */
interface PlayStoreEndpoint {
    var authToken: String

    suspend fun start(): DfeModel
    fun reset()
}
