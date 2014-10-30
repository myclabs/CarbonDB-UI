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
        fullPage: '='
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
                        obj.x -= adj / 2;
                        p.x   += adj / 2;
                    } else if (adj == xa2) {
                        obj.x += adj / 2;
                        p.x   -= adj / 2;
                    } else if (adj == ya1) {
                        obj.y -= adj / 2;
                        p.y   += adj / 2;
                    } else if (adj == ya2) {
                        obj.y += adj / 2;
                        p.y   -= adj / 2;
                    }
                }
                return ix;
            }
        });
    }
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
/**************************
 * End fo utility functions
 **************************/

var zoom = d3.behavior.zoom()
    //.scaleExtent([1, 10])
    .on("zoom", zoomed);

    var svg = d3.select(ele[0])
      .append('svg')
      .style('width', '100%')
      .style('height', '500px')
      .append("g")
      .call(zoom)
      .append("g");
    if (scope.fullPage) {
      $("svg").css("height", window.innerHeight - ($("svg").parent().get(0).getBoundingClientRect().top + 5));
      $("svg").css("border", "none");
    }

 function zoomed() {
  svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
}
          $window.onresize = function() {
            if (scope.fullPage)
              $("svg").css("height", window.innerHeight - ($("svg").parent().get(0).getBoundingClientRect().top + 5));
            scope.$apply();
          };

          scope.$watch(function() {
            return angular.element($window)[0].innerWidth;
          }, function() {
            scope.render(scope.nodes, scope.links, scope.fullPage);
          });
 
          scope.$watch('nodes && links && types', function(newData, oldData, scope) {
            //console.log("listener for d3Force called with newData = " + newData + " scope.nodes = " + scope.nodes);
            scope.render(scope.nodes, scope.links, scope.types, scope.fullPage);
          }, false);
 
          // ------
          // Render
          // ------
          scope.render = function(nodes, links, types, fullPage) {
            if (!nodes || !links || !types) {
              return;
            }
            svg.selectAll('*').remove();
            nodes.forEach(function (node) {
              node.moved = false;
              node.out = [];
              node.inc = [];
            });
            links.forEach(function (o, i) {
              var node = nodes[o.source];
              var outNode = nodes[o.target];
              node.out.push(o.target);
              outNode.inc.push(o.source);
            });

            svg.append("rect")
            .attr("width", 2300)
            .attr("height", 1000)
            .attr("opacity", 0)
            .style("style", "gray")
            .attr("x", -575)
            .attr("y", -250);
 

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

            if (fullPage) {
              var width = window.innerWidth,
                  height = window.innerHeight - ($("svg").parent().get(0).getBoundingClientRect().top + 5);
            }
            else {
              var width = 1150,
                height = 500;
            }

            var force = d3.layout.force()
                .nodes(nodes)
                .links(links)
                .size([width, height])
                .on("tick", tick)
              .linkDistance(50)
              .charge(-800)
              .gravity(.20)
                .start();

        var drag = force.drag()
          .on("dragstart", dragstart);

        function dragstart(d) {
          d3.select(this).classed("fixed", d.fixed = true);
        }
        function dblclick(d) {
          d3.event.stopPropagation();
          d3.select(this).classed("fixed", d.fixed = false);
          force.start();
        }

        var node = svg.selectAll(".node")
            .data(nodes);
          node.enter().append("g")
            .attr("class", "node")
            .call(drag)
            .on("dblclick", dblclick)
            .on("mousedown", function() { d3.event.stopPropagation(); });
            node.exit().remove();
            
            node.append('rect')
            .attr('rx', 5)
            .attr('ry', 5)
            .attr('width' , 90)
            .attr('height', 20)
            .attr('class', 'nodeRect');

            node.each(function(d) {
                var node  = d3.select(this),
                    rect  = node.select('.nodeRect'),
                    lines = wrap(d.name),
                    ddy   = 1.1,
                    dy    = -ddy * lines.length / 2 + .5;

                lines.forEach(function(line) {
                    var text = node.append('text')
                        .text(line)
                        .attr('class', 'nodeText')
                        .attr('dy', dy + 'em');
                    dy += ddy;
                });
            });


        var rect = node.append('rect')
          .attr('width', 80)
          .attr('height', 10)
          .attr('class', 'linkRect');
        node.append('a')
          .attr('xlink:href', function(d) { return '#/group/' + d.id; } )
          .append('text')
          .attr('text-anchor', 'middle')
          .text('View group')
          .attr('class', 'linkText');

        var mouseoutTimeout; // @todo use timeout
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

        node.each(function(d) {
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
        });

        force.start();
        var circle = svg.selectAll(".circle");
        var text = svg.selectAll(".label");

        var link = svg.selectAll(".link")
          .data(links);
            link.enter().insert("line", ".node")
            .attr("marker-end", function(d) { return "url(#end" + (d.type != '#none' ? types[d.type].number : '0') + ")"; } )
            .attr("class", "link")
            .style("stroke", function(d) { if (d.type != '#none') return types[d.type].color; });

        svg.append("defs").selectAll("marker")
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

        svg.style("opacity", 1e-6)
          .transition()
            .duration(1000)
            .style("opacity", 1);
var numTicks = 0;
function tick(e) {
    numTicks++;

    preventCollisions();
    /*nodes.forEach(function(node) {
      node.moved = false;
    });
    nodes.forEach(function(node) {
      if (node.hasOwnProperty("out")) {
        node.out.forEach(function(outNodeIndex) {
          var outNode = nodes[outNodeIndex];
          if ((node.x + 50) > outNode.x) {
            node.x -= 500 * e.alpha;
            outNode.x += 500 * e.alpha;
            node.moved = true;
          }
        });
      }
    });*/

    /*nodes.forEach(function (node, i) {
      var dx = Math.abs(1000 - node.x);
      if (i % 2 == 0) node.x -= 100 * e.alpha;
      else node.x += 100 * e.alpha;
    });*/

    nodes.forEach(function (node, i) {
      if (node.inc.length == 0 && node.out.length > 0)
        node.x -= 200 * e.alpha;
      else if (node.out.length == 0 && node.inc.length > 0)
        node.x += 200 * e.alpha;
    });

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

    node
      .attr('transform', function(d) {
          return 'translate(' + d.x + ',' + d.y + ')';
      });
}
          };
      }}
}])

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