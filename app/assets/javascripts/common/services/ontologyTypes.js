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
