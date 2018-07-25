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
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * This interface models a point on the globe represented by latitude/longitude in
 * decimal degrees.
 *
 * Default implementations of this class are immutable and therefore threadsafe.
 *
 * @param latitude the latitude value for this geographic point.
 * @param longitude the longitude value fo this geographic point.
 */
@Suppress("MagicNumber")
class DecimalDegreesCoordinate private constructor(val latitude: Double, val longitude: Double) {
    companion object {
        fun createDecimalDegreesCoordinate(latitude: Double, longitude: Double):
                DecimalDegreesCoordinate {
            return DecimalDegreesCoordinate(latitude, longitude)
        }
    }

    fun toUsng(
        translator: CoordinateSystemTranslator,
        coordinatePrecision: Precision = Precision.ONE_METER
    ): UsngCoordinate {
        val lat = latitude
        var lon = longitude
        val precision = coordinatePrecision.intValue + 1

        // make lon between -180 & 180
        if (lon < -180) {
            lon += 360.0
        } else if (lon > 180) {
            lon -= 360.0
        }

        // convert lat/lon to UTM coordinates
        val utmCoordinate = DecimalDegreesCoordinate(lat, lon).toUtm(translator)
        val utmEasting = utmCoordinate.easting
        var utmNorthing = utmCoordinate.northing
        // ...then convert UTM to USNG

        // southern hemisphere case
        if (lat < 0) {
            // Use offset for southern hemisphere
            utmNorthing += CoordinateSystemTranslator.NORTHING_OFFSET
        }

        val zoneNumber = translator.getZoneNumber(lat, lon)

        // UTM northing and easting is the analogue of USNG letters + USNG northing and easting so
        // remove the component of UTM northing and easting that corresponds with the USNG letters
        var usngNorthing = (round(utmNorthing) % CoordinateSystemTranslator.BLOCK_SIZE)
        var usngEasting = (round(utmEasting) % CoordinateSystemTranslator.BLOCK_SIZE)

        // truncate USNG string digits to achieve specified precision
        usngNorthing = floor(usngNorthing / 10.0.pow(5 - coordinatePrecision.intValue))
        usngEasting = floor(usngEasting / 10.0.pow(5 - coordinatePrecision.intValue))
        val utmLetterDesignator = translator.getUtmLetterDesignator(lat)[0]

        // begin building USNG string "DDL"

        // add 100k meter grid letters to USNG string "DDL LL"
        if (precision < 1) {
            return UsngCoordinate.createZoneLatitudeCoordinate(zoneNumber, utmLetterDesignator)
        }

        val usngLetters = translator.findGridLetters(zoneNumber, utmNorthing, utmEasting)
        val columnLetter = usngLetters[0]
        val rowLetter = usngLetters[1]
        // REVISIT: Modify to incorporate dynamic precision ?

        // if requested precision is higher than USNG northing or easting, pad front
        // with zeros

        // add easting and northing to USNG string "DDL LL D+ D+"
        return if (coordinatePrecision.intValue < 1) {
            UsngCoordinate.createGridCoordinate(zoneNumber,
                    utmLetterDesignator,
                    columnLetter,
                    rowLetter)
        } else {
            UsngCoordinate.createPreciseCoordinate(zoneNumber,
                    utmLetterDesignator,
                    columnLetter,
                    rowLetter,
                    usngEasting.toInt(),
                    usngNorthing.toInt())
        }
    }

    @Suppress("ComplexCondition")
    fun toUtm(translator: CoordinateSystemTranslator): UtmCoordinate {
        // note: input of lon = 180 or -180 with zone 60 not allowed use 179.9999

        // Constrain reporting USNG coords to the latitude range [80S .. 84N]
        /////////////////
        if (latitude > 84.0 || latitude < -80.0) {
            throw IllegalArgumentException("valid range for lat parameter is -80<lat>84. " +
                    "Supplied value: $latitude.")
        }
        //////////////////////

        // sanity check on input - turned off when testing with Generic Viewer
        if (longitude > 360 || longitude < -180 || latitude > 90 || latitude < -90) {
            throw IllegalArgumentException("Invalid input - lat: $latitude, lon: $longitude")
        }

        // Make sure the longitude is between -180.00 .. 179.99..
        // Convert values on 0-360 range to this range.
        val lonTemp = longitude + 180 - (((longitude + 180) / 360).toInt() * 360).toDouble() - 180.0
        val latRad = latitude * CoordinateSystemTranslator.DEG_2_RAD
        val lonRad = lonTemp * CoordinateSystemTranslator.DEG_2_RAD
        // user-supplied zone number will force coordinates to be computed in a particular zone
        val zoneNumber = translator.getZoneNumber(latitude, longitude)
        val lonOrigin = (zoneNumber - 1) * 6 - 180 + 3 // +3 puts origin in middle of zone
        val lonOriginRad = lonOrigin * CoordinateSystemTranslator.DEG_2_RAD

        val n = translator.equatorialRadius / sqrt(
                1 - translator.eccSquared * sin(latRad) * sin(latRad))
        val t = tan(latRad) * tan(latRad)
        val c = translator.eccPrimeSquared * cos(latRad) * cos(latRad)
        val a = cos(latRad) * (lonRad - lonOriginRad)

        // Note that the term Mo drops out of the "M" equation, because phi
        // (latitude crossing the central meridian, lambda0, at the origin of the
        //  x,y coordinates), is equal to zero for UTM.
        val m = translator.equatorialRadius * (
                (1 - translator.eccSquared / 4 - 3 * (translator.eccSquared.pow(2)) / 64 -
                        5 * (translator.eccSquared.pow(3)) / 256) * latRad -
                        (3 * translator.eccSquared / 8 + 3 * translator.eccSquared.pow(2) / 32 +
                        45 * translator.eccSquared.pow(3) / 1024) *
                        sin(2 * latRad) + (15 * translator.eccSquared.pow(2) / 256 +
                        45 * translator.eccSquared.pow(3) / 1024) *
                        sin(4 * latRad) -
                        (35 * translator.eccSquared.pow(3) / 3072) *
                        sin(6 * latRad))

        val utmEasting = (
                CoordinateSystemTranslator.K0 * n * (a + (1 - t + c) * (a * a * a) / 6 +
                        (5 - 18 * t + t * t + 72 * c - 58 * translator.eccPrimeSquared) *
                        (a * a * a * a * a) / 120) + CoordinateSystemTranslator.EASTING_OFFSET)

        val utmNorthing = (CoordinateSystemTranslator.K0 * (m + n * tan(latRad) * ((a * a) / 2 +
                (5 - t + 9 * c + 4 * c * c) * (a * a * a * a) / 24 +
                (61 - 58 * t + t * t + 600 * c - 330 * translator.eccPrimeSquared) *
                (a * a * a * a * a * a) / 720)))

        return UtmCoordinate.createUtmCoordinate(zoneNumber, utmEasting, utmNorthing)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DecimalDegreesCoordinate

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }
}
