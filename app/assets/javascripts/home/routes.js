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
      .when("/group/:type/:uri",
        {
          templateUrl: "/assets/templates/home/group.html",
          controller:controllers.GroupCtrl,
          resolve:{
            'ontologyTypesData': function(ontologyTypes) {
              // hack to initialize the service when the group url is requested
              return ontologyTypes.promise;
            }
          }
        })
      .otherwise( {templateUrl: "/assets/templates/home/notFound.html"});
  }]);
  return mod;
});
