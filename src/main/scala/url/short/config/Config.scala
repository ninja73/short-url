package url.short.config

import com.typesafe.config.ConfigFactory

object Config {

  case class WebServerConfig(host: String, port: Int)

  private val config = ConfigFactory.load()

  private val host = config.getString("web-server.host")
  private val port = config.getInt("web-server.port")

  val webServer: WebServerConfig = WebServerConfig(host, port)
}
