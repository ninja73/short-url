package url.short.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import url.short.store.store.Store
import url.short.utils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.implicitConversions
import scala.util.hashing.MurmurHash3
import scala.util.{Failure, Success}

final case class TargetURL(url: String)

class ShortRoute()(implicit actorSystem: ActorSystem, storage: Store[Future])
  extends Directives with LazyLogging {

  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)

  val route: Route =
    get {
      extractUnmatchedPath { shortUrl =>
        utils.hashUrlDecoder(shortUrl.dropChars(1).toString).map(id => {
          onComplete(storage.get(id)) {
            case Failure(e) => complete(StatusCodes.InternalServerError, e.getMessage)
            case Success(None) => complete(StatusCodes.NotFound, s"target url not found for $shortUrl")
            case Success(Some(targetUrl)) => redirect(targetUrl, StatusCodes.PermanentRedirect)
          }
        }).getOrElse(complete(StatusCodes.BadRequest, s"failed decode short $shortUrl"))
      }
    } ~
      post {
        entity(as[TargetURL]) { targetURL =>
          utils.HttpUtils.validateUrl(targetURL.url).fold(
            e => complete(StatusCodes.BadRequest, e.getMessage),
            validUrl =>
              onComplete(storage.add(MurmurHash3.stringHash(validUrl).toHexString, validUrl)) {
                case Success(Right(hash)) =>
                  complete(StatusCodes.Created, utils.hashUrlEncoder(hash))
                case Success(Left(e)) =>
                  complete(StatusCodes.BadRequest, e.message)
                case Failure(e) =>
                  complete(StatusCodes.InternalServerError, e.getMessage)
              }
          )
        }
      }
}

object ShortRoute {
  def apply()(implicit system: ActorSystem, storage: Store[Future]): ShortRoute = new ShortRoute()
}
