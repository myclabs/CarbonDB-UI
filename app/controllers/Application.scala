package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

/** Application controller, handles authentication */
object Application extends Controller with Security {

  /** Serves the index page, see views/index.scala.html */
  def index(database: String) = Action {
    if (database != "latest" && database != "wip")
      NotFound("This ontology version is not valid")
    else
      Ok(views.html.index(database))
  }

  /**
   * Returns the JavaScript router that the client can use for "type-safe" routes.
   * @param varName The name of the global variable, defaults to `jsRoutes`
   */
  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.Application.login,
        routes.javascript.Application.logout,
        routes.javascript.Users.user,
        routes.javascript.Users.createUser,
        routes.javascript.Users.updateUser,
        routes.javascript.Users.deleteUser,
        routes.javascript.Onto.upload,
        routes.javascript.Onto.getGroup,
        routes.javascript.Onto.getCategories,
        routes.javascript.Onto.getLastReport,
        routes.javascript.Onto.getGraph,
        routes.javascript.Onto.getDerivedGraph,
        routes.javascript.Onto.getOntologyTypes,
        routes.javascript.Onto.getProcess,
        routes.javascript.Onto.getCoefficient,
        routes.javascript.Onto.getReferences
        // TODO Add your routes here
      )
    ).as(JAVASCRIPT)
  }

  /**
   * Log-in a user. Pass the credentials as JSON body.
   * @return The token needed for subsequent requests
   */
  def login() = Action(parse.json) { implicit request =>
    // TODO Check credentials, log user in, return correct token
    val token = java.util.UUID.randomUUID().toString
    Ok(Json.obj("token" -> token))
  }

  /** Logs the user out, i.e. invalidated the token. */
  def logout() = Action {
    // TODO Invalidate token, remove cookie
    Ok
  }

}
