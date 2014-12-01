/**
 * Home controllers.
 */
define(["angular"], function(angular) {
  "use strict";

  /** Controls the index page */
  var HomeCtrl = function($scope, $rootScope, $location, helper, $http, $window, playRoutes) {
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $rootScope.pageTitle = "CarbonDB";
    $scope.user = {'name': "toto"};
    $scope.switchDatabase = function(database) {
      $window.location.pathname = "/" + database + "/";
    };
  };
  HomeCtrl.$inject = ["$scope", "$rootScope", "$location", "helper", "$http", "$window", "playRoutes"];

  var GraphCtrl = function($scope, $rootScope, $location, $window, playRoutes) {
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $rootScope.pageTitle = "CarbonDB: Graph";
    console.log(playRoutes.controllers.Onto);
    playRoutes.controllers.Onto.getGraph(activeDatabase).get().success(function(data) {
      $scope.d3Nodes = [];
      data.nodes.forEach(function (element, index) { $scope.d3Nodes.push({'name': element, 'id': data.nodesId[index]}) });
      $scope.d3Links = data.links;
      $scope.relationTypes = data.types;
    });
  };
  GraphCtrl.$inject = ["$scope", "$rootScope", "$location", "$window", "playRoutes"];

  var TreeCtrl = function($scope, $rootScope, $location, $window, playRoutes) {
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $rootScope.pageTitle = "CarbonDB: Tree";
    playRoutes.controllers.Onto.getCategories(activeDatabase).get().success(function(data) {
      $scope.categories = data.children;
    });
    $scope.treeOptions = {
      dirSelectable: false,
      isLeaf: function (node) {
        return node.hasOwnProperty('type') ? true : false;
      }
    };
  };
  TreeCtrl.$inject = ["$scope", "$rootScope", "$location", "$window", "playRoutes"];

  var ReferencesCtrl = function($scope, $rootScope, $location, $window, playRoutes) {
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $rootScope.pageTitle = "CarbonDB: References";
    playRoutes.controllers.Onto.getReferences(activeDatabase).get().success(function(data) {
      $scope.references = data.references;
    });
  };
  ReferencesCtrl.$inject = ["$scope", "$rootScope", "$location", "$window", "playRoutes"];

  var DocumentationCtrl = function($rootScope, $scope, $window, $location, $anchorScroll) {
    $rootScope.pageTitle = "CarbonDB: Documentation";
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $scope.scrollTo = function(id) {
      $location.hash(id);
      $anchorScroll();
    }
  };
  DocumentationCtrl.$inject = ["$rootScope", "$scope", "$window", "$location", "$anchorScroll"];

  var PartnersCtrl = function($rootScope, $scope, $window, $location) {
    $rootScope.pageTitle = "CarbonDB: Partners";
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
  };
  PartnersCtrl.$inject = ["$rootScope", "$scope", "$window", "$location"];

  var ContributeCtrl = function($rootScope, $scope, $window, $location) {
    $rootScope.pageTitle = "CarbonDB: Contribute";
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
  };
  PartnersCtrl.$inject = ["$rootScope", "$scope", "$window", "$location"];

  /** Controls the upload page */
  var UploadCtrl = function($scope, $rootScope, $location, helper, $http, $upload, $window, playRoutes) {
    $rootScope.pageTitle = "CarbonDB: Upload";
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
    $scope.latest = {fileUploading: false};
    playRoutes.controllers.Onto.getLastReport('latest').get().success(function(data) {
      $scope.latest.errors = data.errors;
      $scope.latest.warnings = data.warnings;
    });
    $scope.wip = {fileUploading: false};
    playRoutes.controllers.Onto.getLastReport('wip').get().success(function(data) {
      $scope.wip.errors = data.errors;
      $scope.wip.warnings = data.warnings;
    });
    $scope.onFileSelect = function($files, slot) {
      $scope[slot].fileUploading = true;
      $scope[slot].fileUploadResult = "Uploading and processing...";
      $scope[slot].errors = null;
      $scope[slot].warnings = null;
      //$files: an array of files selected, each file has name, size, and type.
      for (var i = 0; i < $files.length; i++) {
        var file = $files[i];
        $scope.upload = $upload.upload({
          url: 'upload/' + slot, //upload.php script, node.js route, or servlet url
          // method: 'POST' or 'PUT',
          // headers: {'header-key': 'header-value'},
          // withCredentials: true,
          data: {slot: slot},
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
          $scope[slot].fileUploading = false;
          $scope[slot].fileUploadResult = data.result;
          $scope[slot].errors = data.report.errors;
          $scope[slot].warnings = data.report.warnings;
          // file is uploaded successfully
        }).error(function(data) {
          $scope[slot].fileUploading = false;
          $scope[slot].fileUploadResult = "Something went wrong when uploading the file: " + data;
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

    playRoutes.controllers.Onto.getGroup(activeDatabase, $routeParams.type + '/' + $routeParams.uri).get().success(function(data) {
      $scope.id = data.id;
      $scope.label = data.label;
      $scope.dimensionsNumber = data.dimensions.length;
      $scope.elementsNumber = data.elementsNumber;
      $scope.sourceRelations = data.sourceRelations;
      $scope.sourceRelations.sort(function(a, b) {
        if (a.source.uri == $scope.URI && b.source.uri != $scope.URI) {
          return 1;
        }
        else if (a.destination.uri == $scope.URI && b.destination.uri != $scope.URI) {
          return -1;
        }
        else if (data.type == 'COEFFICIENT') {
          if (a.source.label < b.source.label) {
            return -1;
          }
          else if (a.source.label > b.source.label) {
            return 1;
          }
          else if (a.destination.label < b.destination.label) {
            return -1;
          }
          else if (a.destination.label > b.destination.label) {
            return 1;
          }
        }
        else if (a.coeff.label < b.coeff.label) {
          return -1;
        }
        else if (a.coeff.label > b.coeff.label) {
          return 1;
        }
        return 0;
      });
      $scope.sourceRelations.forEach(function(s) {
        s.derivedRelations.forEach(function(d) {
          d.sourceLabel = d.source.keywords.map(function(k) { return k.label; }).join(' - ');
          d.coeffLabel = d.coeff.keywords.map(function(k) { return k.label; }).join(' - ');
          d.destinationLabel = d.destination.keywords.map(function(k) { return k.label; }).join(' - ');
        });
      });
      $scope.unit = data.unit.symbol;
      $scope.references = data.references.sort(sortReferencesCompare);
      $scope.tmdUnit = data.unit;
      $scope.commonKeywords = data.commonKeywords;
      $scope.elementsImpactsAndFlows = data.elementsImpactsAndFlows;
      $scope.type = data.type;
      $scope.elementsURI = data.elementsURI;
      $scope.overlap = data.overlap;
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
                if (elements.hasOwnProperty(element)) {
                    if (elements[element].hasOwnProperty($scope.viewType)) {
                      $scope.elements[element] = elements[element][$scope.viewType];
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
                    if (types[t].children[i].children[j].id == $scope.viewType) {
                      $("#viewType option:selected").text((t == 0 ? "[I]" : "[EF]") + " "
                        + types[t].children[i].label
                        + " - "
                        + types[t].children[i].children[j].label);
                      $scope.unit = types[t].children[i].children[j].unit.symbol + " / " + data.unit.symbol;
                    }
                    else {
                      $("#viewType option[value='" + types[t].children[i].children[j].id + "']")
                        .html("&nbsp;&nbsp;&nbsp;&nbsp;" + types[t].children[i].children[j].label);
                    }
                  }
                }
              }
            }
          );
          if (!viewType.selection) {
            $scope.viewType = "it/ghg_emission_measured_using_gwp_over_100_years";
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

    playRoutes.controllers.Onto.getProcess(activeDatabase, "sp/" + $routeParams.id).get().success(function(data) {
      $scope.id = data.id;
      $scope.label = data.label;
      $scope.unit = data.unit;
      $scope.impactsAndFlows = [];
      $scope.groups = data.groups;
      $scope.keywords = data.keywords.keywords.sort(sortKeywordsCompare);
      $scope.relations = data.relations;
      $scope.relations.forEach(function(r) {
        r.sourceLabel = r.source.keywords.map(function(k) { return k.label; }).join(' - ');
        r.coeffLabel = r.coeff.keywords.map(function(k) { return k.label; }).join(' - ');
        r.destinationLabel = r.destination.keywords.map(function(k) { return k.label; }).join(' - ');
      });
      $scope.relations.sort(function(a, b) {
        if (a.originId == $scope.id && b.originId != $scope.id) {
          return 1;
        }
        else if (a.destId == $scope.id && b.destId != $scope.id) {
          return -1;
        }
        else if (a.coeffLabel < b.coeffLabel) {
          return -1;
        }
        else if (a.coeffLabel > b.coeffLabel) {
          return 1;
        }
        return 0;
      });
      var types = [$scope.impactTypes, $scope.flowTypes];
      var processData = [data.impacts, data.flows];
      for (var t = 0; t < 2; t++) {
        for (var i = 0; i < types[t].children.length; i++) {
          var impactTypeCategory = types[t].children[i];
          $scope.impactsAndFlows[impactTypeCategory.id] = new Array();
          for (var j = 0; j < impactTypeCategory.children.length; j++) {
            var impactType = impactTypeCategory.children[j];
            if (processData[t].hasOwnProperty(impactType.id)) {
              var impact = {
                label: impactType.label,
                value: sigFigs(processData[t][impactType.id].value, 3),
                uncertainty: processData[t][impactType.id].uncertainty,
                unit: impactType.unit
              }
              $scope.impactsAndFlows[impactTypeCategory.id].push(impact);
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

    playRoutes.controllers.Onto.getCoefficient(activeDatabase, "sc/" + $routeParams.id).get().success(function(data) {
      $scope.id = data.id;
      $scope.label = data.label;
      $scope.unit = data.unit;
      $scope.value = data.value;
      $scope.groups = data.groups;
      $scope.keywords = data.keywords.keywords.sort(sortKeywordsCompare);
      $scope.relations = data.relations;
      $scope.relations.forEach(function(r) {
        r.sourceLabel = r.source.keywords.map(function(k) { return k.label; }).join(' - ');
        r.coeffLabel = r.coeff.keywords.map(function(k) { return k.label; }).join(' - ');
        r.destinationLabel = r.destination.keywords.map(function(k) { return k.label; }).join(' - ');
      });
      $scope.relations.sort(function(a, b) {
        if (a.sourceLabel < b.sourceLabel) {
          return -1;
        }
        else if (a.sourceLabel > b.sourceLabel) {
          return 1;
        }
        else if (a.destinationLabel < b.destinationLabel) {
          return -1
        }
        else if (a.destinationLabel > b.destinationLabel) {
          return 1;
        }
        return 0;
      });
    });

  };
  CoefficientCtrl.$inject = ["$scope", "$rootScope", "$location", "$routeParams", "$window", "playRoutes"];

  /*mod.filter('escape', function(value) {
    return encodeURIComponent(value);
  });*/

  /** Controls the header */
  var HeaderCtrl = function($scope, userService, helper, $location, $window) {
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
    $scope.activeDatabase = activeDatabase;
    $scope.$on('$routeChangeSuccess', function(){
      $scope.latestLink = $location.protocol() + "://" + $location.host() + ($location.port() != 80 ? ":"+$location.port() : "") + "/latest/#" + $location.path();
      $scope.wipLink = $location.protocol() + "://" + $location.host() + ($location.port() != 80 ? ":"+$location.port() : "") + "/wip/#" + $location.path();
    });
  };
  HeaderCtrl.$inject = ["$scope", "userService", "helper", "$location", "$window"];

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
          if (keywords[i].id == positions[position]) {
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
    DocumentationCtrl: DocumentationCtrl,
    PartnersCtrl: PartnersCtrl,
    ContributeCtrl: ContributeCtrl
  };

});
