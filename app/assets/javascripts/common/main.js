/**
 * Common functionality.
 */
define(["angular", "./services/helper", "./services/playRoutes", "./filters", "./directives/tmd", "./directives/charts", "./services/ontologyTypes", "./services/graph"],
    function(angular) {
  "use strict";

  return angular.module("yourprefix.common", ["common.helper", "common.playRoutes", "common.filters",
    "common.directives.tmd", "common.directives.charts", "common.ontologyTypes", "common.graph"]);
});
