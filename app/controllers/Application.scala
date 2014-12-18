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
        routes.javascript.Onto.getOntologyStats,
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
