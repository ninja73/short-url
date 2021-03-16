package url.short.route
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.scalalogging.LazyLogging

object CustomExceptionHandler extends LazyLogging {
  implicit def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e => extractUri { uri =>
      logger.error(s"Something went wrong, route $uri", e)
      complete(StatusCodes.InternalServerError, s"Something went wrong, route $uri")
    }
  }
}