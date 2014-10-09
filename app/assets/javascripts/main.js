(function(requirejs) {
  "use strict";

  // -- DEV RequireJS config --
  requirejs.config({
    // Packages = top-level folders; loads a contained file named "main.js"
    packages: ["common", "home", "user", "dashboard"],
    shim: {
      "jsRoutes" : {
        deps : [],
        // it's not a RequireJS module, so we have to tell it what var is returned
        exports : "jsRoutes"
      }
    },
    paths: {
      "jsRoutes" : "/jsroutes"
    }
  });

requirejs.config({
  paths: { "angular-tree-control": "angular-tree-control" },
  shim: { "angular-tree-control": [ "angular" ] }
});

  requirejs.onError = function(err) {
    console.log(err);
  };

  // Load the app. This is kept minimal so it doesn't need much updating.
  require(["angular", "angular-cookies", "angular-sanitize", "angular-route", "angular-file-upload", "angular-tree-control", "jquery", "bootstrap", "d3js", "./app"],
    function(angular) {
      angular.bootstrap(document, ["app"]);
    }
  );
})(requirejs);
