/**
 * A common directive.
 * It would also be ok to put all directives into one file, or to define one RequireJS module
 * that references them all.
 */
define(["angular"], function(angular) {
  "use strict";

  var mod = angular.module("common.directives.tmd", []);

  mod.directive("tmd", [function() {
    return {
      restrict: "AE",
      scope: {
        rowDimensions: '=',
        lineDimensions: '=',
        commonKeywords: '=',
        values: '=',
        roundValues: '=',
        uris: '=',
        type: '='
      },
      templateUrl: 'assets/templates/tmd.html',
      link: function (scope, element, attrs) {

        function createTMD(rowDimensions, lineDimensions, commonKeywords, values, roundValues, uris, type) {
          var commonKeywordsCoordinate = new Array();
          for (var i = 0; i < commonKeywords.length; i++) {
            commonKeywordsCoordinate.push(commonKeywords[i].name);
          }
          var tmd = new Array();
          var value, span, uri;
          var rowsCoordinates = new Array();
          var repetitions = 1, numberOfRows =1, numberOfLines = 1;
          for (var i = 0; i < rowDimensions.length; i++) {
            tmd[i] = new Array();
            // ajout des colonnes des lignes d'en tête de dimension
            if (lineDimensions.length > 0) {
              tmd[i].push({'value': '', 'span': lineDimensions.length, 'header': true, 'col': true});
            }
            numberOfRows *= rowDimensions[i].length;
            span = 1;
            for (var j = i + 1; j < rowDimensions.length; j++) {
              span *= rowDimensions[j].length;
            }
            // ajout des headers des dimensions colonne
            for (var j = 0; j < rowDimensions[i].length * repetitions; j++) {
              value = rowDimensions[i][j % rowDimensions[i].length];
              tmd[i].push({'value': value.label, 'span': span, 'header': true, 'col': true});
              for (var k = j*span; k < (j*span)+span; k++) {
                if (!(k in rowsCoordinates)) {
                  rowsCoordinates[k] = new Array();
                }
                rowsCoordinates[k].push(value.name);
              }
            }
            repetitions *= rowDimensions[i].length;
          }

          var lasti = i;
          for (var i = 0; i < rowsCoordinates.length; i++) {
            rowsCoordinates[i].sort();
          }
          for (var i =0; i < lineDimensions.length; i++) {
            numberOfLines *= lineDimensions[i].length;
          }
          numberOfLines += rowDimensions.length;
          for (var i = lasti; i < numberOfLines; i++) {
            var lineCoordinate = new Array();
            tmd[i] = new Array();
            var currentLine = i - lasti;
            for (var j = 0; j < lineDimensions.length; j++) {
              span = 1;
              for (var k = j + 1; k < lineDimensions.length; k++) {
                span *= lineDimensions[k].length;
              }
              value = lineDimensions[j][parseInt(currentLine / span) % lineDimensions[j].length];
              lineCoordinate.push(value.name);
              if ((currentLine % span) == 0) {
                tmd[i].push({'value': value.label, 'span': span, 'header': true, 'col': false});
              }
            }
            for (var j = 0; j < (rowDimensions.length == 0 ? 1 : numberOfRows); j++) {
              var coordinateList = lineCoordinate.concat(commonKeywordsCoordinate);
              if (rowDimensions.length > 0) {
                coordinateList = coordinateList.concat(rowsCoordinates[j]);
              }
              var coordinate = coordinateList.sort().join("").replace(/\./g, "____");
              if (!(coordinate in values)) {
                value = '-';
                uri = false;
              }
              else {
                var uncertainty = Math.round(values[coordinate].uncertainty*100)/100;
                if (roundValues && values[coordinate].value != 0) {
                  var value = sigFigs(values[coordinate].value, 3);
                }
                else {
                  var value = values[coordinate].value;
                }
                uri = uris[coordinate];
              }
              tmd[i].push({'value': value, 'uncertainty': uncertainty, 'span': 1, 'header': false, 'col': false, 'uri': uri});
            }
          }
          scope.tmd = tmd;
        }

        function sigFigs(n, sig) {
          var mult = Math.pow(10, sig - Math.floor(Math.log(n) / Math.LN10) - 1);
          return Math.round(n * mult) / mult;
        }
        scope.$watch('values',
          function (newValue, oldValue, scope) {
            if (typeof scope.rowDimensions !== 'undefined'
                           && typeof scope.lineDimensions !== 'undefined'
                           && typeof scope.values !== 'undefined') {
              createTMD(scope.rowDimensions, scope.lineDimensions, scope.commonKeywords, scope.values, scope.roundValues, scope.uris, scope.type);
            }
          }
        );
        /*scope.$watch(
          function () {return typeof scope.rowDimensions !== 'undefined'
                           && typeof scope.lineDimensions !== 'undefined'
                           && typeof scope.values !== 'undefined'},
          function (newValue, oldValue, scope) {
            if (newValue) {
              createTMD(scope.rowDimensions, scope.lineDimensions, scope.commonKeywords, scope.values, scope.roundValues);
            }
          }
        );*/
      }
    };
  }]);

  return mod;
});
