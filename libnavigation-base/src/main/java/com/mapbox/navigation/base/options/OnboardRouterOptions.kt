package com.mapbox.navigation.base.options

import android.content.Context
import java.io.File
import java.net.URI

/**
 * Defines options for on-board router. These options enable a feature also known as Free Drive.
 * This allows the navigator to map-match your location onto the road network without a route.
 *
 * @param tilesUri tiles endpoint
 * @param version version of tiles
 * @param builder used for updating options
 */
data class OnboardRouterOptions(
    val tilesUri: URI,
    val version: String,
    val filePath: String,
    val builder: Builder
) {
    /**
     * @return the builder that created the [OnboardRouterOptions]
     */
    fun toBuilder() = builder

    /**
     * Builder for [OnboardRouterOptions].
     */
    class Builder {
        private var tilesUri: URI = URI("https://api.mapbox.com")
        private var version: String = "2020_02_02-03_00_00"

        /**
         * Override the routing tiles endpoint.
         */
        fun tilesUri(tilesUri: String) =
            apply { this.tilesUri = URI(tilesUri) }

        fun tilesUri(tilesUri: URI) =
            apply { this.tilesUri = tilesUri }

        /**
         * Override the routing tiles version.
         */
        fun version(version: String) =
            apply { this.version = version }

        /**
         * Creates a path to store the road network tiles.
         */
        private fun defaultInternalDirectory(context: Context): String {
            val directoryVersion = "Offline/${tilesUri.host}/$version/tiles"
            return File(context.filesDir, directoryVersion).absolutePath
        }

        /**
         * Build the [OnboardRouterOptions]
         */
        fun build(context: Context) = OnboardRouterOptions(
                tilesUri = tilesUri,
                version = version,
                filePath = defaultInternalDirectory(context),
                builder = this
        )
    }
}
