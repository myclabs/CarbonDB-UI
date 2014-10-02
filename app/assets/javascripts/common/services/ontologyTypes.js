/** OntologyTypes service */
define(["angular"], function(angular) {
  "use strict";

  var mod = angular.module("common.ontologyTypes", []);

  mod.factory('ontologyTypes', ['playRoutes', function (playRoutes) {
    var impactTypes = new Array();
    var flowTypes = new Array();
    var promise = playRoutes.controllers.Onto.getImpactAndFlowTypes().get().success(function(data) {
      impactTypes = data.impactTypes;
      flowTypes = data.flowTypes;
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
  return mod;
});
