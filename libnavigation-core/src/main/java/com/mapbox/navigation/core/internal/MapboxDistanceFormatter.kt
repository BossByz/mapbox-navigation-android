package com.mapbox.navigation.core.internal

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.navigation.base.internal.VoiceUnit
import com.mapbox.navigation.base.internal.extensions.LocaleEx.getUnitTypeForLocale
import com.mapbox.navigation.base.internal.extensions.inferDeviceLocale
import com.mapbox.navigation.core.R
import com.mapbox.navigation.core.Rounding
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfConversion
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Creates an instance of DistanceFormatter, which can format distances in meters
 * based on a language format and unit type.
 *
 *
 * This constructor will infer device language and unit type using the device locale.
 *
 * @param applicationContext from which to get localized strings from
 * @param locale the locale to use for localization of distance resources
 * @param unitType to use, or UNDEFINED to use default for locale country
 * @param roundingIncrement increment by which to round small distances
 * @param builder used for updating options
 */
class MapboxDistanceFormatter private constructor(
    private val applicationContext: Context,
    private val locale: Locale,
    @VoiceUnit.Type unitType: String,
    @Rounding.Increment private val roundingIncrement: Int,
    private val builder: Builder
) : DistanceFormatter {

    private val smallUnit = when (unitType) {
        VoiceUnit.IMPERIAL -> TurfConstants.UNIT_FEET
        else -> TurfConstants.UNIT_METERS
    }

    private val largeUnit = when (unitType) {
        VoiceUnit.IMPERIAL -> TurfConstants.UNIT_MILES
        else -> TurfConstants.UNIT_KILOMETERS
    }

    companion object {
        private const val smallDistanceUpperThresholdInMeters = 400.0
        private const val mediumDistanceUpperThresholdInMeters = 10000.0

        /**
         * A new instance of [MapboxDistanceFormatter.Builder]
         *
         * @return Builder
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    /**
     * @return the [Builder] that created the [MapboxDistanceFormatter]
     */
    fun toBuilder() = builder

    /**
     * Builder of [MapboxDistanceFormatter]
     */
    class Builder {
        private var unitType: String? = null
        private var locale: Locale? = null
        private var roundingIncrement = 0

        /**
         * Policy for the various units of measurement, UNDEFINED uses default for locale country
         * @param unitType String
         * @return Builder
         */
        fun withUnitType(@VoiceUnit.Type unitType: String) =
            apply { this.unitType = unitType }

        /**
         * Minimal value that distance might be stripped
         *
         * @see [Rounding.Increment]
         * @return Builder
         */
        fun withRoundingIncrement(@Rounding.Increment roundingIncrement: Int) =
            apply { this.roundingIncrement = roundingIncrement }

        /**
         * Use a non-default [Locale]. By default, the [Locale] is used from applicationContext
         *
         * @return Builder
         */
        fun withLocale(locale: Locale) =
            apply { this.locale = locale }

        /**
         * Build a new instance of [MapboxDistanceFormatter]
         *
         * @param context converted to applicationContext to save memory leaks
         */
        fun build(context: Context): MapboxDistanceFormatter {
            val localeToUse: Locale = locale ?: context.applicationContext.inferDeviceLocale()
            val unitTypeToUse: String = when (unitType) {
                null -> localeToUse.getUnitTypeForLocale()
                VoiceUnit.UNDEFINED -> localeToUse.getUnitTypeForLocale()
                else -> unitType!!
            }

            return MapboxDistanceFormatter(
                applicationContext = context.applicationContext,
                locale = localeToUse,
                unitType = unitTypeToUse,
                roundingIncrement = roundingIncrement,
                builder = this)
        }
    }

    /**
     * Returns a formatted SpannableString with bold and size formatting. I.e., "10 mi", "350 m"
     *
     * @param distance in meters
     * @return SpannableString representation which has a bolded number and units which have a
     * relative size of .65 times the size of the number
     */
    override fun formatDistance(distance: Double): SpannableString {
        val distanceAndSuffix = when (distance) {
            !in 0.0..Double.MAX_VALUE -> {
                formatDistanceAndSuffixForSmallUnit(0.0)
            }
            in 0.0..smallDistanceUpperThresholdInMeters -> {
                formatDistanceAndSuffixForSmallUnit(distance)
            }
            in smallDistanceUpperThresholdInMeters..mediumDistanceUpperThresholdInMeters -> {
                formatDistanceAndSuffixForLargeUnit(distance, 1)
            }
            else -> {
                formatDistanceAndSuffixForLargeUnit(distance, 0)
            }
        }
        return getSpannableDistanceString(distanceAndSuffix)
    }

    private fun formatDistanceAndSuffixForSmallUnit(distance: Double): Pair<String, String> {
        val distanceUnit = TurfConversion.convertLength(
            distance,
            TurfConstants.UNIT_METERS,
            smallUnit
        )
        val resources = applicationContext.resourcesWithLocale(locale)
        val unitStringSuffix = getUnitString(resources, smallUnit)
        val roundedNumber = distanceUnit.roundToInt() / roundingIncrement * roundingIncrement
        val roundedValue = (if (roundedNumber < roundingIncrement) roundingIncrement else roundedNumber).toString()
        return Pair(roundedValue, unitStringSuffix)
    }

    private fun formatDistanceAndSuffixForLargeUnit(distance: Double, maxFractionDigits: Int): Pair<String, String> {
        val resources = applicationContext.resourcesWithLocale(locale)
        val unitStringSuffix = getUnitString(resources, largeUnit)
        val distanceUnit =
            TurfConversion.convertLength(distance, TurfConstants.UNIT_METERS, largeUnit)
        val roundedValue = NumberFormat.getNumberInstance(locale).also {
            it.maximumFractionDigits = maxFractionDigits
        }.format(distanceUnit)
        return Pair(roundedValue, unitStringSuffix)
    }

    /**
     * Takes in a distance and units and returns a formatted SpannableString where the number is bold
     * and the unit is shrunked to .65 times the size
     *
     * @param distance formatted with appropriate decimal places
     * @param unit string from TurfConstants. This will be converted to the abbreviated form.
     * @return String with bolded distance and shrunken units
     */
    internal fun getSpannableDistanceString(distanceAndSuffix: Pair<String, String>): SpannableString {
        val spannableString = SpannableString("${distanceAndSuffix.first} ${distanceAndSuffix.second}")

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            distanceAndSuffix.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            RelativeSizeSpan(0.65f), distanceAndSuffix.first.length + 1,
            spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    private fun getUnitString(resources: Resources, @TurfConstants.TurfUnitCriteria unit: String) = when (unit) {
        TurfConstants.UNIT_KILOMETERS -> resources.getString(R.string.kilometers)
        TurfConstants.UNIT_METERS -> resources.getString(R.string.meters)
        TurfConstants.UNIT_MILES -> resources.getString(R.string.miles)
        TurfConstants.UNIT_FEET -> resources.getString(R.string.feet)
        else -> ""
    }

    private fun Context.resourcesWithLocale(locale: Locale?): Resources {
        val config = this.resources.configuration.also {
            it.setLocale(locale)
        }
        return this.createConfigurationContext(config).resources
    }
}
