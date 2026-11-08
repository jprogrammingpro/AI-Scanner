package com.jprogrammingpro.aiscanner.pro

/**
 * Single place that decides which features are unlocked.
 * Today everything is free; a later Pro version only needs a new implementation
 * (e.g. backed by Google Play Billing) without touching the feature code.
 */
enum class Feature {
    UNLIMITED_PAGES,
    OFFICE_EXPORT,
    CUSTOM_TEMPLATES,
    EXPENSE_INSIGHTS,
}

interface Entitlements {
    val isPro: Boolean
    fun isUnlocked(feature: Feature): Boolean
}

object FreeEntitlements : Entitlements {
    override val isPro: Boolean = false
    override fun isUnlocked(feature: Feature): Boolean = true
}
