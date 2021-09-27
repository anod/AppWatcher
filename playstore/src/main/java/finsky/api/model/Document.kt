package finsky.api.model

import android.util.SparseArray
import finsky.protos.*

class Document(private val doc: DocV2) {

    val appDetails: AppDetails
        get() = this.doc.details.appDetails

    val title: String
        get() = this.doc.title

    val docId: String
        get() = if (this.doc.docid == null) "" else this.doc.docid

    val backend: Int
        get() = this.doc.backendId

    val detailsUrl: String
        get() = if (this.doc.detailsUrl == null) "" else this.doc.detailsUrl

    val creator: String
        get() = if (this.doc.creator == null) "" else this.doc.creator

    // Type 1 ?
    val offer: Common.Offer
        get() {
            var offer = this.doc.offerList.firstOrNull { it.offerType == Common.Offer.TYPE.TYPE_1_VALUE }
            if (offer == null) {
                offer = Common.Offer.getDefaultInstance()
            }
            return offer!!
        }

    val iconUrl: String?
        get() {
            val images = this.imageTypeMap.get(4) ?: return null
            return if (images.isNotEmpty()) {
                images[0].imageUrl
            } else null
        }

    private val imageTypeMap: SparseArray<MutableList<Common.Image>> by lazy {
        val typeMap = SparseArray<MutableList<Common.Image>>()
        for (image2 in this.doc.imageList) {
            val imageType = image2.imageType
            if (typeMap.get(imageType) == null) {
                typeMap.put(imageType, mutableListOf())
            }
            typeMap.get(imageType).add(image2)
        }
        typeMap
    }
}
