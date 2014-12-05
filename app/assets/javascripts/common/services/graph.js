/** OntologyTypes service */
define(["angular"], function(angular) {
  "use strict";

  var mod = angular.module("common.graph", []);

  mod.factory('graph', ['playRoutes', function (playRoutes) {
    var nodes = new Array();
    var links = new Array();
    var types;
    var upstreamDepth = 2;
    var downstreamDepth = 2;
    var shown = false;
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
            var newNode = {id: nodes[nodeIndex].id, label: nodes[nodeIndex].label, fixed: true};
            filteredNodes.push(newNode);
            filterNodesAndLinks(nodeIndex, downstreamDepth, "source", false);
            filterNodesAndLinks(nodeIndex, upstreamDepth, "target", false);
        }
        else if (recursion) {
            // we copy the node so d3js does not store the node status
            var newNode = {id: nodes[nodeIndex].id, label: nodes[nodeIndex].label};
            filteredNodes.push(newNode);
        }
        if (depth > 0) {
            var invDirection = "source";
            if (direction == "source")
                invDirection = "target";
            var filteredNodeIndex = filteredNodes.length-1;
            for (var i = 0; i < links.length; i++) {
                if (links[i][direction] == nodeIndex) {
                    var link = {type: links[i].type};
                    link[direction] = recursion ? filteredNodeIndex : 0;
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
        // preparing the data structure for the graph view
        nodes.forEach(function (node) {
            node.out = [];
            node.inc = [];
        });
        links.forEach(function (link) {
            var node = nodes[link.source];
            var outNode = nodes[link.target];
            node.out.push(link.target);
            outNode.inc.push(link.source);
        });
        return {
            nodes: nodes,
            links: links,
            types: types
        };
      },
      getLocalGraph: function(nodeId) {
        filterNodesAndLinks(nodeId, 0);
        // preparing the data structure for the graph view
        filteredNodes.forEach(function (node) {
            node.out = [];
            node.inc = [];
        });
        filteredLinks.forEach(function (link) {
            var node = filteredNodes[link.source];
            var outNode = filteredNodes[link.target];
            node.out.push(link.target);
            outNode.inc.push(link.source);
        });
        return {
            nodes: filteredNodes,
            links: filteredLinks,
            types: types
        };
      },
      setUpstreamDepth: function(pUpstreamDepth) {
        upstreamDepth = pUpstreamDepth;
      },
      getUpstreamDepth: function () {
        return upstreamDepth;
      },
      setDownstreamDepth: function(pDownstreamDepth) {
        downstreamDepth = pDownstreamDepth;
      },
      getDownstreamDepth: function () {
        return downstreamDepth;
      },
      toggleShown: function() {
        shown = !shown;
      },
      isShown: function() {
        return shown;
      }
    };
  }]);
  return mod;
});
