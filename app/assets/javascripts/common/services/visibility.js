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
