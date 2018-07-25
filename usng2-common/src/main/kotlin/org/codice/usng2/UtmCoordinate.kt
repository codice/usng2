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

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * This interface models a point in the Universal Transverse Mercator coordinate system.
 * There are two valid formats for UTM coordinates.  Those are:
 *
 * `<zone number><latitude band letter><space><easting><space><northing>`
 * e.g. 10Q -204832 302043
 *
 * or
 *
 * `<zone number><space><easting><space><northing>`
 * e.g. 10 -204832 302043
 *
 * The default implementation of this class are immutable and therefore threadsafe.
 *
 * @param zoneNumber the zone number for this UTM coordinate.
 * @param easting the easting value for the UTM coordinate.
 * @param northing the northing value of the UTM coordinate.
 * @param latitudeBand the MGRS latitude band for this UTM coordinate.
 */
// TODO RAP 19 Jul 18: enums or typedefs for latitudeBand and nsIndicator
@Suppress("MagicNumber")
class UtmCoordinate private constructor(
    val zoneNumber: Int,
    val easting: Double,
    val northing: Double,
    val latitudeBand: Char? = null,
    val nsIndicator: Char? = null
) {
    val precision: Precision = Precision.forEastNorth(easting.toInt(), northing.toInt())

    fun toBoundingBox(translator: CoordinateSystemTranslator, accuracy: Int?):
            BoundingBoxCoordinate {
        /**************  convert UTM coords to decimal degrees *********************
         *
         * Equations from USGS Bulletin 1532 (or USGS Professional Paper 1395)
         * East Longitudes are positive, West longitudes are negative.
         * North latitudes are positive, South latitudes are negative.
         *
         * Expected Input args:
         * DecimalDegreesCoordinate   : northing-m (numeric), eg. 432001.8
         * southern hemisphere NEGATIVE from equator ('real' value - 10,000,000)
         * UTMEasting    : easting-m  (numeric), eg. 4000000.0
         * UTMZoneNumber : 6-deg longitudinal zone (numeric), eg. 18
         *
         * lat-lon coordinates are turned in the object 'ret' : ret.lat and ret.lon
         *
         */

        // remove 500,000 meter offset for longitude

        val southWest = toLatLon(translator)

        if (accuracy != null && accuracy <= 100000) {
            val tempUtmCoordinate = createUtmCoordinate(zoneNumber,
                    easting + accuracy,
                    northing + accuracy)
            val northEast = tempUtmCoordinate.toLatLon(translator)
            return BoundingBoxCoordinate.createBoundingBox(northEast.latitude,
                    southWest.latitude,
                    northEast.longitude,
                    southWest.longitude)
        } else {
            val zoneLetter = translator.getUtmLetterDesignator(southWest.latitude)
            val lats = translator.getZoneLetterLats(zoneLetter)
            val lons = translator.getZoneNumberLons(zoneNumber)

            if (lats != null) {
                return BoundingBoxCoordinate.createBoundingBox(lats[0], lats[1], lons[0], lons[1])
            }
        }

        throw ParseException("Error converting [${toString()}] to BoundingBoxCoordinate")
    }

    fun toLatLon(translator: CoordinateSystemTranslator): DecimalDegreesCoordinate {
        return if (nsIndicator == 'S') {
            toLatLon(withNoNSIndicator(), translator)
        } else {
            toLatLon(this, translator)
        }
    }

    internal fun withNSIndicator(): UtmCoordinate {
        return if (nsIndicator != null) {
            this
        } else {
            val (newNorthing, newNSIndicator) =
                    if (northing < 0) {
                        northing + CoordinateSystemTranslator.NORTHING_OFFSET to 'S'
                    } else {
                        northing to 'N'
                    }
            return UtmCoordinate(zoneNumber, easting, newNorthing, latitudeBand, newNSIndicator)
        }
    }

    internal fun withNoNSIndicator(): UtmCoordinate {
        return if (nsIndicator == null) {
            this
        } else {
            val (newNorthing, newNSIndicator) =
                    if (nsIndicator == 'S') {
                        northing - CoordinateSystemTranslator.NORTHING_OFFSET to null
                    } else {
                        northing to null
                    }
            return UtmCoordinate(zoneNumber, easting, newNorthing, latitudeBand, newNSIndicator)
        }
    }

    /**
     *
     * @return a String representation of this UTM coordinate. The returned String is parseable by
     * 'parseUtmString'.
     * Calling `coordinate.equals(UtmCoordinate.parseUtmString(coordinate.toString())`
     * will return true.
     */
    override fun toString(): String {
        return "${zoneNumber}${latitudeBand
                ?: ""} ${precision.format(easting.toInt())} ${precision.format(northing.toInt())}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UtmCoordinate

        if (zoneNumber != other.zoneNumber) return false
        if (easting != other.easting) return false
        if (northing != other.northing) return false
        if (latitudeBand != other.latitudeBand) return false
        if (nsIndicator != other.nsIndicator) return false
        if (precision != other.precision) return false

        return true
    }

    override fun hashCode(): Int {
        var result = zoneNumber
        result = 31 * result + easting.hashCode()
        result = 31 * result + northing.hashCode()
        result = 31 * result + (latitudeBand?.hashCode() ?: 0)
        result = 31 * result + (nsIndicator?.hashCode() ?: 0)
        result = 31 * result + precision.hashCode()
        return result
    }

    companion object {
        fun createUtmCoordinate(zoneNumber: Int, easting: Double, northing: Double): UtmCoordinate {
            return UtmCoordinate(zoneNumber, easting, northing)
        }

        fun createUtmCoordinateWithNS(
            zoneNumber: Int,
            easting: Double,
            northing: Double,
            nsIndicator: Char?
        ): UtmCoordinate {
            return UtmCoordinate(zoneNumber, easting, northing, null, nsIndicator)
        }

        fun createUtmCoordinateWithLatBand(
            zoneNumber: Int,
            easting: Double,
            northing: Double,
            latitudeBand: Char?
        ): UtmCoordinate {
            return UtmCoordinate(zoneNumber, easting, northing, latitudeBand)
        }

        @Suppress("LongParameterList")
        fun createUtmCoordinateWithLatBandWithNS(
            zoneNumber: Int,
            easting: Double,
            northing: Double,
            latitudeBand: Char?,
            nsIndicator: Char?
        ): UtmCoordinate {
            return UtmCoordinate(zoneNumber, easting, northing, latitudeBand, nsIndicator)
        }

        /**
         *
         * @param utmString a UTM formatted string. e.g. `10Q 123456 -0123456`
         * @return an object representation of 'utmString'
         * @throws ParseException when 'utmString' isn't correctly formatted.
         */
        fun parseUtmString(utmString: String): UtmCoordinate {
            val utmRegexp = """(\d\d?)(-?[CDEFGHJKLMNPQRSTUVWX]?)(\W?-?\d{6})(\W?-?\d{7})"""
                    .toRegex()

            if (!utmRegexp.matches(utmString)) {
                throw ParseException("Supplied argument '$utmString' is not a valid " +
                        "UTM formatted String.")
            }

            val matchResult = utmRegexp.find(utmString)!!

            val zoneNumber = matchResult.groupValues[1].toInt()
            val latitudeBandString = matchResult.groupValues[2]
            val easting = matchResult.groupValues[3].trim { it <= ' ' }
            val northing = matchResult.groupValues[4].trim { it <= ' ' }

            return if (latitudeBandString.isNotEmpty()) {
                UtmCoordinate(zoneNumber,
                        easting.toDouble(),
                        northing.toDouble(),
                        latitudeBandString[0])
            } else {
                UtmCoordinate(zoneNumber, easting.toDouble(), northing.toDouble())
            }
        }

        private fun toLatLon(utmCoordinate: UtmCoordinate, translator: CoordinateSystemTranslator):
                DecimalDegreesCoordinate {
            val xUTM = utmCoordinate.easting - CoordinateSystemTranslator.EASTING_OFFSET
            val yUTM = utmCoordinate.northing

            // origin longitude for the zone (+3 puts origin in zone center)
            val lonOrigin = (utmCoordinate.zoneNumber - 1) * 6 - 180 + 3
            // M is the "true distance along the central meridian from the Equator to phi
            // (latitude)
            val M = yUTM / CoordinateSystemTranslator.K0
            val mu = M / (translator.equatorialRadius * (1.0 - translator.eccSquared / 4.0 -
                    3.0 * translator.eccSquared * translator.eccSquared / 64.0 -
                    5.0 * translator.eccSquared.pow(3) / 256.0))
            // phi1 is the "footprint latitude" or the latitude at the central meridian which
            // has the same y coordinate as that of the point (phi (lat), lambda (lon) ).
            val phi1Rad = (mu + (3 * translator.e1 / 2 - 27.0 * translator.e1.pow(3) / 32) *
                    sin(2 * mu) +
                    (21.0 * translator.e1.pow(2) / 16 - 55.0 * translator.e1.pow(4) / 32) *
                    sin(4 * mu) + 151.0 * translator.e1.pow(3) / 96 * sin(6 * mu))
            phi1Rad * CoordinateSystemTranslator.RAD_2_DEG

            // Terms used in the conversion equations
            val N1 = translator.equatorialRadius / sqrt(
                    1 - translator.eccSquared * sin(phi1Rad) * sin(phi1Rad))
            val T1 = tan(phi1Rad) * tan(phi1Rad)
            val C1 = translator.eccPrimeSquared * cos(phi1Rad) * cos(phi1Rad)
            val R1 = translator.equatorialRadius * (1 - translator.eccSquared) /
                    (1 - translator.eccSquared * sin(phi1Rad) * sin(phi1Rad)).pow(1.5)
            val D = xUTM / (N1 * CoordinateSystemTranslator.K0)
            // Calculate latitude, in decimal degrees
            var lat = phi1Rad - N1 * tan(phi1Rad) / R1 *
                    (D * D / 2 - (5.0 + 3 * T1 + 10 * C1 - 4.0 * C1 * C1 - 9 *
                            translator.eccPrimeSquared) * D * D * D * D /
                            24 + ((61.0 + 90 * T1 + 298 * C1 + 45.0 * T1 * T1 - 252 *
                            translator.eccPrimeSquared - 3.0 * C1 * C1) *
                    D * D * D * D * D * D) / 720.0)
            lat *= CoordinateSystemTranslator.RAD_2_DEG

            if (lat == 0.0) {
                lat = 0.001
            }

            // Calculate longitude, in decimal degrees
            var lon = (D - (1.0 + 2 * T1 + C1) * D * D * D / 6 +
                    ((5 - 2 * C1 + 28 * T1 - 3.0 * C1 * C1 + 8 *
                            translator.eccPrimeSquared + 24.0 * T1 * T1) *
                            D * D * D * D * D) / 120) / cos(phi1Rad)

            lon = lonOrigin + lon * CoordinateSystemTranslator.RAD_2_DEG
            return DecimalDegreesCoordinate.createDecimalDegreesCoordinate(lat, lon)
        }
    }
}
