/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-UI project <http://www.carbondb.org>
 *
 * CarbonDB-UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-UI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

 /**
 * Home routes.
 */
define(["angular", "./controllers", "common"], function(angular, controllers) {
  "use strict";

  var mod = angular.module("home.routes", ["yourprefix.common"]);
  mod.config(["$routeProvider", function($routeProvider) {
    $routeProvider
      .when("/",  {templateUrl: "/assets/templates/home/home.html", controller:controllers.HomeCtrl})
      .when("/graph",
        {
          templateUrl: "/assets/templates/home/graph.html",
          controller:controllers.GraphCtrl,
          resolve:{
            'ontologyTypesData': ["ontologyTypes", function(ontologyTypes) {
              // hack to initialize the service when the graph is requested
              return ontologyTypes.promise;
            }
          ]}
        })
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
