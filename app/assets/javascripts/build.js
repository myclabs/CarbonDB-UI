/*
 * A custom build profile that is passed to the optimizer via requireJsShim in build.sbt.
 * Play does this via settings it as the mainConfigFile:
 * http://requirejs.org/docs/optimization.html#mainConfigFile
 */
requirejs.config({
  packages: ["common", "home", "user", "dashboard"],
  paths: {
    // Make the optimizer ignore CDN assets
    "_" : "empty:",
    "jquery": "empty:",
    "bootstrap": "empty:",
    "angular": "empty:",
    "angular-cookies": "empty:",
    "angular-route": "empty:",
    "angular-upload-file": "empty:",
    "angular-tree-control": "empty:",
    "d3js": "empty:",
    // empty: so the optimizer doesn't try to find jsRoutes.js in our project
    "jsRoutes" : "empty:"
  }
});
