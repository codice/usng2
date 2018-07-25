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

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.fail

class CoordinateSystemTranslatorTest {
    private val coordinateSystemTranslator = CoordinateSystemTranslator()

    @Test
    fun testzoneNumber() {
        //around Arizona in the United States
        assertEquals(12, coordinateSystemTranslator.getZoneNumber(34.0, -111.0))
        //around Prescott/Chino Valley in Arizona
        assertEquals(12, coordinateSystemTranslator.getZoneNumber(34.5, -112.5))
        //immediately around Prescott city in Arizona
        assertEquals(12, coordinateSystemTranslator.getZoneNumber(34.545, -112.465))
        //around Uruguay
        assertEquals(21, coordinateSystemTranslator.getZoneNumber(-32.5, -55.5))
        //around Buenos Aires city in Argentina
        assertEquals(21, coordinateSystemTranslator.getZoneNumber(-34.5, -58.5))
        //around Merlo town in Buenos Aires
        assertEquals(21, coordinateSystemTranslator.getZoneNumber(-34.66, -58.73))
        //around Madagascar
        assertEquals(38, coordinateSystemTranslator.getZoneNumber(-18.5, 46.5))
        //around Toliara city in Madagascar
        assertEquals(38, coordinateSystemTranslator.getZoneNumber(-22.5, 43.5))
        //around Toliara city center in Madagascar
        assertEquals(38, coordinateSystemTranslator.getZoneNumber(-23.355, 43.67))
        //around Central Japan
        assertEquals(54, coordinateSystemTranslator.getZoneNumber(37.0, 140.5))
        //around Tokyo city in Japan
        assertEquals(54, coordinateSystemTranslator.getZoneNumber(35.5, 139.5))
        //around Tokyo city center in Japan
        assertEquals(54, coordinateSystemTranslator.getZoneNumber(35.69, 139.77))
        //around the international date line
        assertEquals(60, coordinateSystemTranslator.getZoneNumber(28.0, 179.0))
        //to the immediate east
        assertEquals(1, coordinateSystemTranslator.getZoneNumber(28.0, -179.0))
        //with midpoint directly on it (-180)
        assertEquals(1, coordinateSystemTranslator.getZoneNumber(28.0, -180.0))
        //with midpoint directly on it (+180)
        assertEquals(1, coordinateSystemTranslator.getZoneNumber(28.0, 180.0))
        //around the equator
        assertEquals(54, coordinateSystemTranslator.getZoneNumber(1.0, 141.0))
        //to the immediate south
        assertEquals(54, coordinateSystemTranslator.getZoneNumber(-1.0, 141.0))
        //with midpoint directly on it
        assertEquals(54, coordinateSystemTranslator.getZoneNumber(0.0, 141.0))
        //around the international date line and equator
        assertEquals(60, coordinateSystemTranslator.getZoneNumber(1.0, 179.0))
        //to the immediate west and south
        assertEquals(60, coordinateSystemTranslator.getZoneNumber(-1.0, 179.0))
        //to the immediate east and north
        assertEquals(1, coordinateSystemTranslator.getZoneNumber(1.0, -179.0))
        //to the immediate east and south
        assertEquals(1, coordinateSystemTranslator.getZoneNumber(-1.0, -179.0))
        //with midpoint directly on it (0, -180)
        assertEquals(1, coordinateSystemTranslator.getZoneNumber(0.0, -180.0))
        //with midpoint directly on it (0, +180)
        assertEquals(1, coordinateSystemTranslator.getZoneNumber(0.0, 180.0))
    }

    @Test
    fun testGetZoneLetterFromLat() {
        //around Arizona in the United States
        assertEquals("S", coordinateSystemTranslator.getUtmLetterDesignator(34.0))
        //around Prescott/Chino Valley in Arizona
        assertEquals("S", coordinateSystemTranslator.getUtmLetterDesignator(34.5))
        //immediately around Prescott city in Arizona
        assertEquals("S", coordinateSystemTranslator.getUtmLetterDesignator(34.545))
        //around Uruguay
        assertEquals("H", coordinateSystemTranslator.getUtmLetterDesignator(-32.5))
        //around Buenos Aires city in Argentina
        assertEquals("H", coordinateSystemTranslator.getUtmLetterDesignator(-34.5))
        //around Merlo town in Buenos Aires
        assertEquals("H", coordinateSystemTranslator.getUtmLetterDesignator(-34.66))
        //around Madagascar
        assertEquals("K", coordinateSystemTranslator.getUtmLetterDesignator(-18.5))
        //around Toliara city in Madagascar
        assertEquals("K", coordinateSystemTranslator.getUtmLetterDesignator(-22.5))
        //around Toliara city center in Madagascar
        assertEquals("K", coordinateSystemTranslator.getUtmLetterDesignator(-23.355))
        //around Central Japan
        assertEquals("S", coordinateSystemTranslator.getUtmLetterDesignator(37.0))
        //around Tokyo city in Japan
        assertEquals("S", coordinateSystemTranslator.getUtmLetterDesignator(35.5))
        //around Tokyo city center in Japan
        assertEquals("S", coordinateSystemTranslator.getUtmLetterDesignator(35.69))
        //around the equator
        assertEquals("N", coordinateSystemTranslator.getUtmLetterDesignator(1.0))
        //to the immediate south
        assertEquals("M", coordinateSystemTranslator.getUtmLetterDesignator(-1.0))
        //with midpoint directly on it
        assertEquals("N", coordinateSystemTranslator.getUtmLetterDesignator(0.0))
        //imediately south of north polar maximum
        assertEquals("X", coordinateSystemTranslator.getUtmLetterDesignator(83.0))
        //imediately north of north polar maximum
        assertEquals("Z", coordinateSystemTranslator.getUtmLetterDesignator(85.0))
        //directly on north polar maximum
        assertEquals("X", coordinateSystemTranslator.getUtmLetterDesignator(84.0))
        //imediately north of south polar minimum
        assertEquals("C", coordinateSystemTranslator.getUtmLetterDesignator(-79.0))
        //imediately south of south polar minimum
        assertEquals("Z", coordinateSystemTranslator.getUtmLetterDesignator(-81.0))
        //directly on south polar minimum
        assertEquals("C", coordinateSystemTranslator.getUtmLetterDesignator(-80.0))
    }

    @Test
    fun testParseUsng() {
        //should return zone=5; letter=Q
        var parts = coordinateSystemTranslator.parseUsngString("5Q")
        assertEquals(5, parts.zoneNumber)
        assertEquals('Q', parts.latitudeBandLetter)

        //should return zone=12; letter=S
        parts = coordinateSystemTranslator.parseUsngString("12S")
        assertEquals(12, parts.zoneNumber)
        assertEquals('S', parts.latitudeBandLetter)

        //should return zone=5; letter=Q; square1=K; square2=B

        parts = coordinateSystemTranslator.parseUsngString("5Q KB")
        assertEquals(5, parts.zoneNumber)
        assertEquals('Q', parts.latitudeBandLetter)
        assertEquals('K', parts.columnLetter)
        assertEquals('B', parts.rowLetter)

        //should return zone=12; letter=S; square1=V; square2=C

        parts = coordinateSystemTranslator.parseUsngString("12S VC")
        assertEquals(12, parts.zoneNumber)
        assertEquals('S', parts.latitudeBandLetter)
        assertEquals('V', parts.columnLetter)
        assertEquals('C', parts.rowLetter)

        //should return zone=5; letter=Q; square1=K; square2=B; easting=42785; northing=31517

        parts = coordinateSystemTranslator.parseUsngString("5Q KB 42785 31517")
        assertEquals(5, parts.zoneNumber)
        assertEquals('Q', parts.latitudeBandLetter)
        assertEquals('K', parts.columnLetter)
        assertEquals('B', parts.rowLetter)
        assertEquals(5, parts.precision.intValue)
        assertEquals(42785, parts.easting)
        assertEquals(31517, parts.northing)

        //should return zone=12; letter=S; square1=V; square2=C; easting=12900; northing=43292

        parts = coordinateSystemTranslator.parseUsngString("12S VC 12900 43292")
        assertEquals(12, parts.zoneNumber)
        assertEquals('S', parts.latitudeBandLetter)
        assertEquals('V', parts.columnLetter)
        assertEquals('C', parts.rowLetter)
        assertEquals(5, parts.precision.intValue)
        assertEquals(12900, parts.easting)
        assertEquals(43292, parts.northing)
    }

    @Test
    fun testParseMgrs() {
        //should return zone=5; letter=Q
        var parts = coordinateSystemTranslator.parseMgrsString("5Q")
        assertEquals(5, parts.zoneNumber)
        assertEquals('Q', parts.latitudeBandLetter)

        //should return zone=12; letter=S
        parts = coordinateSystemTranslator.parseMgrsString("12S")
        assertEquals(12, parts.zoneNumber)
        assertEquals('S', parts.latitudeBandLetter)

        //should return zone=5; letter=Q; square1=K; square2=B

        parts = coordinateSystemTranslator.parseMgrsString("5QKB")
        assertEquals(5, parts.zoneNumber)
        assertEquals('Q', parts.latitudeBandLetter)
        assertEquals('K', parts.columnLetter)
        assertEquals('B', parts.rowLetter)

        //should return zone=12; letter=S; square1=V; square2=C

        parts = coordinateSystemTranslator.parseMgrsString("12SVC")
        assertEquals(12, parts.zoneNumber)
        assertEquals('S', parts.latitudeBandLetter)
        assertEquals('V', parts.columnLetter)
        assertEquals('C', parts.rowLetter)

        //should return zone=5; letter=Q; square1=K; square2=B; easting=42785; northing=31517

        parts = coordinateSystemTranslator.parseMgrsString("5QKB4278531517")
        assertEquals(5, parts.zoneNumber)
        assertEquals('Q', parts.latitudeBandLetter)
        assertEquals('K', parts.columnLetter)
        assertEquals('B', parts.rowLetter)
        assertEquals(5, parts.precision.intValue)
        assertEquals(42785, parts.easting)
        assertEquals(31517, parts.northing)

        //should return zone=12; letter=S; square1=V; square2=C; easting=12900; northing=43292

        parts = coordinateSystemTranslator.parseMgrsString("12SVC1290043292")
        assertEquals(12, parts.zoneNumber)
        assertEquals('S', parts.latitudeBandLetter)
        assertEquals('V', parts.columnLetter)
        assertEquals('C', parts.rowLetter)
        assertEquals(5, parts.precision.intValue)
        assertEquals(12900, parts.easting)
        assertEquals(43292, parts.northing)
    }

    @Test
    fun testParseUtm() {
        //should return zone=5; letter=Q; easting=-000001; northing=2199600
        var utmCoordinateString = "5Q -000001 2199600"
        var utmCoordinate = coordinateSystemTranslator.parseUtmString(utmCoordinateString)
        assertEquals(5, utmCoordinate.zoneNumber)
        assertEquals('Q', utmCoordinate.latitudeBand)
        kotlin.test.assertEquals(-1.0, utmCoordinate.easting)
        kotlin.test.assertEquals(2199600.0, utmCoordinate.northing)
        assertEquals(Precision.ONE_METER, utmCoordinate.precision)

        //should return zone=5; letter=null; easting=-000001; northing=2199600
        utmCoordinateString = "5 -000001 2199600"
        utmCoordinate = coordinateSystemTranslator.parseUtmString(utmCoordinateString)
        assertEquals(5, utmCoordinate.zoneNumber)
        assertNull(utmCoordinate.latitudeBand)
        kotlin.test.assertEquals(-1.0, utmCoordinate.easting)
        kotlin.test.assertEquals(2199600.0, utmCoordinate.northing)
        assertEquals(Precision.ONE_METER, utmCoordinate.precision)
    }

    @Test
    fun testParseUtmBadInput() {
        assertFailsWith<ParseException> {
            coordinateSystemTranslator.parseUtmString("5Q")
        }
    }

    @Test
    fun testConvertUsngtoUtm() {
        //with single digit zone
        //should return north=2131517; east=242785; zone=5; letter=Q
        var zone = 18
        var letter = 'S'
        var sq1 = 'U'
        var sq2 = 'J'
        var easting = 23487
        var northing = 6483
        var coords = coordinateSystemTranslator.toUtm(
                UsngCoordinate.createPreciseCoordinate(
                        zone, letter, sq1, sq2, easting, northing))
        kotlin.test.assertEquals(4306483.0, floor(coords.northing))
        kotlin.test.assertEquals(323487.0, floor(coords.easting))
        assertEquals(18, coords.zoneNumber)
        assertEquals('S', coords.latitudeBand)

        //with two digit zone
        //should return north=43292; east=12900; zone=12; letter=S
        zone = 12
        letter = 'S'
        sq1 = 'V'
        sq2 = 'C'
        easting = 12900
        northing = 43292
        coords = coordinateSystemTranslator.toUtm(UsngCoordinate.createPreciseCoordinate(zone,
                letter,
                sq1,
                sq2,
                easting,
                northing))
        kotlin.test.assertEquals(3743292.0, floor(coords.northing))
        kotlin.test.assertEquals(412900.0, floor(coords.easting))
        assertEquals(12, coords.zoneNumber)
        assertEquals('S', coords.latitudeBand)

        zone = 5
        letter = 'Q'
        sq1 = 'K'
        sq2 = 'B'
        easting = 42785
        northing = 31517
        coords = coordinateSystemTranslator.toUtm(UsngCoordinate.createPreciseCoordinate(zone,
                letter,
                sq1,
                sq2,
                easting,
                northing))
        kotlin.test.assertEquals(2131517.0, floor(coords.northing))
        kotlin.test.assertEquals(242785.0, floor(coords.easting))
        assertEquals(5, coords.zoneNumber)
        assertEquals('Q', coords.latitudeBand)
    }

    @Test
    fun testConvertUtmToLatLon() {
        //with single digit zone and specifying accuracy
        var northing = 42785.0
        var easting = 131517.0
        var zone = 5
        var accuracy = 100000
        var utmCoordinate = UtmCoordinate.createUtmCoordinate(zone, easting, northing)
        var boundingBox = coordinateSystemTranslator.toBoundingBox(utmCoordinate, accuracy)
        kotlin.test.assertEquals(1.0, floor(boundingBox.north))
        kotlin.test.assertEquals(-156.0, floor(boundingBox.east))
        kotlin.test.assertEquals(0.0, floor(boundingBox.south))
        kotlin.test.assertEquals(-157.0, floor(boundingBox.west))

        //should return lat=0; east=-158
        northing = 42785.0
        easting = 131517.0
        zone = 5
        utmCoordinate = UtmCoordinate.createUtmCoordinate(zone, easting, northing)
        var latLon = coordinateSystemTranslator.toLatLon(utmCoordinate)
        kotlin.test.assertEquals(0.0, floor(latLon.latitude))
        kotlin.test.assertEquals(-157.0, floor(latLon.longitude))

        //should return north=1; east=-115; south=0; west=-116
        northing = 12900.0
        easting = 43292.0
        zone = 12
        accuracy = 100000
        utmCoordinate = UtmCoordinate.createUtmCoordinate(zone, easting, northing)
        boundingBox = coordinateSystemTranslator.toBoundingBox(utmCoordinate, accuracy)
        kotlin.test.assertEquals(1.0, floor(boundingBox.north))
        kotlin.test.assertEquals(-115.0, floor(boundingBox.east))
        kotlin.test.assertEquals(0.0, floor(boundingBox.south))
        kotlin.test.assertEquals(-116.0, floor(boundingBox.west))

        //should return lat=0; lon=-116
        northing = 12900.0
        easting = 43292.0
        zone = 12
        utmCoordinate = UtmCoordinate.createUtmCoordinate(zone, easting, northing)
        latLon = coordinateSystemTranslator.toLatLon(utmCoordinate)
        kotlin.test.assertEquals(0.0, floor(latLon.latitude))
        kotlin.test.assertEquals(-116.0, floor(latLon.longitude))

        northing = 6168016.0
        easting = 341475.0
        zone = 21
        utmCoordinate = UtmCoordinate.createUtmCoordinate(zone, easting, northing - CoordinateSystemTranslator.NORTHING_OFFSET)
        latLon = coordinateSystemTranslator.toLatLon(utmCoordinate)
        kotlin.test.assertEquals(-35.0, floor(latLon.latitude))
        kotlin.test.assertEquals(-59.0, floor(latLon.longitude))

        northing = 6168016.0
        easting = 341475.0
        zone = 21
        utmCoordinate = UtmCoordinate.createUtmCoordinateWithNS(zone, easting, northing, 'S')
        latLon = coordinateSystemTranslator.toLatLon(utmCoordinate)
        kotlin.test.assertEquals(-35.0, floor(latLon.latitude))
        kotlin.test.assertEquals(-59.0, floor(latLon.longitude))
    }

    @Test
    fun testConvertUsngToLatLon() {
        //should return north=19; east=-155; south=19; west=-155
        var usng = coordinateSystemTranslator.parseUsngString("5Q KB 42785 31517")
        var boundingBox = coordinateSystemTranslator.toBoundingBox(usng)
        kotlin.test.assertEquals(19.0, floor(boundingBox.north))
        kotlin.test.assertEquals(-156.0, floor(boundingBox.east))
        kotlin.test.assertEquals(19.0, floor(boundingBox.south))
        kotlin.test.assertEquals(-156.0, floor(boundingBox.west))

        //should return north=33; east=-111; south=33; west=-111
        usng = coordinateSystemTranslator.parseUsngString("12S VC 12900 43292")
        boundingBox = coordinateSystemTranslator.toBoundingBox(usng)
        kotlin.test.assertEquals(33.0, floor(boundingBox.north))
        kotlin.test.assertEquals(-112.0, floor(boundingBox.east))
        kotlin.test.assertEquals(33.0, floor(boundingBox.south))
        kotlin.test.assertEquals(-112.0, floor(boundingBox.west))
    }

    @Test
    fun testConvertBoundingBoxToUsng() {
        //should return 12S
        var usngString = "12S"
        var usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        var usngResult = coordinateSystemTranslator.toUsng(BoundingBoxCoordinate
                .createBoundingBox(37.0, 31.0, -108.0, -114.0))
        assertEquals(usngCoordinate, usngResult)
        assertEquals(usngString, usngResult.toString())

        //should return 12S UD
        usngString = "12S UD"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        usngResult = coordinateSystemTranslator.toUsng(BoundingBoxCoordinate
                .createBoundingBox(34.55, 34.45, -112.4, -112.3))
        assertEquals(usngCoordinate, usngResult)
        assertEquals(usngString, usngResult.toString())

        //should return 12S UD 7 1
        usngString = "12S UD 7 1"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.50, 34.45, -112.4, -112.4)))

        //should return 12S UD 65 24
        usngString = "12S UD 65 24"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.55,
                        34.55,
                        -112.465,
                        -112.47)))

        //should return 12S UD 649 241
        usngString = "12S UD 649 241"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.55,
                        34.55,
                        -112.471,
                        -112.472)))

        //should return 12S UD 6494 2412
        usngString = "12S UD 6494 2412"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.55,
                        34.55,
                        -112.47200,
                        -112.47190)))

        //should return 12S UD 649 241
        usngString = "12S UD 64941 24126"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.55,
                        34.55,
                        -112.47200,
                        -112.47199)))

        //should return 21H
        usngString = "21H"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-30.0, -35.0, -53.0, -58.0)))

        //should return 21H UB
        usngString = "21H UB"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-34.5, -35.0, -58.5, -58.5)))

        //should return 21H UB 41 63
        usngString = "21H UB 41 63"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-34.665,
                        -34.66,
                        -58.73,
                        -58.73)))

        //should return 38K
        usngString = "38K"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-11.0, -26.0, 51.0, 42.0)))

        //should return 38K LA
        usngString = "38K LA"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-21.9, -22.0, 43.7, 43.6)))

        //should return 38K LA
        usngString = "38K LA 6 6"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-22.0, -22.0, 43.7, 43.65)))

        //should return 38K LV 64 17
        usngString = "38K LV 66 12"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-23.395, -23.39, 43.70, 43.695)))

        //should return 54S
        usngString = "54S"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(41.0, 33.0, 143.0, 138.0)))

        //should return 54S UD
        usngString = "54S UD"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(35.0, 35.0, 140.0, 139.0)))

        //should return 54S UE 41 63
        usngString = "54S UE 86 51"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(35.7, 35.7, 139.75, 139.745)))
        //should return 60R
        usngString = "60R"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.0, 23.0, 179.0, 172.0)))

        //should return 1R
        usngString = "1R"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.0, 23.0, -179.0, -172.0)))

        //should return 1R BM
        usngString = "1R BM"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(28.0, 28.0, 179.9, -179.9)))

        //should return 58N
        usngString = "58N"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(8.0, 1.0, 166.0, 159.0)))

        //should return 58M
        usngString = "58M"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-1.0, -8.0, 166.0, 159.0)))

        //should return 58N
        usngString = "58N"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(8.0, -8.0, 166.0, 159.0)))

        //should return 60N
        usngString = "60N"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(8.0, 1.0, 179.0, 172.0)))

        //should return 60M
        usngString = "60M"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-1.0, -8.0, 179.0, 172.0)))

        //should return 1N
        usngString = "1N"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(8.0, 1.0, -179.0, -172.0)))

        //should return 1M
        usngString = "1M"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-1.0, -8.0, -179.0, -172.0)))

        //should return 1N AA
        usngString = "1N AA"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(0.0, 0.0, -179.9, 179.9)))

        //should return 30R
        usngString = "30R"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.0, 23.0, -1.0, -8.0)))

        //should return 31R
        usngString = "31R"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.0, 23.0, 1.0, 8.0)))

        //should return 31R
        usngString = "31R"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(34.0, 23.0, -1.0, 1.0)))

        //should return 30M
        usngString = "30M"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-1.0, -8.0, -1.0, -8.0)))

        //should return 31N
        usngString = "31N"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(8.0, 1.0, 8.0, 1.0)))

        //should return 31M
        usngString = "31M"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(-1.0, -8.0, 8.0, 1.0)))

        //should return 31N
        usngString = "31N"
        usngCoordinate = coordinateSystemTranslator.parseUsngString(usngString)
        assertEquals(usngCoordinate,
                coordinateSystemTranslator.toUsng(BoundingBoxCoordinate.createBoundingBox(8.0, -8.0, 1.0, -1.0)))
    }

    @Test
    fun testConvertLatLonToUsng() {
        //around Arizona in the United States
        //should return 12S WC 0 6
        assertEquals("12S WC 0 6",
                coordinateSystemTranslator.toUsng(
                        DecimalDegreesCoordinate.createDecimalDegreesCoordinate(34.0, -111.0),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around Prescott/Chino Valley in Arizona
        //should return 12S UD 0 0
        assertEquals("12S UD 6 1",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(34.5, -112.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //immediately around Prescott city in Arizona
        //should return 12S UD 65 23
        assertEquals("12S UD 65 23",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(34.545, -112.465),
                        Precision.ONE_KILOMETER)
                        .toString())

        //around Uruguay
        //should return 21H XE 4 0
        assertEquals("21H XE 4 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-32.5, -55.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around Buenos Aires city in Argentina
        //should return 21H UB 6 8
        assertEquals("21H UB 6 8",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-34.5, -58.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around Merlo town in Buenos Aires
        //should return 21H UB 41 63
        assertEquals("21H UB 41 63",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-34.66, -58.73),
                        Precision.ONE_KILOMETER)
                        .toString())

        //around Madagascar
        //should return 38K PE 5 5
        assertEquals("38K PE 5 5",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-18.5, 46.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around Toliara city in Madagascar
        //should return 38K LA 4 1
        assertEquals("38K LA 4 1",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-22.5, 43.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around Toliara city center in Madagascar
        //should return 38K LA 64 17
        assertEquals("38K LA 45 11",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-22.5, 43.5),
                        Precision.ONE_KILOMETER)
                        .toString())

        //around Central Japan
        //should return 54S VF 5 9
        assertEquals("54S VF 5 9",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(37.0, 140.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around Tokyo city in Japan
        //should return 54S UE 6 2
        assertEquals("54S UE 6 2",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(35.5, 139.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around Tokyo city center in Japan
        //should return 54S UE 41 63
        assertEquals("54S UE 88 50",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(35.69, 139.77),
                        Precision.ONE_KILOMETER)
                        .toString())

        //around the international date line'
        //to the immediate west
        //should return 60R US 5 5
        assertEquals("60R US 5 5",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(28.5, 175.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate east
        //should return 1R FM 4 5
        assertEquals("1R FM 4 5",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(28.5, -175.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //with date line crossing the middle
        //should return 1R BM 0 5
        assertEquals("1R BM 0 5",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(28.5, 180.0),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around the equator
        //to the immediate north
        //should return 58N BK 2 9
        assertEquals("58N BK 2 9",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(4.5, 162.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate south
        //should return 58M BA 2 0
        assertEquals("58M BA 2 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-4.5, 162.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //with equator crossing the middle
        //should return 58N BF 2 0
        assertEquals("58N BF 2 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(0.0, 162.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around the international date line and equator
        //to the immediate west and north
        //should return 60N UK 3 9
        assertEquals("60N UK 3 9",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(4.5, 175.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate west and south
        //should return 60M UA 3 0
        assertEquals("60M UA 3 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-4.5, 175.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate east and north
        //should return 1N FE 6 9
        assertEquals("1N FE 6 9",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(4.5, -175.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate east and south
        //should return 1M FR 6 0
        assertEquals("1M FR 6 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-4.5, -175.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //with crossing of date line and equator at center point
        //should return 1N AA 6 0
        assertEquals("1N AA 6 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(0.0, 180.0),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around the prime meridian
        //to the immediate west
        //should return 30R US 5 5
        assertEquals("30R US 5 5",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(28.5, -4.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate east
        //should return 31R FM 4 5
        assertEquals("31R FM 4 5",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(28.5, 4.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //with date line crossing the middle
        //should return 31R BM 0 5
        assertEquals("31R BM 0 5",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(28.5, 0.0),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //around the prime meridian and equator
        //to the immediate west and north
        //should return 30N UK 3 9
        assertEquals("30N UK 3 9",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(4.5, -4.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate west and south
        //should return 30M UA 3 0
        assertEquals("30M UA 3 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-4.5, -4.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate east and north
        //should return 31N FE 6 9
        assertEquals("31N FE 6 9",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(4.5, 4.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //to the immediate east and south
        //should return 31M FR 6 0
        assertEquals("31M FR 6 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(-4.5, 4.5),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //with crossing of prime meridian and equator at center point
        //should return 31N AA 6 0
        assertEquals("31N AA 6 0",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(0.0, 0.0),
                        Precision.TEN_KILOMETERS)
                        .toString())

        //with crossing of prime meridian and equator at center point
        //should return 31N
        assertEquals("31N",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(0.0, 0.0),
                        Precision.SIX_BY_EIGHT_DEGREES)
                        .toString())

        //with crossing of prime meridian and equator at center point
        //should return 31N AA
        assertEquals("31N AA",
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(0.0, 0.0),
                        Precision.ONE_HUNDRED_KILOMETERS)
                        .toString())
    }

    @Test
    fun testConvertLatLonToUtm() {
        val latLons = arrayOf(doubleArrayOf(34.0, -111.0), doubleArrayOf(34.5, -112.5), doubleArrayOf(34.545, -112.465), doubleArrayOf(-32.5, -55.5), doubleArrayOf(-34.5, -58.5), doubleArrayOf(-34.66, -58.73), doubleArrayOf(-18.5, 46.5), doubleArrayOf(-22.5, 43.5), doubleArrayOf(-23.355, 43.67), doubleArrayOf(37.0, 140.5), doubleArrayOf(35.5, 139.5), doubleArrayOf(35.69, 139.77), doubleArrayOf(28.5, 175.5), doubleArrayOf(28.5, -175.5), doubleArrayOf(28.5, 180.0), doubleArrayOf(4.5, 162.5), doubleArrayOf(-4.5, 162.5), doubleArrayOf(0.0, 162.5), doubleArrayOf(4.5, 175.5), doubleArrayOf(-4.5, 175.5), doubleArrayOf(4.5, -175.5), doubleArrayOf(-4.5, -175.5), doubleArrayOf(0.0, 180.0), doubleArrayOf(28.5, -4.5), doubleArrayOf(28.5, 4.5), doubleArrayOf(28.5, 0.0), doubleArrayOf(4.5, -4.5), doubleArrayOf(-4.5, -4.5), doubleArrayOf(4.5, 4.5), doubleArrayOf(-4.5, 4.5), doubleArrayOf(0.0, 0.0))

        val eastNorthZones = arrayOf(intArrayOf(500000, 3762155, 12), intArrayOf(362289, 3818618, 12), intArrayOf(365575, 3823561, 12), intArrayOf(640915, -3596850, 21), intArrayOf(362289, -3818618, 21), intArrayOf(341475, -3836700, 21), intArrayOf(658354, -2046162, 38), intArrayOf(345704, -2488944, 38), intArrayOf(364050, -2583444, 38), intArrayOf(455511, 4094989, 54), intArrayOf(363955, 3929527, 54), intArrayOf(388708, 3950262, 54), intArrayOf(353193, 3153509, 60), intArrayOf(646806, 3153509, 1), intArrayOf(206331, 3156262, 1), intArrayOf(222576, 497870, 58), intArrayOf(222576, -497870, 58), intArrayOf(221723, 0, 58), intArrayOf(333579, 497566, 60), intArrayOf(333579, -497566, 60), intArrayOf(666420, 497566, 1), intArrayOf(666420, -497566, 1), intArrayOf(166021, 0, 1), intArrayOf(353193, 3153509, 30), intArrayOf(646806, 3153509, 31), intArrayOf(206331, 3156262, 31), intArrayOf(333579, 497566, 30), intArrayOf(333579, -497566, 30), intArrayOf(666420, 497566, 31), intArrayOf(666420, -497566, 31), intArrayOf(166021, 0, 31))

        for (i in latLons.indices) {
            val utmCoordinate = coordinateSystemTranslator.toUtm(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(latLons[i][0],
                    latLons[i][1]))
            assertEquals(eastNorthZones[i][0].toLong(), (utmCoordinate.easting.toLong()))
            assertEquals(eastNorthZones[i][1].toLong(), (utmCoordinate.northing.toLong()))
            assertEquals(eastNorthZones[i][2], utmCoordinate.zoneNumber)
        }
    }

    @Test
    fun testConvertLatLonToUtmWithNS() {
        val latLons = arrayOf(doubleArrayOf(34.0, -111.0), doubleArrayOf(34.5, -112.5), doubleArrayOf(34.545, -112.465), doubleArrayOf(-32.5, -55.5), doubleArrayOf(-34.5, -58.5), doubleArrayOf(-34.66, -58.73), doubleArrayOf(-18.5, 46.5), doubleArrayOf(-22.5, 43.5), doubleArrayOf(-23.355, 43.67), doubleArrayOf(37.0, 140.5), doubleArrayOf(35.5, 139.5), doubleArrayOf(35.69, 139.77), doubleArrayOf(28.5, 175.5), doubleArrayOf(28.5, -175.5), doubleArrayOf(28.5, 180.0), doubleArrayOf(4.5, 162.5), doubleArrayOf(-4.5, 162.5), doubleArrayOf(0.0, 162.5), doubleArrayOf(4.5, 175.5), doubleArrayOf(-4.5, 175.5), doubleArrayOf(4.5, -175.5), doubleArrayOf(-4.5, -175.5), doubleArrayOf(0.0, 180.0), doubleArrayOf(28.5, -4.5), doubleArrayOf(28.5, 4.5), doubleArrayOf(28.5, 0.0), doubleArrayOf(4.5, -4.5), doubleArrayOf(-4.5, -4.5), doubleArrayOf(4.5, 4.5), doubleArrayOf(-4.5, 4.5), doubleArrayOf(0.0, 0.0))

        val eastNorthZonesNS = arrayOf(intArrayOf(500000, 3762155, 12, 'N'.toInt()), intArrayOf(362289, 3818618, 12, 'N'.toInt()), intArrayOf(365575, 3823561, 12, 'N'.toInt()), intArrayOf(640915, 6403149, 21, 'S'.toInt()), intArrayOf(362289, 6181381, 21, 'S'.toInt()), intArrayOf(341475, 6163299, 21, 'S'.toInt()), intArrayOf(658354, 7953837, 38, 'S'.toInt()), intArrayOf(345704, 7511055, 38, 'S'.toInt()), intArrayOf(364050, 7416555, 38, 'S'.toInt()), intArrayOf(455511, 4094989, 54, 'N'.toInt()), intArrayOf(363955, 3929527, 54, 'N'.toInt()), intArrayOf(388708, 3950262, 54, 'N'.toInt()), intArrayOf(353193, 3153509, 60, 'N'.toInt()), intArrayOf(646806, 3153509, 1, 'N'.toInt()), intArrayOf(206331, 3156262, 1, 'N'.toInt()), intArrayOf(222576, 497870, 58, 'N'.toInt()), intArrayOf(222576, 9502129, 58, 'S'.toInt()), intArrayOf(221723, 0, 58, 'N'.toInt()), intArrayOf(333579, 497566, 60, 'N'.toInt()), intArrayOf(333579, 9502433, 60, 'S'.toInt()), intArrayOf(666420, 497566, 1, 'N'.toInt()), intArrayOf(666420, 9502433, 1, 'S'.toInt()), intArrayOf(166021, 0, 1, 'N'.toInt()), intArrayOf(353193, 3153509, 30, 'N'.toInt()), intArrayOf(646806, 3153509, 31, 'N'.toInt()), intArrayOf(206331, 3156262, 31, 'N'.toInt()), intArrayOf(333579, 497566, 30, 'N'.toInt()), intArrayOf(333579, 9502433, 30, 'S'.toInt()), intArrayOf(666420, 497566, 31, 'N'.toInt()), intArrayOf(666420, 9502433, 31, 'S'.toInt()), intArrayOf(166021, 0, 31, 'N'.toInt()))

        for (i in latLons.indices) {
            val utmCoordinate = coordinateSystemTranslator.toUtmWithNS(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(latLons[i][0],
                    latLons[i][1]))
            assertEquals(eastNorthZonesNS[i][0].toLong(), (utmCoordinate.easting.toLong()))
            assertEquals(eastNorthZonesNS[i][1].toLong(), (utmCoordinate.northing.toLong()))
            assertEquals(eastNorthZonesNS[i][2], utmCoordinate.zoneNumber)
            assertEquals(eastNorthZonesNS[i][3].toLong(), (utmCoordinate.nsIndicator?.toLong()))
        }
    }

    @Test
    fun testGithubData() {
        val latLons = arrayOf(doubleArrayOf(39.9489, -75.15), doubleArrayOf(39.277881, -76.622639), doubleArrayOf(38.88, -77.07), doubleArrayOf(40.7484, -73.9857), doubleArrayOf(34.1341, -118.3217), doubleArrayOf(27.9881, 86.9253), doubleArrayOf(38.8977, -77.0366), doubleArrayOf(38.8895, -77.0352), doubleArrayOf(36.8206, -76.0333), doubleArrayOf(34.2364, -77.9542), doubleArrayOf(-36.0872, -72.8078), doubleArrayOf(-36.1333, -72.7833), doubleArrayOf(-36.1222, -72.8044))

        val eastNorthZones = arrayOf(intArrayOf(4422096, 487186, 18), intArrayOf(4348868, 360040, 18), intArrayOf(4305496, 320444, 18), intArrayOf(4511322, 585628, 18), intArrayOf(3777813, 378131, 11), intArrayOf(3095886, 492654, 45), intArrayOf(4307395, 323385, 18), intArrayOf(4306483, 323486, 18), intArrayOf(4075469, 407844, 18), intArrayOf(3792316, 227899, 18), intArrayOf(6004156, 697374, 18), intArrayOf(5998991, 699464, 18), intArrayOf(6000266, 697593, 18))

        val usngStrings = arrayOf("18S VK 87187 22096", "18S UJ 60040 48869", "18S UJ 20444 05497", "18T WL 85628 11322", "11S LT 78132 77814", "45R VL 92654 95886", "18S UJ 23386 07396", "18S UJ 23487 06483", "18S VF 07844 75469", "18S TC 27900 92317", "18H XF 97375 04155", "18H XE 99464 98991", "18H XF 97593 00265")

        for (i in usngStrings.indices) {
            val usngCoordinate = coordinateSystemTranslator.parseUsngString(usngStrings[i])
            executeGithubDataTest(latLons[i][0],
                    latLons[i][1],
                    eastNorthZones[i][0],
                    eastNorthZones[i][1],
                    eastNorthZones[i][2],
                    usngCoordinate)
        }
    }

    private fun executeGithubDataTest(
        lat: Double,
        lon: Double,
        utmNorthing: Int,
        utmEasting: Int,
        zoneNum: Int,
        usng: UsngCoordinate
    ) {
        var adjustableUtmNorthing = utmNorthing
        if (lat < 0) {
            adjustableUtmNorthing -= 10000000
        }

        var utmCoordinate: UtmCoordinate = coordinateSystemTranslator.toUtm(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(lat, lon))
        val utmToLL: DecimalDegreesCoordinate
        val usngToLL: DecimalDegreesCoordinate = coordinateSystemTranslator.toLatLon(usng)

        assertEquals(utmEasting.toLong(), (utmCoordinate.easting.toLong()))
        assertEquals(adjustableUtmNorthing.toLong(), (utmCoordinate.northing.toLong()))
        assertEquals(zoneNum, utmCoordinate.zoneNumber)

        utmCoordinate = UtmCoordinate.createUtmCoordinate(zoneNum, utmEasting.toDouble(), adjustableUtmNorthing.toDouble())
        utmToLL = coordinateSystemTranslator.toLatLon(utmCoordinate)

        assertEquals(round(lat * 10000), round(utmToLL.latitude * 10000))
        assertEquals(round(lon * 10000), round(utmToLL.longitude * 10000))

        assertEquals(usng,
                coordinateSystemTranslator.toUsng(DecimalDegreesCoordinate.createDecimalDegreesCoordinate(lat, lon),
                        Precision.ONE_METER))

        assertEquals(round(lat * 10000), round(usngToLL.latitude * 10000))
        assertEquals(round(lon * 10000), round(usngToLL.longitude * 10000))
    }

    @Test
    fun testLLBoxToUSNG() {
        //should return 18S UJ 2349 0648
        var usngString = "18S UJ 2349 0648"
        var expected = coordinateSystemTranslator.parseUsngString(usngString)
        var lat = 38.8895
        var lon = -77.0352
        var lon2 = -77.0351
        var boundingBox = BoundingBoxCoordinate.createBoundingBox(lat, lat, lon, lon2)
        var actual = coordinateSystemTranslator.toUsng(boundingBox)
        assertEquals(expected, actual)

        //should return 18S UJ 234 064
        usngString = "18S UJ 234 064"
        expected = coordinateSystemTranslator.parseUsngString(usngString)
        lat = 38.8895
        lon = -77.0352
        lon2 = -77.035
        boundingBox = BoundingBoxCoordinate.createBoundingBox(lat, lat, lon, lon2)
        actual = coordinateSystemTranslator.toUsng(boundingBox)
        assertEquals(expected, actual)
    }

    @Test
    fun testLLPointtoUSNG() {

        // TODO RAP 21 Jul 18: Clean up this test
        val usngStrings = arrayOf("18S UJ 23487 06483", "18S UJ 23 06", "18S UJ 2 0", "18S UJ", "17S")

        val precisions = arrayOf(null, Precision.ONE_KILOMETER, Precision.TEN_KILOMETERS, Precision.ONE_HUNDRED_KILOMETERS, Precision.SIX_BY_EIGHT_DEGREES)

        val expectedValues = arrayOf(doubleArrayOf(38.8895, -77.0352), doubleArrayOf(38.8895, -77.033), doubleArrayOf(38.8895, -77.06), doubleArrayOf(38.8895, -77.2), doubleArrayOf(38.8895, -80.0))

        for (i in usngStrings.indices) {
            val expected = coordinateSystemTranslator.parseUsngString(usngStrings[i])
            val decimalDegreesCoordinate = DecimalDegreesCoordinate.createDecimalDegreesCoordinate(expectedValues[i][0],
                    expectedValues[i][1])

            val actual =
                    if (precisions[i] == null) {
                        coordinateSystemTranslator.toUsng(decimalDegreesCoordinate)
                    } else {
                        coordinateSystemTranslator.toUsng(decimalDegreesCoordinate, precisions[i]!!)
                    }

            assertEquals(expected.columnLetter, actual.columnLetter)
            assertEquals(expected.latitudeBandLetter, actual.latitudeBandLetter)
            assertEquals(expected.rowLetter, actual.rowLetter)
            assertEquals(expected.easting, actual.easting)
            assertEquals(expected.northing, actual.northing)
            assertEquals(expected.zoneNumber, actual.zoneNumber)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun testUsngToLatLon() {
        //should return 38.8895 -77.0352
        var usng = coordinateSystemTranslator.parseUsngString("18S UJ 23487 06483")
        val lat = 38.8895
        val lon = -77.0352
        val llResult = coordinateSystemTranslator.toLatLon(usng)
        println("lat = $lat and resultLat = ${llResult.latitude}")
        assertEquals(lat, llResult.latitude, 0.0001)
        assertEquals(lon, llResult.longitude, 0.0001)

        val inputValues = arrayOf("18S UJ 2349 0648", "18S UJ 234 064", "18S UJ 23 06", "18S UJ 2 0", "18S UJ", "17S", "14R")

        val expectedValues = arrayOf(doubleArrayOf(38.8895, -77.0352, -77.0351, 38.8895), doubleArrayOf(38.8896, -77.0361, -77.0350, 38.8887), doubleArrayOf(38.8942, -77.0406, -77.0294, 38.8850), doubleArrayOf(38.9224, -77.0736, -76.9610, 38.8304), doubleArrayOf(39.7440, -77.3039, -76.1671, 38.8260), doubleArrayOf(40.0, -84.0, -78.0, 32.0), doubleArrayOf(32.0, -102.0, -96.0, 24.0))

        for (i in inputValues.indices) {
            usng = coordinateSystemTranslator.parseUsngString(inputValues[i])
            val result = coordinateSystemTranslator.toBoundingBox(usng)
            validateUsngToLatLonResult(expectedValues[i], result)
        }
    }

    private fun validateUsngToLatLonResult(expectedValues: DoubleArray, result: BoundingBoxCoordinate) {
        assertEquals(expectedValues[0], result.north, 0.0001)
        assertEquals(expectedValues[1], result.west, 0.0001)
        assertEquals(expectedValues[2], result.east, 0.0001)
        assertEquals(expectedValues[3], result.south, 0.0001)
    }

    private fun assertEquals(expected: Double, actual: Double, epsilon: Double = 0.01) {
        if (expected != actual) {
            if (abs(expected - actual) > epsilon) {
                fail("Expected <$expected>, actual <$actual>.")
            }
        }
    }
}
