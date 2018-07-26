/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Default package
data class Usng(
    val zoneNumber: Int,
    val latitudeBandLetter: String,
    val columnLetter: String? = null,
    val rowLetter: String? = null,
    val easting: Int? = null,
    val northing: Int? = null
)
