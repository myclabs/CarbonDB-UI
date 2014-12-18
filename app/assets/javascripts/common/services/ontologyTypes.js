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

  var mod = angular.module("common.ontologyTypes", []);

  mod.factory('ontologyTypes', ['playRoutes', function (playRoutes) {
    var impactTypes = [];
    var flowTypes = [];
    var impactTypesTree = [];
    var flowTypesTree = [];
    var relationTypes = {};
    var promise = playRoutes.controllers.Onto.getOntologyTypes(activeDatabase).get().success(function(data) {
      impactTypes = data.plain.impactTypes;
      flowTypes = data.plain.flowTypes;
      impactTypesTree = data.tree.impactTypesTree;
      flowTypesTree = data.tree.flowTypesTree;
      // sets the relation types colors
      var fill = d3.scale.category10();
      data.relationTypes.relationTypes.forEach(function(type, index) {
        relationTypes[type.id] = type;
        relationTypes[type.id].color = fill(index+1);
        relationTypes[type.id].number = index+1;

      });
    });
    return {
      promise: promise,
      getImpactTypes: function () {
        return impactTypes;
      },
      getFlowTypes: function() {
        return flowTypes;
      },
      getImpactTypesTree: function () {
        return impactTypesTree;
      },
      getFlowTypesTree: function() {
        return flowTypesTree;
      },
      getRelationTypes: function() {
        return relationTypes;
      }
    };
  }]);
  mod.factory('viewType', [function(){
    return {selection: ''};
  }]);
  return mod;
});
