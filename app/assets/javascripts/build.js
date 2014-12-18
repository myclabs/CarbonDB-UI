/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-UI project <http://www.carbondb.org>
 *
 * CarbonDB-UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-UI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

 /*
 * A custom build profile that is passed to the optimizer via requireJsShim in build.sbt.
 * Play does this via settings it as the mainConfigFile:
 * http://requirejs.org/docs/optimization.html#mainConfigFile
 */
requirejs.config({
  packages: ["common", "home", "user", "dashboard"],
  paths: {
    // Make the optimizer ignore CDN assets
    "_" : "empty:",
    "jquery": "empty:",
    "bootstrap": "empty:",
    "angular": "empty:",
    "angular-cookies": "empty:",
    "angular-route": "empty:",
    "angular-sanitize": "empty:",
    "angular-file-upload": "empty:",
    "angular-tree-control": "empty:",
    "d3js": "empty:",
    // empty: so the optimizer doesn't try to find jsRoutes.js in our project
    "jsRoutes" : "empty:"
  }
});
