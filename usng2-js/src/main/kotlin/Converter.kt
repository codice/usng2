/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Default package
import org.codice.usng2.BoundingBoxCoordinate
import org.codice.usng2.CoordinateSystemTranslator
import org.codice.usng2.DecimalDegreesCoordinate
import org.codice.usng2.Precision
import org.codice.usng2.UsngCoordinate
import org.codice.usng2.UtmCoordinate

class Converter(isNad83Datum: Boolean) {
    private val translator = CoordinateSystemTranslator(isNad83Datum)

    companion object {
        @JsName("NORTHING_OFFSET")
        const val NORTHING_OFFSET = CoordinateSystemTranslator.NORTHING_OFFSET
    }

    @JsName("llBboxToUsng")
    fun llBboxToUsng(north: Double, south: Double, east: Double, west: Double): String {
        val latLonCoordinate = BoundingBoxCoordinate.createBoundingBox(north,
                south,
                east,
                west)
        println(translator.toUsng(latLonCoordinate).toString())
        return translator.toUsng(latLonCoordinate).toString()
    }

    @JsName("llToUtm")
    fun llToUtm(lat: Double, lon: Double): Utm {
        val ll = DecimalDegreesCoordinate.createDecimalDegreesCoordinate(lat, lon)
        val utm = translator.toUtm(ll)
        return utmCoordinateOutput(utm)
    }

    @JsName("llToUtmWithNS")
    fun llToUtmWithNS(lat: Double, lon: Double): Utm {
        val ll = DecimalDegreesCoordinate.createDecimalDegreesCoordinate(lat, lon)
        val utm = translator.toUtmWithNS(ll)
        return utmCoordinateOutput(utm)
    }

    @JsName("llToUsng")
    fun llToUsng(lat: Double, lon: Double, precision: Int): String {
        return llToUsngObject(lat, lon, precision).toString()
    }

    @JsName("llToMgrs")
    fun llToMgrs(lat: Double, lon: Double, precision: Int): String {
        return llToUsngObject(lat, lon, precision).toMgrsString()
    }

    /**
     * Converts lat/lng to USNG coordinates.  Calls LLtoUTM first, then
     * converts UTM coordinates to a USNG string.
     * Returns string of the format: DDL LL DDDD DDDD (4-digit precision), eg:
     * "18S UJ 2286 0705" locates Washington Monument in Washington, D.C.
     * to a 10-meter precision.
     *
     * Precision refers to levels of USNG precision. Ie a precision of
     * 0 returns a string in the form DDL
     * 1 returns a string in the form DDL LL
     * 2 returns a string in the form DDL LL D D
     * etc.
     *
     * This interpretation of precision requires decrementing the value
     * prior looking up the {@code Precision} enum value.
     */
    private fun llToUsngObject(lat: Double, lon: Double, precision: Int): UsngCoordinate {
        val ll = DecimalDegreesCoordinate.createDecimalDegreesCoordinate(lat, lon)
        return translator.toUsng(ll, Precision.fromInt(precision - 1))
    }

    @JsName("utmToLl")
    fun utmToLl(northing: Double, easting: Double, zoneNumber: Int): LatLon {
        val utm = UtmCoordinate.createUtmCoordinate(zoneNumber, easting, northing)
        val latLon = translator.toLatLon(utm)
        return LatLon(latLon.latitude, latLon.longitude)
    }

    @JsName("utmToLlWithNS")
    fun utmToLlWithNS(
        northing: Double,
        easting: Double,
        zoneNumber: Int,
        nsIndicator: String
    ): LatLon {
        val utm = UtmCoordinate.createUtmCoordinateWithNS(zoneNumber, easting,
                northing, nsIndicator.first())
        val latLon = translator.toLatLon(utm)
        return LatLon(latLon.latitude, latLon.longitude)
    }

    @JsName("utmToBoundingBox")
    fun utmToBoundingBox(northing: Double, easting: Double, zoneNumber: Int, accuracy: Int?):
            BoundingBox {
        val utm = UtmCoordinate.createUtmCoordinate(zoneNumber, easting, northing)
        val bb = translator.toBoundingBox(utm, accuracy)
        return BoundingBox(bb.north, bb.south, bb.east, bb.west)
    }

    @Suppress("LongParameterList")
    @JsName("usngToUtm")
    fun usngToUtm(
        zoneNumber: Int,
        latitudeBandLetter: String,
        columnLetter: String,
        rowLetter: String,
        easting: Int,
        northing: Int
    ): Utm {
        val usng = UsngCoordinate.createPreciseCoordinate(zoneNumber,
                latitudeBandLetter.first(),
                columnLetter.first(),
                rowLetter.first(),
                easting,
                northing)
        val utm = translator.toUtm(usng)
        return utmCoordinateOutput(utm)
    }

    @JsName("usngToLl")
    fun usngToLl(usngString: String): LatLon {
        val usng = UsngCoordinate.parseUsngString(usngString)
        val latLon = translator.toLatLon(usng)
        return LatLon(latLon.latitude, latLon.longitude)
    }

    @JsName("usngToBoundingBox")
    fun usngToBoundingBox(usngString: String): BoundingBox {
        val usng = UsngCoordinate.parseUsngString(usngString)
        val bb = translator.toBoundingBox(usng)
        return BoundingBox(bb.north, bb.south, bb.east, bb.west)
    }

    @JsName("utmLetterDesignator")
    fun utmLetterDesignator(lat: Double): String {
        return translator.getUtmLetterDesignator(lat)
    }

    @JsName("getZoneNumber")
    fun getZoneNumber(lat: Double, lon: Double): Int {
        return translator.getZoneNumber(lat, lon)
    }

    @JsName("parseUsngString")
    fun parseUsngString(usngString: String): Usng {
        return usngCoordinateOutput(translator.parseUsngString(usngString))
    }

    private fun utmCoordinateOutput(input: UtmCoordinate): Utm {
        return Utm(input.zoneNumber,
                input.easting,
                input.northing,
                input.latitudeBand?.toString(),
                input.nsIndicator?.toString())
    }

    private fun usngCoordinateOutput(input: UsngCoordinate): Usng {
        return Usng(input.zoneNumber,
                input.latitudeBandLetter.toString(),
                input.columnLetter?.toString(),
                input.rowLetter?.toString(),
                input.easting,
                input.northing)
    }
}
