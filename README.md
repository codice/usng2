<!--
Copyright (c) 2009 Larry Moore, larmoor@gmail.com
              2014 Mike Adair, Richard Greenwood, Didier Richard, Stephen Irons, Olivier Terral and
                   Calvin Metcalf (proj4js)
              2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
-->
# usng2

## This project is currently hibernating. Please see [usng.js](https://github.com/codice/usng.js) and [usng4j](https://github.com/codice/usng4j)

[![Download the latest release at http://artifacts.codice.org/content/repositories/snapshots/org/codice/usng2/usng2/1.0-SNAPSHOT/](https://img.shields.io/badge/Download-1.0--SNAPSHOT-green.svg)](http://artifacts.codice.org/content/repositories/snapshots/org/codice/usng2/usng2/1.0-SNAPSHOT/)
[![Download the npm release at https://www.npmjs.com/package/usng2](https://img.shields.io/badge/npm-0.99-brightgreen.svg)](https://www.npmjs.com/package/usng2)
[![Join the chat at https://groups.google.com/forum/#!forum/usng2-developers](https://img.shields.io/badge/Google%20Group-Join%20the%20chat-blue.svg)](https://groups.google.com/forum/#!forum/usng2-developers)

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

## Discussion
* Discuss USNG2 in our forum [usng2-developers](https://groups.google.com/forum/#!forum/usng2-developers)
* For more support options, check our [Support guide](https://github.com/codice/usng2/blob/master/.github/SUPPORT.md)
* Contributors should check our [Contributor guidelines](https://github.com/codice/usng2/blob/master/.github/CONTRIBUTING.md)

## License
[MIT](https://github.com/codice/usng2/blob/master/LICENSE.md)
