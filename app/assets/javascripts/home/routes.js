/**
 * Home routes.
 */
define(["angular", "./controllers", "common"], function(angular, controllers) {
  "use strict";

  var mod = angular.module("home.routes", ["yourprefix.common"]);
  mod.config(["$routeProvider", function($routeProvider) {
    $routeProvider
      .when("/",  {templateUrl: "/assets/templates/home/home.html", controller:controllers.HomeCtrl})
      .when("/graph",  {templateUrl: "/assets/templates/home/graph.html", controller:controllers.GraphCtrl})
      .when("/tree",  {templateUrl: "/assets/templates/home/tree.html", controller:controllers.TreeCtrl})
      .when("/references",  {templateUrl: "/assets/templates/home/references.html", controller:controllers.ReferencesCtrl})
      .when("/upload",  {templateUrl: "/assets/templates/home/upload.html", controller:controllers.UploadCtrl})
      .when("/documentation",  {templateUrl: "/assets/templates/home/documentation.html", controller:controllers.DocumentationCtrl})
      .when("/partners",  {templateUrl: "/assets/templates/home/partners.html", controller:controllers.PartnersCtrl})
      .when("/contribute",  {templateUrl: "/assets/templates/home/contribute.html", controller:controllers.ContributeCtrl})
      .when("/group/:type/:uri",
        {
          templateUrl: "/assets/templates/home/group.html",
          controller:controllers.GroupCtrl,
          resolve:{
            'ontologyTypesData': ["ontologyTypes", function(ontologyTypes) {
              // hack to initialize the service when the group url is requested
              return ontologyTypes.promise;
            }
          ]}
        })
      .when("/process/sp/:id",
        {
          templateUrl: "/assets/templates/home/process.html",
          controller:controllers.ProcessCtrl,
          resolve:{
            'ontologyTypesData': ["ontologyTypes", function(ontologyTypes) {
              // hack to initialize the service when the group url is requested
              return ontologyTypes.promise;
            }
          ]}
        })
      .when("/coefficient/sc/:id", {templateUrl: "/assets/templates/home/coefficient.html", controller:controllers.CoefficientCtrl})
      .otherwise( {templateUrl: "/assets/templates/home/notFound.html"});
  }]);
  return mod;
});
