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

 /** OntologyTypes service */
define(["angular"], function(angular) {
  "use strict";

  var mod = angular.module("common.visibility", []);

  mod.factory('visibility', [function () {
    var visibilities = {
        processGroupGraph: false,
        processGroupData: true,
        processGroupRelations: false,
        processGroupComments: true,
        processGraph: false,
        processImpacts: true,
        processFlows: false,
        processRelations: false,
        coefficientGroupData: true,
        coefficientGroupComments: false,
        coefficientGroupRelations: false,
        coefficientRelations: true
    }
    return {
      toggleVisibility: function (target) {
        if (visibilities.hasOwnProperty(target)) {
            visibilities[target] = !visibilities[target];
        }
      },
      isVisible: function(target) {
        if (visibilities.hasOwnProperty(target)) {
            return visibilities[target];
        }
        return false;
      }
    };
  }]);
  return mod;
});
