/**
 * A common directive.
 * It would also be ok to put all directives into one file, or to define one RequireJS module
 * that references them all.
 */
define(["angular"], function(angular) {
  "use strict";

  var mod = angular.module("common.directives.charts", []);

mod.directive('d3Force', ['$window',
  function($window) {
    return {
      restrict: 'A',
      scope: {
        nodes: '=',
        links: '=',
        types: '=',
        label: '@',
        onClick: '&',
        fullPage: '=',
        local: '=',
        nodeId: '=nodeId',
        derived: '='
      },
      link: function(scope, ele, attrs) {
/*******************
 * Utility functions
 *******************/
var maxLineChars = 26,
    wrapChars    = ' /_-.'.split('');
var wrap = function wrap(text) {
    if (text.length <= maxLineChars) {
        return [text];
    } else {
        for (var k = 0; k < wrapChars.length; k++) {
            var c = wrapChars[k];
            for (var i = maxLineChars; i >= 0; i--) {
                if (text.charAt(i) === c) {
                    var line = text.substring(0, i + 1);
                    return [line].concat(wrap(text.substring(i + 1)));
                }
            }
        }
        return [text.substring(0, maxLineChars)]
            .concat(wrap(text.substring(maxLineChars)));
    }
}
var preventCollisions = function() {
    var quadtree = d3.geom.quadtree(scope.nodes);

    for (var key in scope.nodes) {
        var obj = scope.nodes[key],
            ox1 = obj.x + obj.extent.left,
            ox2 = obj.x + obj.extent.right,
            oy1 = obj.y + obj.extent.top,
            oy2 = obj.y + obj.extent.bottom;

        if (true) {
        quadtree.visit(function(quad, x1, y1, x2, y2) {
            if (quad.point && quad.point !== obj) {
                // Check if the rectangles intersect
                var p   = quad.point,
                    px1 = p.x + p.extent.left,
                    px2 = p.x + p.extent.right,
                    py1 = p.y + p.extent.top,
                    py2 = p.y + p.extent.bottom,
                    ix  = (px1 <= ox2 && ox1 <= px2 && py1 <= oy2 && oy1 <= py2);
                if (ix) {
                    var xa1 = ox2 - px1, // shift obj left , p right
                        xa2 = px2 - ox1, // shift obj right, p left
                        ya1 = oy2 - py1, // shift obj up   , p down
                        ya2 = py2 - oy1, // shift obj down , p up
                        adj = Math.min(xa1, xa2, ya1, ya2);

                    if (adj == xa1) {
                        if (!obj.fixed)
                            obj.x -= adj / 2;
                        if (!p.fixed)
                            p.x   += adj / 2;
                    } else if (adj == xa2) {
                        if (!obj.fixed)
                            obj.x += adj / 2;
                        if (!p.fixed)
                            p.x   -= adj / 2;
                    } else if (adj == ya1) {
                        if (!obj.fixed)
                            obj.y -= adj / 2;
                        if (!p.fixed)
                            p.y   += adj / 2;
                    } else if (adj == ya2) {
                        if (!obj.fixed)
                            obj.y += adj / 2;
                        if (!p.fixed)
                            p.y   -= adj / 2;
                    }
                }
                return ix;
            }
        });}
    }
}

var setBoudingBox = function(d) {
    var node   = d3.select(this),
        text   = node.selectAll('.nodeText'),
        bounds = {},
        first  = true;

    text.each(function() {
        var box = this.getBBox();
        if (first || box.x < bounds.x1) {
            bounds.x1 = box.x;
        }
        if (first || box.y < bounds.y1) {
            bounds.y1 = box.y;
        }
        if (first || box.x + box.width > bounds.x2) {
            bounds.x2 = box.x + box.width;
        }
        if (first || box.y + box.height > bounds.y2) {
            bounds.y2 = box.y + box.height;
        }
        first = false;
    }).attr('text-anchor', 'middle');

    var oldWidth = bounds.x2 - bounds.x1;

    bounds.x1 -= oldWidth / 2;
    bounds.x2 -= oldWidth / 2;

    bounds.x1 -= 5; //padding.left;
    bounds.y1 -= 5; //padding.top;
    bounds.x2 += 5; //padding.left + padding.right;
    bounds.y2 += 5; //padding.top  + padding.bottom;

    adaptNodeToBounds(node, bounds);

    d.extent = {
        left   : bounds.x1 - 10, //margin.left,
        right  : bounds.x2 + 10, //margin.left + margin.right,
        top    : bounds.y1 - 10, //margin.top,
        bottom : bounds.y2 + 10 //margin.top  + margin.bottom
    };

    d.edge = {
        left   : new geo.LineSegment(bounds.x1, bounds.y1, bounds.x1, bounds.y2),
        right  : new geo.LineSegment(bounds.x2, bounds.y1, bounds.x2, bounds.y2),
        top    : new geo.LineSegment(bounds.x1, bounds.y1, bounds.x2, bounds.y1),
        bottom : new geo.LineSegment(bounds.x1, bounds.y2, bounds.x2, bounds.y2)
    };
}

var adaptNodeToBounds = function(node, bounds) {
  node.select('.nodeRect')
    .attr('x', bounds.x1)
    .attr('y', bounds.y1)
    .attr('width' , bounds.x2 - bounds.x1)
    .attr('height', bounds.y2 - bounds.y1);
  node.select('.linkRect')
    .attr('x', bounds.x1 + 5)
    .attr('y', bounds.y2)
    .attr('width' , bounds.x2 - bounds.x1 - 10);

  node.select('.linkText')
    .attr('x', bounds.x1 + (bounds.x2 - bounds.x1)/2)
    .attr('y', bounds.y2 + 7)
    .attr('width' , bounds.x2 - bounds.x1 - 10);
};

var dragstart = function(d) {
    d3.select(this).classed("fixed", d.fixed = true);
    d3.select(this).classed("moved", d.moved = true);
    d3.event.sourceEvent.stopPropagation();
}

var dblclick = function(d) {
    d3.event.stopPropagation();
    d3.select(this).classed("fixed", d.fixed = false);
    //force.start();
}
/**************************
 * End fo utility functions
 **************************/

scope.$watch('nodes', function(newData, oldData, scope) {
    if (scope.nodes || scope.links || scope.types) {
        scope.render(scope.nodes, scope.links, scope.types, scope.fullPage, scope.local, scope.derived, scope.nodeId);
    }
});

// ------
// Render
// ------
scope.render = function(nodes, links, types, fullPage, local, derived, nodeId) {

    var zoom = d3.behavior.zoom()
        //.scaleExtent([1, 10])
        .on("zoom", zoomed);

    // create the svg
    //console.log(angular.element(ele)[0].offsetWidth);
    d3.select(ele[0]).selectAll('*').remove();
    var svg = d3.select(ele[0])
        .append('svg')
        .attr('width', window.innerWidth);
    var gZoom = svg.append("g")
        .call(zoom)
        .append("g");
    if (fullPage) {
        svg.attr("height", window.innerHeight - ($("svg").parent().get(0).getBoundingClientRect().top + 5));
        $("svg").css("border", "none");
    }
    else {
        svg.attr('height', '500');
    }

    $("svg").on("resize", function() { console.log("resize"); });

    function zoomed() {
        gZoom.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    }

    // this rectangle is here for the pan and should cover all svg area
    gZoom.append("rect")
        .attr("width", 100000)
        .attr("height", 100000)
        .attr("opacity", 0)
        .attr("x", -50000)
        .attr("y", -50000);

    // setting the arrows heads color based on the relation types
    // fill is also used for the edges line color
    var fill = d3.scale.category10();
    var numberOfTypes = 0;
    var typesEndMarkers = [{id: 'end0', color: '#aaa'}];
    for(var type in types) {
        if (types.hasOwnProperty(type)) {
            types[type].color = fill(++numberOfTypes);
            types[type].number = numberOfTypes;
            typesEndMarkers.push({id: "end" + numberOfTypes, color: fill(numberOfTypes)});
        }
    }

    // set the force layout parameters
    var force = d3.layout.force()
        .nodes(nodes)
        .links(links)
        .size([svg.attr("width"), svg.attr("height")])
        .on("tick", tick)
        .linkDistance(50)
        .charge(local ? -2000 : -800)
        .gravity(.20);

    var drag = force.drag()
               .on("dragstart", dragstart);

    // set the nodes properties
    var node = gZoom.selectAll(".node").data(nodes);
    node.enter().append("g")
        .attr("class", "node")
        .classed("fixed", function (n) { return n.fixed })
        .call(drag)
        .on("dblclick", dblclick)
        .on("mousedown", function() { d3.event.stopPropagation(); })
        .append('rect')
        .attr('rx', 5)
        .attr('ry', 5)
        .attr('width' , 90)
        .attr('height', 20)
        .attr('class', 'nodeRect')
    // node group link rectangle
    node.append('rect')
        .attr('width', 80)
        .attr('height', function() { return local ? 14 : 10} )
        .attr('class', 'linkRect');
    // node group link
    node.append('a')
        .attr('xlink:href', function(d) { return derived ? '#/process/' + d.id : '#/group/' + d.id; } )
        .append('text')
        .attr('text-anchor', 'middle')
        .text(function() { return derived ? 'View process' : 'View group'; })
        .attr('dy', function() { return local ? 3 : 0; } )
        .classed({'linkText' : true, 'linkTextLocal' : local});
    node.exit().remove();

    // add the nodes multiline label
    node.each(function(d) {
        var node  = d3.select(this),
            lines = wrap(d.label),
            ddy   = 1.1,
            dy    = -ddy * lines.length / 2 + .5;

        lines.forEach(function(line) {
            var text = node.append('text')
                .text(line)
                .classed({'nodeText' : true, 'nodeTextLocal' : local})
                .attr('dy', dy + 'em');
            dy += ddy;
        });
    });


    // add mouseover event to show the group link
    node.on('mouseover', function(d) {
        d3.select(this).select('.linkRect')
          .transition(300)
          .style("opacity",100);
        d3.select(this).select('.linkText')
          .transition()
          .style("opacity",100);
    })
    .on('mouseout', function(d) {
        d3.select(this).select('.linkRect')
          .transition(300)
          .style("opacity",0);
        d3.select(this).select('.linkText')
          .transition(300)
          .style("opacity",0);
    });

    // bounding boxes calculation
    node.each(setBoudingBox);

    force.start();

    d3.select(window).on("resize", resize);
    resize(true);

    function resize(init) {
        var width = window.innerWidth;
        var height = svg.attr("height");
        if (fullPage) {
            height = window.innerHeight - ($("svg").parent().get(0).getBoundingClientRect().top + 5);
        }
        if (init || !local || width != svg.attr("width") || height != svg.attr("height")) {
            node.each(function (node) {
                if (local && node.fixed && node.id == nodeId) {
                    node.px = (width / 2) + node.extent.left / 2;
                    node.py = (height / 2) + node.extent.top / 2;
                }
                else if (node.fixed) {
                    node.px += (width - svg.attr("width")) / 2;
                    node.py += (height - svg.attr("height")) / 2;
                }
            });
            svg.attr("height", height)
               .attr("width", width);
            force.size([width, height]).resume();
        }
        init = false;
    }

    // add the edges
    var link = gZoom.selectAll(".link")
                  .data(links);
    link.enter().insert("line", ".node")
        .attr("marker-end", function(d) { return "url(#end" + (d.type != '#none' ? types[d.type].number : '0') + ")"; } )
        .attr("class", "link")
        .style("stroke", function(d) { if (d.type != '#none') return types[d.type].color; });

    // add the edges arrow head
    gZoom.append("defs").selectAll("marker")
        .data(typesEndMarkers)
        .enter().append("marker")
        .attr("id", function(d) { return d.id; })
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 10)
        .attr("refY", 0)
        .attr("markerWidth", 6)

        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .style("stroke", function(d) { return d.color; })
        .style("fill", function(d) { return d.color; })
        .append("path")
        .attr("d", "M0,-5L10,0L0,5");

    // set the svg opacity in order to hide the beginning of force process
    gZoom.style("opacity", 1e-6)
        .transition()
        .duration(1000)
        .style("opacity", 1);

    function tick(e) {
        preventCollisions();
        // move the nodes without upstream node to the left
        // and those without downstream nodes to the right
        nodes.forEach(function (node, i) {
            if (!node.fixed) {
                if (node.inc.length == 0 && node.out.length > 0) {
                    node.x -= 200 * e.alpha;
                }
                else if (node.out.length == 0 && node.inc.length > 0)
                    node.x += 200 * e.alpha;
            }
        });

        // move the links according to their nodes position
        link
          .attr('x1', function(d) {
            return d.source.x;
          })
          .attr('y1', function(d) {
            return d.source.y;
          })
          .each(function(d) {
            var x    = d.target.x,
                y    = d.target.y,
                line = new geo.LineSegment(d.source.x, d.source.y, x, y);

            for (var e in d.target.edge) {
              var ix = line.intersect(d.target.edge[e].offset(x, y));
              if (ix.in1 && ix.in2) {
                x = ix.x;
                y = ix.y;
                break;
              }
            }

            d3.select(this)
              .attr('x2', x)
              .attr('y2', y);
          });

        // move the nodes according to their new position
        node
          .attr('transform', function(d) {
              return 'translate(' + d.x + ',' + d.y + ')';
          });
    }
};
      }}; // end of link
}]); // end of directive

mod.directive('tooltip', function(){
    return {
        restrict: 'A',
        link: function(scope, element, attrs){
            $(element).hover(function(){
                // on mouseenter
                $(element).tooltip('show');
            }, function(){
                // on mouseleave
                $(element).tooltip('hide');
            });
        }
    };
});

  return mod;
});