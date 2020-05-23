package com.mapbox.navigation.base.options

import android.content.Context
import java.io.File
import java.net.URI

/**
 * Defines endpoint's properties to retrieve routing tiles.
 *
 * @param tilesUri tiles endpoint
 * @param version version of tiles
 * @param builder used for updating options
 */
data class OnboardRouterEndpointOptions(
    val tilesUri: URI,
    val version: String,
    val builder: Builder
) {
    /**
     * @return the builder that created the [OnboardRouterEndpointOptions]
     */
    fun toBuilder() = builder

    /**
     * Creates a path to store the road network tiles.
     */
    fun createDefaultTilePath(context: Context): String {
        val directoryVersion = "Offline/${tilesUri.host}/$version/tiles"
        return File(context.filesDir, directoryVersion).absolutePath
    }

    /**
     * Build the [OnboardRouterEndpointOptions]
     */
    class Builder {
        private var tilesUri: String = "https://api.mapbox.com"
        private var version: String = "2020_02_02-03_00_00"

        /**
         * Override the routing tiles endpoint.
         *
         * @return Builder
         */
        fun tilesUri(host: String) =
            apply { this.tilesUri = host }

        /**
         * Override the routing tiles version.
         */
        fun version(version: String) =
            apply { this.version = version }

        /**
         * Build the [OnboardRouterEndpointOptions]
         */
        fun build() = OnboardRouterEndpointOptions(
            tilesUri = URI(tilesUri),
            version = version,
            builder = this
        )
    }
}
