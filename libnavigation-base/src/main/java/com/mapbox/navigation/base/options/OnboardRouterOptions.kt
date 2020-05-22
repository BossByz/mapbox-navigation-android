package com.mapbox.navigation.base.options

import android.content.Context

/**
 * Defines options for on-board router. These options enable a feature also known as Free Drive.
 * This allows the navigator to map-match your location onto the road network without a route.
 *
 * @param onboardRouterRouterEndpointOptions options for requesting routing tiles
 * @param tileFilePath File path to store routing tiles
 *  @see [OnboardRouterEndpointOptions.createDefaultTilePath] for a default storage.
 * @param inMemoryTileCache Max size of tiles cache (optional)
 * @param mapMatchingSpatialCache Max size of cache for map matching (optional)
 * @param threadsCount Max count of native threads (optional)
 * @param builder used for updating options
 */
data class OnboardRouterOptions(
    val onboardRouterRouterEndpointOptions: OnboardRouterEndpointOptions,
    val tileFilePath: String,
    val inMemoryTileCache: Int?,
    val mapMatchingSpatialCache: Int?,
    val threadsCount: Int?,
    val builder: Builder
) {
    /**
     * @return the builder that created the [OnboardRouterOptions]
     */
    fun toBuilder() = builder

    companion object {
        /**
         * Create and use the default options
         *
         * @param context is needed for the default destination [OnboardRouterOptions.tileFilePath].
         */
        @JvmStatic
        fun createDefaultOptions(context: Context): OnboardRouterOptions {
            val onboardEndpointOptions = OnboardRouterEndpointOptions.Builder().build()
            return Builder(
                onboardRouterEndpointOptions = onboardEndpointOptions,
                tileDestinationPath = onboardEndpointOptions.createDefaultTilePath(context)
            ).build()
        }
    }

    /**
     * Builder for [OnboardRouterOptions].
     *
     * @param onboardRouterEndpointOptions required source for the router tiles
     * @param tileDestinationPath required file destination for the router tiles
     */
    class Builder(
        private var onboardRouterEndpointOptions: OnboardRouterEndpointOptions,
        private var tileDestinationPath: String
    ) {
        private var inMemoryTileCache: Int? = null
        private var mapMatchingSpatialCache: Int? = null
        private var threadsCount: Int = 2

        /**
         * The source of the onboard router data.
         */
        fun endpoint(onboardRouterEndpointOptions: OnboardRouterEndpointOptions) =
            apply { this.onboardRouterEndpointOptions = onboardRouterEndpointOptions }

        /**
         * File path where tiles will be stored.
         */
        fun tilePath(tilePath: String) =
            apply { this.tileDestinationPath = tilePath }

        /**
         * Map size of tiles cache in bytes
         */
        fun inMemoryTileCache(inMemoryTileCache: Int?) =
            apply { this.inMemoryTileCache = inMemoryTileCache }

        /**
         * Max size of cache for map matching in bytes
         */
        fun mapMatchingSpatialCache(mapMatchingSpatialCache: Int?) =
            apply { this.mapMatchingSpatialCache = mapMatchingSpatialCache }

        /**
         * Max count of native threads
         */
        fun threadsCount(threadsCount: Int) =
            apply { this.threadsCount = threadsCount }

        /**
         * Build the [OnboardRouterOptions]
         */
        fun build() = OnboardRouterOptions(
            onboardRouterRouterEndpointOptions = onboardRouterEndpointOptions,
            tileFilePath = tileDestinationPath,
            inMemoryTileCache = inMemoryTileCache,
            mapMatchingSpatialCache = mapMatchingSpatialCache,
            threadsCount = threadsCount,
            builder = this
        )
    }
}
