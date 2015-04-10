# CarbonDB UI

## Intro

This project is the UI for the [CardonDB project](https://github.com/myclabs/carbondb):
a reasonner for the [CarbonDB Ontology project](https://github.com/myclabs/carbonontology).

It uses [Playframework 2](http://www.playframework.com), [WebJars](http://www.webjars.org),
[RequireJS](http://www.requirejs.org) and [AngularJS](http://www.angularjs.org).

## Code Organization

The JavaScript modules are organized as follows:

    |- app
    |-- assets
    |--- javascripts    <- contains all the JavaScript modules
    |---- app.js        <- app module, wires everything together
    |---- main.js       <- tells RequireJS how to load modules and bootstraps the app
    |---- common/       <- a module, in this case
    |----- main.js      <- main file of the module, loads all sub-files in this folder
    |----- filters.js   <- common's filters
    |----- directives/  <- common's directives
    |----- services/    <- common's services
    |---- ...


## Trying It Out

### Dev Mode

* Load dependencies via `play update`
* Run via `play run`
* Go to [localhost:9000](http://localhost:9000)

If any file in `app/log` is modified, you should replace the `lib/log.jar` file:

    activator compile && jar -cf lib/log.jar -C target/scala-2.10/classes log

Or launch activator UI via `activator ui`

This uses the normal JavaScript files and loads libraries from the downloaded WebJars.

### Prod Mode

* Produce executable via `play clean dist`
* Extract `unzip target/universal/carbondb-ui-x.x.x.zip`
* Run `carbondb-ui-x.x.x/bin/play-angular-require-seed -Dhttp.port=9000 -Dconfig.resource=prod.conf`

This uses the uglified JavaScript files and loads WebJars resources from the jsDelivr CDN.

Alternatively, you can find in the deploy.sh file a script we use to run the application
on [carbondb.org](http://www.carbondb.org) with [Supervisor](http://supervisord.org/).