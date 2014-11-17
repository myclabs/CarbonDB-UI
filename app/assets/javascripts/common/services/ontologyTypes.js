/** OntologyTypes service */
define(["angular"], function(angular) {
  "use strict";

  var mod = angular.module("common.ontologyTypes", []);

  mod.factory('ontologyTypes', ['playRoutes', function (playRoutes) {
    var impactTypes = new Array();
    var flowTypes = new Array();
    var impactTypesTree = new Array();
    var flowTypesTree = new Array();
    var promise = playRoutes.controllers.Onto.getImpactAndFlowTypes(activeDatabase).get().success(function(data) {
      impactTypes = data.plain.impactTypes;
      flowTypes = data.plain.flowTypes;
      impactTypesTree = data.tree.impactTypesTree;
      flowTypesTree = data.tree.flowTypesTree;
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
      }
    };
  }]);
  mod.factory('viewType', [function(){
    return {selection: ''};
  }]);
  return mod;
});
