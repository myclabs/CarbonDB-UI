/**
 * Home routes.
 */
define(["angular", "./controllers", "common"], function(angular, controllers) {
  "use strict";

  var mod = angular.module("home.routes", ["yourprefix.common"]);
  mod.config(["$routeProvider", function($routeProvider) {
    $routeProvider
      .when("/",  {templateUrl: "/assets/templates/home/home.html", controller:controllers.HomeCtrl})
      .when("/upload",  {templateUrl: "/assets/templates/home/upload.html", controller:controllers.UploadCtrl})
      .when("/about",  {templateUrl: "/assets/templates/home/about.html", controller:controllers.AboutCtrl})
      .when("/help",  {templateUrl: "/assets/templates/home/help.html", controller:controllers.HelpCtrl})
      .when("/whats-new",  {templateUrl: "/assets/templates/home/whats-new.html", controller:controllers.WhatsNewCtrl})
      .when("/known-bugs",  {templateUrl: "/assets/templates/home/known-bugs.html", controller:controllers.KnownBugsCtrl})
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
