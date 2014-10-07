/** OntologyTypes service */
define(["angular"], function(angular) {
  "use strict";

  var mod = angular.module("common.ontologyTypes", []);

  mod.factory('ontologyTypes', ['playRoutes', function (playRoutes) {
    var impactTypes = new Array();
    var flowTypes = new Array();
    var promise = playRoutes.controllers.Onto.getImpactAndFlowTypes().get().success(function(data) {
      impactTypes = data.impactTypesTree;
      flowTypes = data.flowTypesTree;
    });
    return {
      promise: promise,
      getImpactTypes: function () {
        return impactTypes;
      },
      getFlowTypes: function() {
        return flowTypes;
      }
    };
  }]);
  mod.factory('viewType', [function(){
    return {selection: ''};
  }]);
  return mod;
});
