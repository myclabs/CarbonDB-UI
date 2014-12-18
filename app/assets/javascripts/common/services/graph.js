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

  var mod = angular.module("common.graph", []);

  mod.factory('graph', ['playRoutes', function (playRoutes) {
    var nodes = new Array();
    var links = new Array();
    var derivedNodes = new Array();
    var derivedLinks = new Array();
    var types;
    var upstreamDepth = 2;
    var downstreamDepth = 2;
    var derivedUpstreamDepth = 2;
    var derivedDownstreamDepth = 2;
    var shown = false;
    var derivedShown = false;
    var promise = playRoutes.controllers.Onto.getGraph(activeDatabase).get().success(function(data) {
        nodes = data.nodes;
        links = data.links;
        types = data.types;
    });
    var derivedPromise = playRoutes.controllers.Onto.getDerivedGraph(activeDatabase).get().success(function(data) {
        derivedNodes = data.nodes;
        derivedLinks = data.links;
        types = data.types;
    });
    var filteredLinks = new Array();
    var filteredNodes = new Array();
    var filterNodesAndLinks = function(nodes, links, nodeIndex, upstreamDepth, downstreamDepth) {
        filteredLinks = new Array();
        filteredNodes = new Array();
        nodeIndex = findIndex(nodes, nodeIndex);
        var newNode = {id: nodes[nodeIndex].id, label: nodes[nodeIndex].label, fixed: true};
        filteredNodes.push(newNode);
        filterSubNodesAndLinks(nodes, links, nodeIndex, downstreamDepth, "source", 0);
        filterSubNodesAndLinks(nodes, links, nodeIndex, upstreamDepth, "target", 0);
    }
    // private
    var filterSubNodesAndLinks = function(nodes, links, nodeIndex, depth, direction, filteredNodeIndex) {
        if (depth > 0) {
            var invDirection = "source";
            if (direction == "source")
                invDirection = "target";
            for (var i = 0; i < links.length; i++) {
                if (links[i][direction] == nodeIndex) {
                    // we copy the node so d3js does not store the node status
                    var newNode = {id: nodes[links[i][invDirection]].id, label: nodes[links[i][invDirection]].label};
                    filteredNodes.push(newNode);
                    var link = {type: links[i].type};
                    link[direction] = filteredNodeIndex;
                    link[invDirection] = filteredNodes.length-1;
                    filteredLinks.push(link);
                    filterSubNodesAndLinks(nodes, links, links[i][invDirection], depth-1, direction, filteredNodes.length-1);
                }
            }
        }
    }
    function findIndex(nodes, nodeId) {
        var foundIndex = -1;
        nodes.forEach(function (node, index) {
            if (node.id == nodeId) foundIndex = index;
        });
        return foundIndex;
    }
    return {
      promise: promise,
      derivedPromise: derivedPromise,
      getGraph: function () {
        var nodesCopy = [];
        // preparing the data structure for the graph view
        nodes.forEach(function (node) {
            var newNode = {id: node.id, label: node.label};
            newNode.out = [];
            newNode.inc = [];
            nodesCopy.push(newNode);
        });
        var linksCopy = [];
        links.forEach(function (link) {
            var node = nodesCopy[link.source];
            var outNode = nodesCopy[link.target];
            node.out.push(link.target);
            outNode.inc.push(link.source);
            var newLink = {type: link.type, source: link.source, target: link.target};
            linksCopy.push(newLink);
        });
        return {
            nodes: nodesCopy,
            links: linksCopy,
            types: types
        };
      },
      getLocalGraph: function(nodeId) {
        filterNodesAndLinks(nodes, links, nodeId, upstreamDepth, downstreamDepth);
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
      getLocalDerivedGraph: function(nodeId) {
        filterNodesAndLinks(derivedNodes, derivedLinks, nodeId, derivedUpstreamDepth, derivedDownstreamDepth);
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
      },
      setDerivedUpstreamDepth: function(pDerivedUpstreamDepth) {
        derivedUpstreamDepth = pDerivedUpstreamDepth;
      },
      getDerivedUpstreamDepth: function () {
        return derivedUpstreamDepth;
      },
      setDerivedDownstreamDepth: function(pDerivedDownstreamDepth) {
        derivedDownstreamDepth = pDerivedDownstreamDepth;
      },
      getDerivedDownstreamDepth: function () {
        return derivedDownstreamDepth;
      },
      toggleDerivedShown: function() {
        derivedShown = !derivedShown;
      },
      isDerivedShown: function() {
        return derivedShown;
      }
    };
  }]);
  return mod;
});
