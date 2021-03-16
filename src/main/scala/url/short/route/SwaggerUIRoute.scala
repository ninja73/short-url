package url.short.route

import akka.http.scaladsl.server.Directives.{getFromResourceDirectory, pathPrefix}
import akka.http.scaladsl.server.Route

object SwaggerUIRoute {
  val route: Route = pathPrefix("swagger-ui") {
    getFromResourceDirectory("swagger-ui")
  }
}
