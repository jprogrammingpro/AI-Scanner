package com.jprogrammingpro.aiscanner.domain

import androidx.annotation.StringRes
import com.jprogrammingpro.aiscanner.R

enum class ScanMode(@StringRes val label: Int, val tuned: Boolean) {
    DOCUMENT(R.string.mode_document, tuned = true),
    PHOTO(R.string.mode_photo, tuned = true),
    BUSINESS_CARD(R.string.mode_business_card, tuned = false),
    ID_CARD(R.string.mode_id_card, tuned = false),
    BOOK(R.string.mode_book, tuned = false),
    WHITEBOARD(R.string.mode_whiteboard, tuned = false),
    RECEIPT(R.string.mode_receipt, tuned = false),
    QR(R.string.mode_qr, tuned = false);

    companion object {
        fun fromName(name: String?): ScanMode = entries.firstOrNull { it.name == name } ?: DOCUMENT
    }
}
