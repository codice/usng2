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

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * This interface models an area on the globe represented by a north/south/east/west
 * bounding box.
 *
 * Default implementations of this class are immutable and therefore threadsafe.
 *
 * @param north the northern line of latitude for this bounding box.
 * @param south ths southern line of latitude for this bounding box.
 * @param east the eastern line of longitude for this bounding box.
 * @param west the western line of longitude for this bounding box.
 */
@Suppress("MagicNumber")
class BoundingBoxCoordinate private constructor(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double
) {
    companion object {
        fun createBoundingBox(north: Double, south: Double, east: Double, west: Double):
                BoundingBoxCoordinate {
            return BoundingBoxCoordinate(north, south, east, west)
        }
    }

    @Suppress("ComplexCondition", "ComplexMethod")
    fun toUsng(translator: CoordinateSystemTranslator): UsngCoordinate {
        // calculate midpoints for use in USNG string calculation
        var lat = (north + south) / 2
        var lon = (east + west) / 2

        // round down edge cases
        when {
            lon >= 180 -> lon = 179.9
            lon <= -180 -> lon = -179.9
        }
        when {
            lat >= 90 -> lat = 89.9
            lat <= -90 -> lat = -89.9
        }

        // calculate distance between two points (North, West) and (South, East)
        val r = 6371000 // metres
        val phi1 = north * CoordinateSystemTranslator.DEG_2_RAD
        val phi2 = south * CoordinateSystemTranslator.DEG_2_RAD
        val deltaPhi = (south - north) * CoordinateSystemTranslator.DEG_2_RAD
        val deltaLlamda = (west - east) * CoordinateSystemTranslator.DEG_2_RAD

        // trigonometry calculate distance

        var height = sin(deltaPhi / 2) * sin(deltaPhi / 2)
        height = r.toDouble() * 2.0 * atan2(sqrt(height), sqrt(1 - height))
        var length = cos(phi1) * cos(phi2) * sin(deltaLlamda / 2) * sin(
                deltaLlamda / 2)
        length = r.toDouble() * 2.0 * atan2(sqrt(length), sqrt(1 - length))

        val dist = max(height, length)
        // divide distance by square root of two

        if (lon == 0.0 && (east > 90 || east < -90) && (west > 90 || west < -90)) {
            lon = 180.0
        }
        // calculate a USNG string with a precision based on distance
        // precision is defined in toUsng declaration
        var precision = Precision.ONE_METER

        when {
            dist > 100000 -> precision = Precision.SIX_BY_EIGHT_DEGREES
            dist > 10000 -> precision = Precision.ONE_HUNDRED_KILOMETERS
            dist > 1000 -> precision = Precision.TEN_KILOMETERS
            dist > 100 -> precision = Precision.ONE_KILOMETER
            dist > 10 -> precision = Precision.ONE_HUNDRED_METERS
            dist > 1 -> precision = Precision.TEN_METERS
        }

        // result is a USNG string of the form DDL LL DDDDD DDDDD
        // length of string will be based on the precision variable

        // result is a USNG string of the form DDL LL DDDDD DDDDD
        // length of string will be based on the precision variable
        return DecimalDegreesCoordinate.createDecimalDegreesCoordinate(lat, lon)
                .toUsng(translator, precision)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BoundingBoxCoordinate

        if (north != other.north) return false
        if (south != other.south) return false
        if (east != other.east) return false
        if (west != other.west) return false

        return true
    }

    override fun hashCode(): Int {
        var result = north.hashCode()
        result = 31 * result + south.hashCode()
        result = 31 * result + east.hashCode()
        result = 31 * result + west.hashCode()
        return result
    }
}
