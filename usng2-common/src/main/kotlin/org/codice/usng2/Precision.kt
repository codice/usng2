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

import kotlin.math.max

/**
 * This enum represents the valid precision ranges that can be specified in the Unites States Grid
 * System.
 */
@Suppress("MagicNumber")
enum class Precision(val intValue: Int) {
    SIX_BY_EIGHT_DEGREES(-1),
    ONE_HUNDRED_KILOMETERS(0),
    TEN_KILOMETERS(1),
    ONE_KILOMETER(2),
    ONE_HUNDRED_METERS(3),
    TEN_METERS(4),
    ONE_METER(5);

    fun format(value: Int): String {
        return if (intValue <= 0) {
            ""
        } else {
            "$value".padStart(intValue, '0')
        }
    }

    companion object {
        fun fromInt(value: Int): Precision {
            return if (value <= -1) {
                SIX_BY_EIGHT_DEGREES
            } else {
                when (value) {
                    0 -> ONE_HUNDRED_KILOMETERS
                    1 -> TEN_KILOMETERS
                    2 -> ONE_KILOMETER
                    3 -> ONE_HUNDRED_METERS
                    4 -> TEN_METERS
                    else -> ONE_METER
                }
            }
        }

        fun forEastNorth(easting: Int, northing: Int): Precision {
            val maxValue = max(easting, northing)

            return when {
                maxValue > 9999 -> ONE_METER
                maxValue > 999 -> TEN_METERS
                maxValue > 99 -> ONE_HUNDRED_METERS
                maxValue > 9 -> ONE_KILOMETER
                else -> TEN_KILOMETERS
            }
        }
    }
}
