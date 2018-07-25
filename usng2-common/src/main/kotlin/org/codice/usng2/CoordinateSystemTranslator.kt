/*
Copyright (c) 2009 Larry Moore, larmoor@gmail.com
              2014 Mike Adair, Richard Greenwood, Didier Richard, Stephen Irons, Olivier Terral and
                   Calvin Metcalf (proj4js)
              2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
package org.codice.usng2

import kotlin.math.PI
import kotlin.math.round
import kotlin.math.sqrt

/**
 * A utility for converting between coordinate systems.
 *
 * The default implementation of this class is immutable and therefore threadsafe.
 */
@Suppress("MagicNumber", "TooManyFunctions")
class CoordinateSystemTranslator(isNad83Datum: Boolean = true) {
    internal val equatorialRadius: Double

    internal val eccPrimeSquared: Double

    internal val eccSquared: Double

    internal val e1: Double

    init {
        // check for NAD83
        if (isNad83Datum) {
            equatorialRadius = 6378137.0 // GRS80 ellipsoid (meters)
            eccSquared = 0.006694380023
        } else {
            equatorialRadius = 6378206.4 // Clarke 1866 ellipsoid (meters)
            eccSquared = 0.006768658
        } // else NAD27 datum is assumed

        eccPrimeSquared = eccSquared / (1 - eccSquared)

        e1 = (1 - sqrt(x = 1 - eccSquared)) / (1 + sqrt(1 - eccSquared))
    }

    companion object {
        const val DEG_2_RAD = PI / 180

        const val RAD_2_DEG = 180.0 / PI

        const val BLOCK_SIZE = 100000

        // For diagram of zone sets; please see the "United States National Grid" white paper.
        const val GRIDSQUARE_SET_COL_SIZE = 8 // column width of grid square set

        const val GRIDSQUARE_SET_ROW_SIZE = 20 // row height of grid square set

        // UTM offsets
        const val EASTING_OFFSET = 500000.0 // (meters)

        const val NORTHING_OFFSET = 10000000.0 // (meters)

        // scale factor of central meridian
        const val K0 = 0.9996

        const val USNG_SQ_LET_ODD = "ABCDEFGHJKLMNPQRSTUV"

        const val USNG_SQ_LET_EVEN = "FGHJKLMNPQRSTUVABCDE"

        private val ZONE_LETTER_LATS_MAP = mapOf(
                "C" to doubleArrayOf(-72.0, -80.0),
                "D" to doubleArrayOf(-64.0, -72.0),
                "E" to doubleArrayOf(-56.0, -64.0),
                "F" to doubleArrayOf(-48.0, -56.0),
                "G" to doubleArrayOf(-40.0, -48.0),
                "H" to doubleArrayOf(-32.0, -40.0),
                "J" to doubleArrayOf(-24.0, -32.0),
                "K" to doubleArrayOf(-16.0, -24.0),
                "L" to doubleArrayOf(-8.0, -16.0),
                "M" to doubleArrayOf(-0.01, -8.0),
                "N" to doubleArrayOf(8.0, 0.01),
                "P" to doubleArrayOf(16.0, 8.0),
                "Q" to doubleArrayOf(24.0, 16.0),
                "R" to doubleArrayOf(32.0, 24.0),
                "S" to doubleArrayOf(40.0, 32.0),
                "T" to doubleArrayOf(48.0, 40.0),
                "U" to doubleArrayOf(56.0, 48.0),
                "V" to doubleArrayOf(64.0, 56.0),
                "W" to doubleArrayOf(72.0, 64.0),
                "X" to doubleArrayOf(84.0, 72.0)
        )
    }

    /**
     *
     * @param latLonCoordinate the bounding box to be converted.
     * @return a UsngCoordinate that represents the supplied DecimalDegreesCoordinate.
     */
    fun toUsng(latLonCoordinate: BoundingBoxCoordinate): UsngCoordinate {
        return latLonCoordinate.toUsng(this)
    }

    /**
     * Converts from decimal degrees to UTM.
     *
     * @param decimalDegreesCoordinate the Lat/Lon coordinate to be converted.
     * @return the UTM equivalent of decimalDegreesCoordinate.
     */
    fun toUtm(decimalDegreesCoordinate: DecimalDegreesCoordinate): UtmCoordinate {
        return decimalDegreesCoordinate.toUtm(this)
    }

    /**
     * Converts from decimal degrees to UTM with N/S indicator.
     *
     * @param decimalDegreesCoordinate the Lat/Lon coordinate to be converted.
     * @return the UTM equivalent of decimalDegreesCoordinate. This UTM coordinate will use the N/S
     * indicator and its northing value will be adjusted by
     * {@link CoordinateSystemTranslator#NORTHING_OFFSET}
     */
    fun toUtmWithNS(decimalDegreesCoordinate: DecimalDegreesCoordinate): UtmCoordinate {
        return decimalDegreesCoordinate.toUtm(this).withNSIndicator()
    }

    /**
     * Converts from decimal degrees to USNG.
     *
     * @param decimalDegreesCoordinate the lat/lon coordinate to be converted.
     * @return a USNG equivalent of decimalDegreesCoordinate.
     */
    fun toUsng(decimalDegreesCoordinate: DecimalDegreesCoordinate): UsngCoordinate {
        return decimalDegreesCoordinate.toUsng(this)
    }

    /**
     * Converts from decimal degrees to USNG.
     *
     * @param decimalDegreesCoordinate the lat/lon coordinate to be converted.
     * @param coordinatePrecision the requested precision of the returned UsngCoordinate.
     * @return a USNG equivalent of decimalDegreesCoordinate.
     */
    fun toUsng(
        decimalDegreesCoordinate: DecimalDegreesCoordinate,
        coordinatePrecision: Precision
    ): UsngCoordinate {
        return decimalDegreesCoordinate.toUsng(this, coordinatePrecision)
    }

    /**
     * Converts from UTM to a bounding box.
     *
     * @param utmCoordinate the UTM coordinate to be converted.
     * @return the lat/lon equiavlent of utmCoordinate.
     */
    fun toBoundingBox(utmCoordinate: UtmCoordinate, accuracy: Int? = null): BoundingBoxCoordinate {
        return utmCoordinate.toBoundingBox(this, accuracy)
    }

    /**
     * Converts from UTM to decimal degrees.
     *
     * @param utmCoordinate
     * @return
     */
    fun toLatLon(utmCoordinate: UtmCoordinate): DecimalDegreesCoordinate {
        return utmCoordinate.toLatLon(this)
    }

    /**
     * Converts from USNG to UTM.
     * @param usngCoordinate the USNG coordinate to be converted.
     * @return the UTM equivalent of usngCoordinate.
     */
    fun toUtm(usngCoordinate: UsngCoordinate): UtmCoordinate {
        return usngCoordinate.toUtm()
    }

    /**
     * Convert from USNG to lat/lon.
     * @param usngCoordinate the USNG coordinate to be converted.
     * @return the lat/lon equivalent of usngp.
     */
    fun toLatLon(usngCoordinate: UsngCoordinate): DecimalDegreesCoordinate {
        return usngCoordinate.toLatLon(this)
    }

    /**
     * Convert from USNG to lat/lon.
     * @param usngCoordinate the USNG coordinate to be converted.
     * @return the lat/lon equivalent of usngp.
     */
    fun toBoundingBox(usngCoordinate: UsngCoordinate): BoundingBoxCoordinate {
        return usngCoordinate.toBoundingBox(this)
    }

    /**
     *
     * @param utmString a UTM formatted string. e.g. `10Q 123456 -0123456`
     * @return an object representation of 'utmString'
     * @throws ParseException when 'utmString' isn't correctly formatted.
     */
    fun parseUtmString(utmString: String): UtmCoordinate {
        return UtmCoordinate.parseUtmString(utmString)
    }

    /**
     * @param usngString a properly formatted USNG string.
     * @return a fully parsed UsngCoordinate object.
     * @throws ParseException when 'usngStr' isn't in USNG format.
     */
    fun parseUsngString(usngString: String): UsngCoordinate {
        return UsngCoordinate.parseUsngString(usngString)
    }

    /**
     * @param mgrsString a properly formatted MGRS string.
     * @return a fully parsed UsngCoordinate object.
     * @throws ParseException when 'msgrsString' isn't in MGRS format.
     */
    fun parseMgrsString(mgrsString: String): UsngCoordinate {
        return UsngCoordinate.parseMgrsString(mgrsString)
    }

    @Suppress("ComplexCondition")
    fun getZoneNumber(lat: Double, lon: Double): Int {
        // sanity check on input
        if (lon > 360 || lon < -180 || lat > 84 || lat < -80) {
            throw IllegalArgumentException("Invalid input - lat: $lat, lon: $lon")
        }

        // convert 0-360 to [-180 to 180] range
        val lonTemp = lon + 180 - (((lon + 180) / 360).toInt() * 360).toDouble() - 180.0
        var zoneNumber = (lonTemp + 180).toInt() / 6 + 1

        // Handle special case of west coast of Norway
        if (lat >= 56.0 && lat < 64.0 && lonTemp >= 3.0 && lonTemp < 12.0) {
            zoneNumber = 32
        }

        // Special zones for Svalbard
        if (lat >= 72.0 && lat < 84.0) {
            when {
                lonTemp >= 0.0 && lonTemp < 9.0 -> zoneNumber = 31
                lonTemp >= 9.0 && lonTemp < 21.0 -> zoneNumber = 33
                lonTemp >= 21.0 && lonTemp < 33.0 -> zoneNumber = 35
                lonTemp >= 33.0 && lonTemp < 42.0 -> zoneNumber = 37
            }
        }

        return zoneNumber
    }

    fun getUtmLetterDesignator(lat: Double): String {
        return if (lat > 84 || lat < -80) {
            "Z"
        } else {
            var index = (lat + 80) / 8

            if (index >= 6) index++ // skip 'I'
            if (index >= 12) index++ // skip 'O'
            if (index >= 22) index-- // adjust for 80 to 84, which should be 'X'

            (67 + index).toChar().toString() // 'C'
        }
    }

    fun findSet(zoneNum: Int): Int {
        val z = zoneNum % 6

        if (z < 0) {
            return -1
        }

        return if (z == 0) {
            6
        } else z
    }

    fun findGridLetters(zoneNum: Int, northing: Double, easting: Double): String {
        var row = 1

        // northing coordinate to single-meter precision
        var north1m = round(northing)

        // Get the row position for the square identifier that contains the point
        while (north1m >= BLOCK_SIZE) {
            north1m -= BLOCK_SIZE
            row++
        }

        // cycle repeats (wraps) after 20 rows
        row %= GRIDSQUARE_SET_ROW_SIZE
        var col = 0

        // easting coordinate to single-meter precision
        var east1m = round(easting)

        // Get the column position for the square identifier that contains the point
        while (east1m >= BLOCK_SIZE) {
            east1m -= BLOCK_SIZE
            col++
        }

        // cycle repeats (wraps) after 8 columns
        col %= GRIDSQUARE_SET_COL_SIZE

        val set = findSet(zoneNum)
        return this.lettersHelper(set, row, col)
    }

    fun lettersHelper(setter: Int, row: Int, col: Int): String {
        var row = row
        var col = col

        // handle case of last row
        if (row == 0) {
            row = GRIDSQUARE_SET_ROW_SIZE - 1
        } else {
            row--
        }

        // handle case of last column
        if (col == 0) {
            col = GRIDSQUARE_SET_COL_SIZE - 1
        } else {
            col--
        }

        val (l1: String?, l2: String?) = setterHelper(setter)

        return l1!!.substring(col, col + 1) + l2!!.substring(row, row + 1)
    }

    fun getZoneLetterLats(letter: String): DoubleArray? {
        return ZONE_LETTER_LATS_MAP[letter]
    }

    fun getZoneNumberLons(zone: Int): DoubleArray {
        val east = -180.0 + 6 * zone
        val west = east - 6

        return doubleArrayOf(east, west)
    }

    private fun setterHelper(setter: Int): Pair<String?, String?> {
        var l1: String? = null
        var l2: String? = null

        when (setter) {
            1 -> {
                l1 = "ABCDEFGH" // column ids
                l2 = USNG_SQ_LET_ODD // row ids
            }

            2 -> {
                l1 = "JKLMNPQR"
                l2 = USNG_SQ_LET_EVEN
            }

            3 -> {
                l1 = "STUVWXYZ"
                l2 = USNG_SQ_LET_ODD
            }

            4 -> {
                l1 = "ABCDEFGH"
                l2 = USNG_SQ_LET_EVEN
            }

            5 -> {
                l1 = "JKLMNPQR"
                l2 = USNG_SQ_LET_ODD
            }

            6 -> {
                l1 = "STUVWXYZ"
                l2 = USNG_SQ_LET_EVEN
            }
        }
        return l1 to l2
    }
}
