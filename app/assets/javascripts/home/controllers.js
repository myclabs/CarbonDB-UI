/**
 * Home controllers.
 */
define(["angular"], function(angular) {
  "use strict";

  /** Controls the index page */
  var HomeCtrl = function($scope, $rootScope, $location, helper, $http, $window, playRoutes) {
    //console.log(helper.sayHi());
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $rootScope.pageTitle = "CarbonDB";
    /*playRoutes.controllers.Onto.getProcessGroups().get().success(function(data) {
      $scope.processGroups = data;
    });
    playRoutes.controllers.Onto.getCoefficientGroups().get().success(function(data) {
      $scope.coefficientGroups = data;
    });*/
    playRoutes.controllers.Onto.getCategories().get().success(function(data) {
      $scope.categories = data.children;
    });
    //$scope.categories = [{"uri":"http://www.myc-sense.com/ontologies/bc#electricity_category","label":"Electricity","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#transport_category","label":"Transport","children":[{"uri":"http://www.myc-sense.com/ontologies/bc#rail_transport","label":"http://www.myc-sense.com/ontologies/bc#rail_transport","children":[{"uri":"http://www.myc-sense.com/ontologies/bc#rail_passenger_transport","label":"http://www.myc-sense.com/ontologies/bc#rail_passenger_transport","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#rail_freight_transport","label":"http://www.myc-sense.com/ontologies/bc#rail_freight_transport","children":[]}]},{"uri":"http://www.myc-sense.com/ontologies/bc#road_transport","label":"Road transport","children":[{"uri":"http://www.myc-sense.com/ontologies/bc#road_passenger_transport","label":"Road passenger transport","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#road_freight_transport","label":"Road freight transport","children":[]}]},{"uri":"http://www.myc-sense.com/ontologies/bc#air_transport","label":"Air transport","children":[{"uri":"http://www.myc-sense.com/ontologies/bc#air_freight_transport","label":"Air freight transport","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#air_passenger_transport","label":"Air passenger transport","children":[]}]},{"uri":"http://www.myc-sense.com/ontologies/bc#ship_transport","label":"Ship transport","children":[{"uri":"http://www.myc-sense.com/ontologies/bc#ship_passenger_transport","label":"Ship passenger transport","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#ship_freight_transport","label":"Ship freight transport","children":[]}]}]},{"uri":"http://www.myc-sense.com/ontologies/bc#fuel_category","label":"Fuels","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#animal_husbandry_category","label":"Animal husbandry","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#waste","label":"Waste","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#agriculture","label":"Agriculture","children":[]},{"uri":"http://www.myc-sense.com/ontologies/bc#direct_emission_of_greenhouse_gas_category","label":"Direct emission of greenhouse gas","children":[]}];
    $scope.treeOptions = {
      dirSelectable: false,
      isLeaf: function (node) {
        return node.hasOwnProperty('unit') ? true : false;
      }
    };

    playRoutes.controllers.Onto.getGraph().get().success(function(data) {
      $scope.d3Nodes = [];
      data.nodes.forEach(function (element, index) { $scope.d3Nodes.push({'name': element, 'id': data.nodesId[index]}) });
      $scope.d3Links = data.links;
    });

    /*$scope.d3Nodes = [{'name': 'bidule'}, {}, {}, {}, {}, {}, {}, {}, {}, {}];

    $scope.d3Links = [
              { 'source': 0, 'target': 1 },
              { 'source': 4, 'target': 5 }
            ];*/
    $scope.user = {'name': "toto"};
  };
  HomeCtrl.$inject = ["$scope", "$rootScope", "$location", "helper", "$http", "$window", "playRoutes"];

  var GraphCtrl = function($scope, $rootScope, $location, $window, playRoutes) {
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $rootScope.pageTitle = "CarbonDB: Graph";

    playRoutes.controllers.Onto.getGraph().get().success(function(data) {
      $scope.d3Nodes = [];
      data.nodes.forEach(function (element, index) { $scope.d3Nodes.push({'name': element, 'id': data.nodesId[index]}) });
      $scope.d3Links = data.links;
    });
  };
  GraphCtrl.$inject = ["$scope", "$rootScope", "$location", "$window", "playRoutes"];

  var TreeCtrl = function($scope, $rootScope, $location, $window, playRoutes) {
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $rootScope.pageTitle = "CarbonDB: Tree";
    playRoutes.controllers.Onto.getCategories().get().success(function(data) {
      $scope.categories = data.children;
    });
    $scope.treeOptions = {
      dirSelectable: false,
      isLeaf: function (node) {
        return node.hasOwnProperty('unit') ? true : false;
      }
    };
  };
  TreeCtrl.$inject = ["$scope", "$rootScope", "$location", "$window", "playRoutes"];

  var ReferencesCtrl = function($scope, $rootScope, $location, $window, playRoutes) {
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $rootScope.pageTitle = "CarbonDB: References";
    playRoutes.controllers.Onto.getReferences().get().success(function(data) {
      $scope.references = data.references;
      $scope.referencesGroups = data.referencesGroups;
    });
  };
  ReferencesCtrl.$inject = ["$scope", "$rootScope", "$location", "$window", "playRoutes"];

  var AboutCtrl = function($rootScope, $scope, $window, $location) {
    $rootScope.pageTitle = "CarbonDB: About";
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
  };
  AboutCtrl.$inject = ["$rootScope", "$scope", "$window", "$location"];

  var HelpCtrl = function($rootScope, $scope, $window, $location) {
    $rootScope.pageTitle = "CarbonDB: Help";
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
  };
  HelpCtrl.$inject = ["$rootScope", "$scope", "$window", "$location"];

  var WhatsNewCtrl = function($rootScope, $scope, $window, $location) {
    $rootScope.pageTitle = "CarbonDB: What's new";
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
  };
  WhatsNewCtrl.$inject = ["$rootScope", "$scope", "$window", "$location"];

  var KnownBugsCtrl = function($rootScope, $scope, $window, $location) {
    $rootScope.pageTitle = "CarbonDB: Known bugs";
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
  };
  KnownBugsCtrl.$inject = ["$rootScope", "$scope", "$window", "$location"];

  /** Controls the upload page */
  var UploadCtrl = function($scope, $rootScope, $location, helper, $http, $upload, $window, playRoutes) {
    $rootScope.pageTitle = "CarbonDB: Upload";
    $scope.fileUploading = false;
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    playRoutes.controllers.Onto.getLastReport().get().success(function(data) {
      $scope.errors = data.errors;
      $scope.warnings = data.warnings;
    });
    $scope.onFileSelect = function($files) {
      $scope.fileUploading = true;
      $scope.fileUploadResult = "Uploading and processing...";
      $scope.errors = null;
      $scope.warnings = null;
      //$files: an array of files selected, each file has name, size, and type.
      for (var i = 0; i < $files.length; i++) {
        var file = $files[i];
        $scope.upload = $upload.upload({
          url: 'upload', //upload.php script, node.js route, or servlet url
          // method: 'POST' or 'PUT',
          // headers: {'header-key': 'header-value'},
          // withCredentials: true,
          data: {},
          file: file // or list of files: $files for html5 only
          // fileName: 'doc.jpg' or ['1.jpg', '2.jpg', ...] // to modify the name of the file
      /* customize file formData name ('Content-Desposition'), server side file variable name. 
          Default is 'file' */
          //fileFormDataName: myFile, //or a list of names for multiple files (html5).
          /* customize how data is added to formData. See #40#issuecomment-28612000 for sample code */
          //formDataAppender: function(formData, key, val){}
        }).progress(function(evt) {
          console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
        }).success(function(data, status, headers, config) {
          $scope.fileUploading = false;
          $scope.fileUploadResult = data.result;
          $scope.errors = data.report.errors;
          $scope.warnings = data.report.warnings;
          // file is uploaded successfully
        }).error(function(data) {
          $scope.fileUploadResult = "Sorry, something went wrong when uploading the file";
        });
        //.then(success, error, progress); 
        //.xhr(function(xhr){xhr.upload.addEventListener(...)})// access and attach any event listener to XMLHttpRequest.
      }
    };
  };
  UploadCtrl.$inject = ["$scope", "$rootScope", "$location", "helper", "$http", "$upload", "$window", "playRoutes"];

  var GroupCtrl = function($scope, $rootScope, $location, helper, $http, $routeParams, $window, playRoutes, ontologyTypes, viewType) {
    $rootScope.pageTitle = "CarbonDB: Group view";
    $scope.groupName = $routeParams.uri;
    $scope.impactTypes = ontologyTypes.getImpactTypesTree();
    $scope.flowTypes = ontologyTypes.getFlowTypesTree();

    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });

    playRoutes.controllers.Onto.getGroup($routeParams.type + '/' + $routeParams.uri).get().success(function(data) {
      $scope.URI = data.URI;
      $scope.label = data.label;
      $scope.dimensionsNumber = data.dimensions.length;
      $scope.elementsNumber = data.elementsNumber;
      $scope.sourceRelations = data.sourceRelations;
      $scope.unit = data.unit;
      $scope.references = data.references.sort(sortReferencesCompare);
      $scope.baseUnit = data.unit;
      $scope.commonKeywords = data.commonKeywords;
      $scope.elementsImpactsAndFlows = data.elementsImpactsAndFlows;
      $scope.type = data.type;
      $scope.elementsURI = data.elementsURI;
      if (data.comment) {
        $scope.comment = data.comment.replace(/\n/g, "<br>");
      }
      else {
        $scope.comment = false;
      }

      // setting up the line and row dimensions
      $scope.rowDimensions = new Array();
      $scope.lineDimensions = new Array();
      for (var i =0; i < data.dimensions.length; i++) {
        if (data.dimensions[i].orientation == 'VERTICAL')
          $scope.lineDimensions.push(sortKeywords(data.dimensions[i].keywords, data.dimensions[i].keywordsPosition));
        else if (data.dimensions[i].orientation == 'HORIZONTAL')
          $scope.rowDimensions.push(sortKeywords(data.dimensions[i].keywords, data.dimensions[i].keywordsPosition));
        else if (i % 2 == 0)
          $scope.lineDimensions.push(sortKeywords(data.dimensions[i].keywords, data.dimensions[i].keywordsPosition));
        else
          $scope.rowDimensions.push(sortKeywords(data.dimensions[i].keywords, data.dimensions[i].keywordsPosition));
      }

      // setting up the elements
      if (data.type == 'COEFFICIENT') {
        $scope.elements = data.elementsValue;
      }
      else { // type = 'PROCESS'
        $scope.$watch('viewType',
            function (newValue, oldValue, scope) {
              $scope.elements = {};

              var elements = data.elementsImpactsAndFlows;
              for (var element in elements) {
                if (elements[element] == 'empty') {
                  $scope.elements[element] = elements[element];
                }
                else if (elements.hasOwnProperty(element)) {
                    if (elements[element].hasOwnProperty($scope.viewType.replace(/\./g, "____"))) {
                      $scope.elements[element] = elements[element][$scope.viewType.replace(/\./g, "____")];
                    }
                    else {
                      $scope.elements[element] = {'value': 0.0, 'uncertainty': 0.0};
                    }
                }
              }
              viewType.selection = $scope.viewType;
              var types = [$scope.impactTypes, $scope.flowTypes];
              for (var t = 0; t < 2; t++) {
                for (var i = 0; i < types[t].children.length; i++) {
                  for (var j = 0; j < types[t].children[i].children.length; j++) {
                    if (types[t].children[i].children[j].uri == $scope.viewType) {
                      $("#viewType option:selected").text((t == 0 ? "[I]" : "[EF]") + " "
                        + types[t].children[i].label
                        + " - "
                        + types[t].children[i].children[j].label);
                      $scope.unit = types[t].children[i].children[j].unit + " / " + data.unit;
                    }
                    else {
                      $("#viewType option[value='" + types[t].children[i].children[j].uri + "']")
                        .html("&nbsp;&nbsp;&nbsp;&nbsp;" + types[t].children[i].children[j].label);
                    }
                  }
                }
              }
            }
          );
          if (!viewType.selection) {
            $scope.viewType = "http://www.myc-sense.com/ontologies/bc#it/ghg_emission_measured_using_gwp_over_100_years";
          }
          else {
            $scope.viewType = viewType.selection;
          }
      }
    });

  };
  GroupCtrl.$inject = ["$scope", "$rootScope", "$location", "helper", "$http", "$routeParams", "$window", "playRoutes", "ontologyTypes", "viewType"];

  var ProcessCtrl = function($scope, $rootScope, $location, $routeParams, $window, playRoutes, ontologyTypes) {
    $rootScope.pageTitle = "CarbonDB: Process view";
    $scope.impactTypes = ontologyTypes.getImpactTypesTree();
    $scope.flowTypes = ontologyTypes.getFlowTypesTree();

    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });

    playRoutes.controllers.Onto.getProcess("sp/" + $routeParams.id).get().success(function(data) {
      $scope.id = data.id;
      $scope.label = data.label;
      $scope.unit = data.unit;
      $scope.impactsAndFlows = [];
      $scope.groups = data.groups;
      $scope.keywords = data.keywords.keywords.sort(sortKeywordsCompare);
      $scope.relations = data.relations;
      var types = [$scope.impactTypes, $scope.flowTypes];
      var processData = [data.impacts, data.flows];
      for (var t = 0; t < 2; t++) {
        for (var i = 0; i < types[t].children.length; i++) {
          var impactTypeCategory = types[t].children[i];
          $scope.impactsAndFlows[impactTypeCategory.uri] = new Array();
          for (var j = 0; j < impactTypeCategory.children.length; j++) {
            var impactType = impactTypeCategory.children[j];
            var impactTypeURI = impactType.uri.replace(/\./g, "____");
            if (processData[t].hasOwnProperty(impactTypeURI)) {
              var impact = {
                label: impactType.label,
                value: sigFigs(processData[t][impactTypeURI].value, 3),
                uncertainty: processData[t][impactTypeURI].uncertainty,
                unit: impactType.unit
              }
              $scope.impactsAndFlows[impactTypeCategory.uri].push(impact);
            }
          }
        }
      }
    });

  };
  ProcessCtrl.$inject = ["$scope", "$rootScope", "$location", "$routeParams", "$window", "playRoutes", "ontologyTypes"];

  var CoefficientCtrl = function($scope, $rootScope, $location, $routeParams, $window, playRoutes) {
    $rootScope.pageTitle = "CarbonDB: Coefficient view";

    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });

    playRoutes.controllers.Onto.getCoefficient("sc/" + $routeParams.id).get().success(function(data) {
      $scope.id = data.id;
      $scope.label = data.label;
      $scope.unit = data.unit;
      $scope.value = data.value;
      $scope.groups = data.groups;
      $scope.keywords = data.keywords.keywords.sort(sortKeywordsCompare);
      $scope.relations = data.relations;
    });

  };
  CoefficientCtrl.$inject = ["$scope", "$rootScope", "$location", "$routeParams", "$window", "playRoutes"];

  /*mod.filter('escape', function(value) {
    return encodeURIComponent(value);
  });*/

  /** Controls the header */
  var HeaderCtrl = function($scope, userService, helper, $location) {
    // Wrap the current user from the service in a watch expression
    $scope.$watch(function() {
      var user = userService.getUser();
      return user;
    }, function(user) {
      $scope.user = user;
    }, true);

    $scope.logout = function() {
      userService.logout();
      $scope.user = undefined;
      $location.path("/");
    };
  };
  HeaderCtrl.$inject = ["$scope", "userService", "helper", "$location"];

  /** Controls the footer */
  var FooterCtrl = function(/*$scope*/) {
  };
  //FooterCtrl.$inject = ["$scope"];

  var sortKeywordsCompare = function (k1, k2) {
    if (k1.label > k2.label)
      return 1;
    if (k1.label < k2.label)
      return -1;
    return 0;
  }

  var sortKeywords = function (pKeywords, positions) {
    var keywords = pKeywords.slice(0);
    var sortedKeywords = [];
    for (var position in positions) {
      if (positions.hasOwnProperty(position)) {
        var indexInKeywords = -1;
        for (var i = 0; i < keywords.length; i++) {
          if (keywords[i].name == positions[position]) {
            indexInKeywords = i;
            break;
          }
        }
        if (indexInKeywords > -1) {
          sortedKeywords.push(keywords[i]);
          keywords.splice(indexInKeywords, 1);
        }
      }
    }
    if (keywords.length > 0) {
      sortedKeywords = sortedKeywords.concat(keywords.sort(sortKeywordsCompare));
    }
    return sortedKeywords;
  }

  var sortReferencesCompare = function (r1, r2) {
    if (r1.creator > r2.creator)
      return 1;
    if (r1.creator < r2.creator)
      return -1;
    if (r1.date > r2.date)
      return 1;
    if (r1.date < r2.date)
      return -1;
    return 0;
  }

  function sigFigs(n, sig) {
    var mult = Math.pow(10, sig - Math.floor(Math.log(n) / Math.LN10) - 1);
    return Math.round(n * mult) / mult;
  }

  return {
    HeaderCtrl: HeaderCtrl,
    FooterCtrl: FooterCtrl,
    HomeCtrl: HomeCtrl,
    GraphCtrl: GraphCtrl,
    TreeCtrl: TreeCtrl,
    ReferencesCtrl: ReferencesCtrl,
    GroupCtrl: GroupCtrl,
    ProcessCtrl: ProcessCtrl,
    CoefficientCtrl: CoefficientCtrl,
    UploadCtrl: UploadCtrl,
    AboutCtrl: AboutCtrl,
    HelpCtrl: HelpCtrl,
    WhatsNewCtrl: WhatsNewCtrl,
    KnownBugsCtrl: KnownBugsCtrl
  };

});
