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

 /**
 * Common functionality.
 */
define(
  [
    "angular",
    "./services/helper",
    "./services/playRoutes",
    "./filters",
    "./directives/tmd",
    "./directives/charts",
    "./services/ontologyTypes",
    "./services/graph",
    "./services/visibility"
  ],
  function(angular) {
    "use strict";

    return angular.module(
        "yourprefix.common",
        ["common.helper",
         "common.playRoutes",
         "common.filters",
        "common.directives.tmd",
        "common.directives.charts",
        "common.ontologyTypes",
        "common.graph",
        "common.visibility"]);
  }
);
