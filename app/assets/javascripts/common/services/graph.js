/** OntologyTypes service */
define(["angular"], function(angular) {
  "use strict";

  var mod = angular.module("common.graph", []);

  mod.factory('graph', ['playRoutes', function (playRoutes) {
    var nodes = new Array();
    var links = new Array();
    var types;
    var promise = playRoutes.controllers.Onto.getGraph(activeDatabase).get().success(function(data) {
      nodes = data.nodes;
      links = data.links;
      types = data.types;
    });
    var filteredLinks = new Array();
    var filteredNodes = new Array();
    var filterNodesAndLinks = function(nodeIndex, depth, direction, recursion) {
        if (typeof recursion === 'undefined') {
            filteredLinks = new Array();
            filteredNodes = new Array();
            nodeIndex = findIndex(nodeIndex);
            filteredNodes.push(nodes[nodeIndex]);
            filterNodesAndLinks(nodeIndex, depth, "source", false);
            filterNodesAndLinks(nodeIndex, depth, "target", false);
        }
        else if (recursion) {
            filteredNodes.push(nodes[nodeIndex]);
        }
        if (depth > 0) {
            var invDirection = "source";
            if (direction == "source")
                invDirection = "target";
            for (var i = 0; i < links.length; i++) {
                if (links[i][direction] == nodeIndex) {
                    var link = {type: links[i].type};
                    link[direction] = recursion ? filteredNodes.length-1 : 0;
                    link[invDirection] = filteredNodes.length;
                    filteredLinks.push(link);
                    filterNodesAndLinks(links[i][invDirection], depth-1, direction, true);
                }
            }
        }
    }
    function findIndex(nodeId) {
        var foundIndex = -1;
        nodes.forEach(function (node, index) {
            if (node.id == nodeId) foundIndex = index;
        });
        return foundIndex;
    }
    return {
      promise: promise,
      getGraph: function () {
        return {
            nodes: nodes,
            links: links,
            types: types
        };
      },
      getLocalGraph: function(nodeId, depth) {
        filterNodesAndLinks(nodeId, depth);
        return {
            nodes: filteredNodes,
            links: filteredLinks,
            types: types
        };
      }
    };
  }]);
  return mod;
});
