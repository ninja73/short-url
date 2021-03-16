package url.short.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import url.short.hash.Hash
import url.short.store.store.{InMemoryStore, Store}
import url.short.utils

import javax.ws.rs._
import javax.ws.rs.core.{MediaType => JMediaType}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

final case class TargetURL(url: String)

class ShortRoute(state: InMemoryStore)(implicit actorSystem: ActorSystem,
                                       storage: Store[String, InMemoryStore, Future],
                                       hashids: Hash[String])
  extends Directives with LazyLogging {

  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)

  val route: Route = lookup ~ create

  @Path("/{short}")
  @GET
  @Operation(summary = "redirect to target url", description = "transform short url to target url and redirect",
    parameters = Array(new Parameter(name = "short", in = ParameterIn.PATH, description = "short url")),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "redirect"),
      new ApiResponse(responseCode = "400", description = "Failed decide short"),
      new ApiResponse(responseCode = "404", description = "Short url not found in store"),
      new ApiResponse(responseCode = "500", description = "Internal server error"),
    ))
  def lookup: Route = get {
    path(RemainingPath) { shortUrl =>
      onComplete(state.get(shortUrl.toString)) {
        case Failure(e) => complete(StatusCodes.InternalServerError, e.getMessage)
        case Success(None) => complete(StatusCodes.NotFound, s"target url not found for $shortUrl")
        case Success(Some(targetUrl)) => redirect(targetUrl, StatusCodes.PermanentRedirect)
      }
    }
  }

  @Path("/")
  @POST
  @Consumes(Array(JMediaType.APPLICATION_JSON))
  @Produces(Array(JMediaType.APPLICATION_JSON))
  @Operation(summary = "Create short name", description = "Create short name",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[TargetURL])))),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "Create short name",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
      new ApiResponse(responseCode = "400", description = "Url not valid"),
      new ApiResponse(responseCode = "409", description = "Cannot create uniq short url"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def create: Route = post {
    entity(as[TargetURL]) { targetURL =>
      utils.HttpUtils.validateUrl(targetURL.url).fold(
        e => complete(StatusCodes.BadRequest, e.getMessage),
        validUrl =>
          onComplete(state.add(validUrl)) {
            case Success(Right(hash)) =>
              complete(StatusCodes.Created, hash)
            case Success(Left(e)) =>
              complete(StatusCodes.Conflict, e.message)
            case Failure(e) =>
              complete(StatusCodes.InternalServerError, e.getMessage)
          }
      )
    }
  }
}

object ShortRoute {
  def apply(state: InMemoryStore)(implicit system: ActorSystem,
                                  storage: Store[String, InMemoryStore, Future],
                                  hashids: Hash[String]): ShortRoute = new ShortRoute(state)
}
