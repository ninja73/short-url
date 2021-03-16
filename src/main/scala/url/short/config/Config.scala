package url.short.config

import com.typesafe.config.ConfigFactory

object Config {

  case class HashidsConfig(salt: String, alphabet: String, size: Int)

  case class WebServerConfig(host: String, port: Int, hashidsConfig: HashidsConfig)

  private val config = ConfigFactory.load()

  private val host = config.getString("web-server.host")
  private val port = config.getInt("web-server.port")

  private val salt = config.getString("hashids.salt")
  private val alphabet = config.getString("hashids.alphabet")
  private val size = config.getInt("hashids.size")

  private val hashidsConfig: HashidsConfig = HashidsConfig(salt, alphabet, size)

  val webServer: WebServerConfig = WebServerConfig(host, port, hashidsConfig)
}
