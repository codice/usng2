<!--
Copyright (c) 2009 Larry Moore, larmoor@gmail.com
              2014 Mike Adair, Richard Greenwood, Didier Richard, Stephen Irons, Olivier Terral and Calvin Metcalf (proj4js)
              2018 Codice Foundation
Released under the MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
-->
# usng2
Java & Javascript converter for USNG (U.S. National Grid) and MGRS (Military Grid Reference System)
coordinates.

## Features
 * Convert Lat/Lon Bounding Box to closest USNG
 * Convert Lat/Lon to UTM (with or without North/South indicator)
 * Convert Lat/Lon point to USNG
 * Convert Lat/Lon point to MGRS
 * Convert UTM (with or without North/South indicator) to Lat/Lon point
 * Convert UTM to Lat/Lon Bounding Box
 * Convert USNG to UTM
 * Convert USNG to Lat/Lon point
 * Convert USNG to Lat/Lon Bounding Box
 * Get UTM Letter Designator for a given Lat
 * Get Zone Number for a give Lat/Lon point
 * Parse a USNG string

## Library Formats
This repository produces artifacts for use in both Java and Javascript projects.
The Java library can be found in the [Codice artifact repository](artifacts.codice.org)
and can be included in your projects with Maven coordinates:
```xml
        <dependency>
            <groupId>org.codice.usng2</groupId>
            <artifactId>usng2</artifactId>
            <version>[current release version]</version>
        </dependency>
```

The Javascript library is delivered as a [UMD package from npm](https://www.npmjs.com/package/usng2)
and can be included in your projects by invoking `require("usng2")`

## Usage
Example usage of this file with Cesium and OpenLayers can be found within https://github.com/codice/ddf.

Javascript users can also look at the included javascript test file,
[tests.js](https://github.com/codice/usng2/blob/master/js-test/tests/tests.js).