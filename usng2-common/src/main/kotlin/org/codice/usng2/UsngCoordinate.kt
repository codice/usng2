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

import kotlin.math.pow

/**
 * This interface models a point in the United States National Grid coordinate system.
 * There are several valid formats for USNG coordinates.  A fully specified coordinate is formatted
 * like this:
 *
 * `<zone number><latitude band letter><space><grid column><grid row>
 *     <space><easting><space><northing>`
 * e.g. 18T WL 85628 11322
 *
 * Only `<zone number>` and `<latitude band letter>` are required. The grid letters are
 * required if easting and northing values are supplied. The easting and northing values have a
 * maximum length of 5 characters each (with an optional '-').
 *
 * Default implementations of this class are immutable and therefore threadsafe.
 *
 * @param zoneNumber the zone number of this USNG coordinate.
 * @param latitudeBandLetter the latitude band letter of this USNG coordinate.
 * @param columnLetter the grid column letter of this USNG coordinate.
 * @param rowLetter the grid row letter of this USNG coordinate.
 * @param easting - the easting value of this USNG coordinate.
 * @param northing - the northing value of this USNG coordinate.
 */
@Suppress("MagicNumber")
class UsngCoordinate private constructor(
    val zoneNumber: Int,
    val latitudeBandLetter: Char,
    val columnLetter: Char? = null,
    val rowLetter: Char? = null,
    val easting: Int? = null,
    val northing: Int? = null
) {
    var precision: Precision = Precision.SIX_BY_EIGHT_DEGREES

    companion object {
        private val USNG_REGEXP = """(\d\d?)([CDEFGHJKLMNPQRSTUVWX])
            |\W?([ABCDEFGHJKLMNPQRSTUVWXYZ][ABCDEFGHJKLMNPQRSTUV])?
            |(\W\d{0,5})?(\W\d{0,5})?""".trimMargin().replace("\\s".toRegex(), "").toRegex()

        private val MGRS_REGEXP = """(\d\d?)([CDEFGHJKLMNPQRSTUVWX])
            |\W?([ABCDEFGHJKLMNPQRSTUVWXYZ][ABCDEFGHJKLMNPQRSTUV])?
            |(\d{0,5})\W*(\d{0,5})\W*""".trimMargin().replace("\\s".toRegex(), "").toRegex()

        fun createZoneLatitudeCoordinate(
            zoneNumber: Int,
            latitudeBandLetter: Char
        ): UsngCoordinate {
            return UsngCoordinate(zoneNumber, latitudeBandLetter).apply {
                precision = Precision.SIX_BY_EIGHT_DEGREES
            }
        }

        fun createGridCoordinate(
            zoneNumber: Int,
            latitudeBandLetter: Char,
            columnLetter: Char,
            rowLetter: Char
        ): UsngCoordinate {
            return UsngCoordinate(zoneNumber, latitudeBandLetter, columnLetter, rowLetter).apply {
                precision = Precision.ONE_HUNDRED_KILOMETERS
            }
        }

        @Suppress("LongParameterList")
        fun createPreciseCoordinate(
            zoneNumber: Int,
            latitudeBandLetter: Char,
            columnLetter: Char,
            rowLetter: Char,
            easting: Int,
            northing: Int
        ): UsngCoordinate {
            return UsngCoordinate(zoneNumber, latitudeBandLetter, columnLetter, rowLetter, easting,
                    northing).apply {
                precision = Precision.forEastNorth(easting, northing)
            }
        }

        /**
         * @param usngStr a properly formatted USNG string.
         * @return a fully parsed UsngCoordinate object.
         * @throws ParseException when 'usngStr' isn't in USNG format.
         */
        internal fun parseUsngString(usngStr: String): UsngCoordinate {
            return parseCoordinateString(usngStr, USNG_REGEXP)
        }

        /**
         * @param mgrsStr a properly formatted MGRS string.
         * @return a fully parsed UsngCoordinate object.
         * @throws ParseException when 'msgrsStr' isn't in MGRS format.
         */
        internal fun parseMgrsString(mgrsStr: String): UsngCoordinate {
            return parseCoordinateString(mgrsStr, MGRS_REGEXP)
        }

        private fun parseCoordinateString(coordinateString: String, pattern: Regex):
                UsngCoordinate {
            if (!pattern.matches(coordinateString.toUpperCase())) {
                throw ParseException("Supplied argument '$coordinateString' " +
                        "is not a valid USNG formatted String.")
            }

            val matchResult = pattern.find(coordinateString.toUpperCase())!!

            val zoneNumber = matchResult.groupValues[1].toInt()
            val latitudeBandLetter = matchResult.groupValues[2].first()

            return if (matchResult.groupValues[3].isNotEmpty()) {
                val columnLetter = matchResult.groupValues[3].get(0)
                val rowLetter = matchResult.groupValues[3].get(1)

                if (matchResult.groupValues[5].isNotEmpty()) {
                    val easting = matchResult.groupValues[4].trim { it <= ' ' }.toInt()
                    val northing = matchResult.groupValues[5].trim { it <= ' ' }.toInt()

                    createPreciseCoordinate(zoneNumber,
                            latitudeBandLetter,
                            columnLetter,
                            rowLetter,
                            easting,
                            northing)
                } else {
                    createGridCoordinate(zoneNumber,
                            latitudeBandLetter,
                            columnLetter,
                            rowLetter)
                }
            } else {
                createZoneLatitudeCoordinate(zoneNumber, latitudeBandLetter)
            }
        }
    }

    fun toUtm(): UtmCoordinate {
        val zone = zoneNumber
        val letter = latitudeBandLetter

        val sq1 = columnLetter ?: 0.toChar()
        val sq2 = rowLetter ?: 0.toChar()

        val east = easting
        val north = northing

        // easting goes from 100,000 - 800,000 and repeats across zones
        // A,J,S correspond with 100,000, B,K,T correspond with 200,000 etc
        val eastingArray = arrayOf("", "AJS", "BKT", "CLU", "DMV", "ENW", "FPX", "GQY", "HRZ")

        // zoneBase - southern edge of N-S zones of millions of meters
        val zoneBase = doubleArrayOf(1.1, 2.0, 2.8, 3.7, 4.6, 5.5, 6.4, 7.3, 8.2, 9.1, 0.0, 0.8,
                1.7, 2.6, 3.5, 4.4, 5.3, 6.2, 7.0, 7.9)

        // multiply zone bases by 1 million to get the proper length for each
        for (i in zoneBase.indices) {
            zoneBase[i] = zoneBase[i] * 1000000
        }

        // northing goes from 0 - 1,900,000. A corresponds with 0, B corresponds with 200,000,
        // V corresponds with 1,900,000
        val northingArrayOdd = "ABCDEFGHJKLMNPQRSTUV"

        // even numbered zones have the northing letters offset from the odd northing.
        // So, F corresponds with 0, G corresponds with 100,000 and E corresponds with 1,900,000
        val northingArrayEven = "FGHJKLMNPQRSTUVABCDE"

        var easting = -1.0

        // the index of the string the letter is in will be the base easting, as explained in the
        // declaration of eastingArray
        val indexOfEastingZone = eastingArray.indexOfFirst { it.contains(sq1) }
        if (indexOfEastingZone > -1) {
            // multiply by 100,000 to get the proper base easting
            easting = (indexOfEastingZone * 100000).toDouble()

            // add the east parameter to get the total easting
            easting += east!! * 10.0.pow(5 - precision.intValue)
        }

        var northing: Double

        if (sq2 != 0.toChar()) {
            // if zone number is even, use northingArrayEven, if odd, use northingArrayOdd
            // similar to finding easting, the index of sq2 corresponds with the base easting
            northing =
                    if (zone % 2 == 0) {
                        (northingArrayEven.indexOf(sq2) * 100000).toDouble()
                    } else {
                        (northingArrayOdd.indexOf(sq2) * 100000).toDouble()
                    }

            // we can exploit the repeating behavior of northing to find what the total northing
            // should be iterate through the horizontal zone bands until our northing is greater
            // than the zoneBase of our zone
            while (northing < zoneBase["CDEFGHJKLMNPQRSTUVWX".indexOf(letter)]) {
                northing += 2000000
            }

            // add the north parameter to get the total northing
            northing += north!! * 10.0.pow(5 - precision.intValue)
        } else {
            // add approximately half of the height of one large region to ensure we're in the
            // right zone
            northing = zoneBase["CDEFGHJKLMNPQRSTUVWX".indexOf(letter)] + 499600
        }

        // set return object
        return UtmCoordinate.createUtmCoordinateWithLatBand(zone, easting, northing, letter)
    }

    fun toLatLon(translator: CoordinateSystemTranslator): DecimalDegreesCoordinate {
        // convert USNG coords to UTM; this routine counts digits and sets precision

        val coords = createPreciseCoordinate(zoneNumber,
                latitudeBandLetter,
                columnLetter ?: 0.toChar(),
                rowLetter ?: 0.toChar(),
                easting ?: 0,
                northing ?: 0).toUtm()

        var northing = coords.northing

        // southern hemisphere case
        if (latitudeBandLetter < 'N') {
            northing -= CoordinateSystemTranslator.NORTHING_OFFSET
        }

        return UtmCoordinate.createUtmCoordinate(zoneNumber, coords.easting, northing)
                .toLatLon(translator)
    }

    fun toBoundingBox(translator: CoordinateSystemTranslator): BoundingBoxCoordinate {
        // convert USNG coords to UTM; this routine counts digits and sets precision
        val coords = createPreciseCoordinate(zoneNumber,
                latitudeBandLetter,
                columnLetter ?: 0.toChar(),
                rowLetter ?: 0.toChar(),
                easting ?: 0,
                northing ?: 0).toUtm()

        var northing = coords.northing

        // southern hemisphere case
        if (latitudeBandLetter < 'N') {
            northing -= CoordinateSystemTranslator.NORTHING_OFFSET
        }

        val accuracy = (100000 / 10.0.pow(precision.intValue)).toInt()

        return UtmCoordinate.createUtmCoordinate(zoneNumber, coords.easting, northing)
                .toBoundingBox(translator, accuracy)
    }

    /**
     *
     * @return a String representation of this USNG coordinate. The returned String is parseable by
     * 'parseUsngString' and calling
     * `coordinate.equals(UsngCoordinate.parseUsngString(coordinate.toString())` will return true.
     */
    override fun toString(): String {
        return this.toString(true)
    }

    /**
     *
     * {@inheritDoc}
     */
    fun toMgrsString(): String {
        return this.toString(false)
    }

    private fun toString(includeSpaces: Boolean): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(zoneNumber)
                .append(latitudeBandLetter)

        if (columnLetter != null && rowLetter != null) {
            if (includeSpaces) {
                stringBuilder.append(" ")
            }

            stringBuilder.append(columnLetter)
                    .append(rowLetter)

            if (easting != null && northing != null) {
                if (includeSpaces) {
                    stringBuilder.append(" ")
                }

                stringBuilder.append(precision.format(easting))

                if (includeSpaces) {
                    stringBuilder.append(" ")
                }

                stringBuilder.append(precision.format(northing))
            }
        }

        return stringBuilder.toString()
    }

    @Suppress("ComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UsngCoordinate

        if (zoneNumber != other.zoneNumber) return false
        if (latitudeBandLetter != other.latitudeBandLetter) return false
        if (columnLetter != other.columnLetter) return false
        if (rowLetter != other.rowLetter) return false
        if (easting != other.easting) return false
        if (northing != other.northing) return false
        if (precision != other.precision) return false

        return true
    }

    override fun hashCode(): Int {
        var result = zoneNumber
        result = 31 * result + latitudeBandLetter.hashCode()
        result = 31 * result + (columnLetter?.hashCode() ?: 0)
        result = 31 * result + (rowLetter?.hashCode() ?: 0)
        result = 31 * result + (easting ?: 0)
        result = 31 * result + (northing ?: 0)
        result = 31 * result + precision.hashCode()
        return result
    }
}
