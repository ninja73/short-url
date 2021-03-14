package url.short

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import url.short.config.Config
import url.short.route.CustomExceptionHandler._
import url.short.route.ShortRoute
import url.short.store.store._

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters._

object Main extends App with LazyLogging {
  implicit val system: ActorSystem = ActorSystem.create("geo-service")
  implicit val materializer: Materializer = Materializer(system)
  implicit val dispatcher: ExecutionContext = system.dispatcher

  val map: concurrent.Map[String, String] = new ConcurrentHashMap[String, String].asScala
  implicit val state: Store[Future] = InMemoryStore(map)

  val router = handleExceptions(exceptionHandler)(ShortRoute().route)
  val init = for {
    bind <- Http().newServerAt(Config.webServer.host, Config.webServer.port).bind(router)
  } yield {
    logger.info(s"Started host: ${Config.webServer.host}, port: ${Config.webServer.port}")
    bind
  }

  Await.ready(system.whenTerminated, Duration.Inf)

  init
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
