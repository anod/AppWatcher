package finsky.api

import finsky.protos.Details

fun Details.DetailsResponse?.toDocument(): Document? {
    return this?.docV2?.let { Document(it) }
}

fun Details.BulkDetailsResponse?.filterDocuments(filter: FilterPredicate): List<Document> {
    if (this == null) {
        return emptyList()
    }
    return filterResponseDocuments(this, filter)
}

private fun filterResponseDocuments(response: Details.BulkDetailsResponse, filter: FilterPredicate): List<Document> {
    val list = mutableListOf<Document>()
    for (i in response.entryList.indices) {
        response.getEntry(i).doc?.let {
            list.add(Document(it))
        }
    }

    return if (list.isEmpty()) {
        list
    } else {
        list.filter(filter)
    }
}