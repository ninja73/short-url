package url.short.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import url.short.config.Config.WebServerConfig
import url.short.route.ShortRoute

final class SwaggerDoc(conf: WebServerConfig) extends SwaggerHttpService {
  override val apiClasses = Set(classOf[ShortRoute])
  override val host = s"${if (conf.host != "0.0.0.0") conf.host else "localhost"}:${conf.port}"
  override val info: Info = Info(version = "1.0")
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}