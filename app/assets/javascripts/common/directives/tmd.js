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
        roundValues: '='
      },
      templateUrl: 'assets/templates/tmd.html',
      link: function (scope, element, attrs) {

        function createTMD(rowDimensions, lineDimensions, commonKeywords, values, roundValues) {
          console.log(values);
          var commonKeywordsCoordinate = new Array();
          for (var i = 0; i < commonKeywords.length; i++) {
            commonKeywordsCoordinate.push(commonKeywords[i].name);
          }
          var tmd = new Array();
          var value, span;
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
              if (values[coordinate] == 'empty') {
                value = '-';
              }
              else {
                var uncertainty = Math.round(values[coordinate].uncertainty*100)/100;
                if (roundValues) {
                  var value = sigFigs(values[coordinate].value, 3);
                }
                else {
                  var value = values[coordinate].value;
                }
              }
              tmd[i].push({'value': value, 'uncertainty': uncertainty, 'span': 1, 'header': false, 'col': false});
            }
          }
          //console.log(tmd);
          scope.tmd = tmd;
        }

        function sigFigs(n, sig) {
          // x = 10^{log10(n)-sig+1} * ROUND ( n / 10^{log10(n)-sig+1} )
          // n = 1000, log10(n) = 3, sig = 2
          // x = ROUND ( n * 10^{sig-log10(n)-1}) / 10^{sig-log10(n)-1}
          var mult = Math.pow(10, sig - Math.floor(Math.log(n) / Math.LN10) - 1);
          return Math.round(n * mult) / mult;
        }

        scope.$watch(
          function () {return typeof scope.rowDimensions !== 'undefined'
                           && typeof scope.lineDimensions !== 'undefined'
                           && typeof scope.values !== 'undefined'},
          function (newValue, oldValue, scope) {
            if (newValue) {
              createTMD(scope.rowDimensions, scope.lineDimensions, scope.commonKeywords, scope.values, scope.roundValues);
            }
          }
        );
      }
    };
  }]);

  return mod;
});
