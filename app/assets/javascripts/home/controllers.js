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
    $scope.d3Data = [
      {name: "Greg", score: 98},
      {name: "Ari", score: 96},
      {name: 'Q', score: 75},
      {name: "Loser", score: 48}
    ];

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

  var AboutCtrl = function($rootScope, $scope, $window, $location) {
    $rootScope.pageTitle = "CarbonDB";
    $scope.user = {'name': "toto"};
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
  };
  AboutCtrl.$inject = ["$rootScope", "$scope", "$window", "$location"];

  var HelpCtrl = function($rootScope, $scope, $window, $location) {
    $rootScope.pageTitle = "CarbonDB";
    $scope.user = {'name': "toto"};
    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });
  };
  HelpCtrl.$inject = ["$rootScope", "$scope", "$window", "$location"];

  /** Controls the upload page */
  var UploadCtrl = function($scope, $rootScope, $location, helper, $http, $upload, $window, playRoutes) {
    $rootScope.pageTitle = "CarbonDB";
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

  var GroupCtrl = function($scope, $rootScope, $location, helper, $http, $routeParams, $window, playRoutes, ontologyTypes) {
    $rootScope.pageTitle = "CarbonDB";
    $scope.groupName = $routeParams.uri;
    $scope.impactTypes = ontologyTypes.getImpactTypes();
    $scope.flowTypes = ontologyTypes.getFlowTypes();
    $scope.viewType = "http://www____myc-sense____com/ontologies/bc#ti/ghg_emission_measured_using_gwp_over_100_years";

    if ($location.host() != 'localhost')
      $window.ga('send', 'pageview', { page: $location.path() });

    playRoutes.controllers.Onto.getGroup($routeParams.type + '/' + $routeParams.uri).get().success(function(data) {
      $scope.URI = data.URI;
      $scope.label = data.label;
      $scope.dimensionsNumber = data.dimensions.length;
      $scope.elementsNumber = data.elementsNumber;
      $scope.sourceRelations = data.sourceRelations;
      $scope.unit = data.unit;
      $scope.commonKeywords = data.commonKeywords;
      $scope.type = data.type;

      // setting up the line and row dimensions
      $scope.rowDimensions = new Array();
      $scope.lineDimensions = new Array();
      var sortKeywordsCompare = function (k1, k2) {
        if (k1.label > k2.label)
          return 1;
        if (k1.label < k2.label)
          return -1;
        return 0;
      }
      for (var i =0; i < data.dimensions.length; i++) {
        if (data.dimensions[i].orientation == 'VERTICAL')
          $scope.lineDimensions.push(data.dimensions[i].keywords.sort(sortKeywordsCompare));
        else if (data.dimensions[i].orientation == 'HORIZONTAL')
          $scope.rowDimensions.push(data.dimensions[i].keywords.sort(sortKeywordsCompare));
        else if (i % 2 == 0)
          $scope.lineDimensions.push(data.dimensions[i].keywords.sort(sortKeywordsCompare));
        else
          $scope.rowDimensions.push(data.dimensions[i].keywords.sort(sortKeywordsCompare));
      }
      $scope.$watch('viewType',
          function (newValue, oldValue, scope) {
            $scope.elements = {};
            var elements = data.elementsFlows;
            if ($scope.impactTypes.hasOwnProperty($scope.viewType)) {
              elements = data.elementsImpacts;
            }
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
          }
        );
    });

  };
  GroupCtrl.$inject = ["$scope", "$rootScope", "$location", "helper", "$http", "$routeParams", "$window", "playRoutes", "ontologyTypes"];

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

  return {
    HeaderCtrl: HeaderCtrl,
    FooterCtrl: FooterCtrl,
    HomeCtrl: HomeCtrl,
    GroupCtrl: GroupCtrl,
    UploadCtrl: UploadCtrl,
    AboutCtrl: AboutCtrl,
    HelpCtrl: HelpCtrl
  };

});
