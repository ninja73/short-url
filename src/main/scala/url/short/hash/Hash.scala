package url.short.hash

import org.hashids.Hashids

trait Hash[K] {
  def hashUrlDecoder(short: String): Option[K]

  def hashUrlEncoder(in: K): String
}


class HashIds(h: Hashids) extends Hash[String] {
  override def hashUrlDecoder(short: String): Option[String] = {
    val result = h.decodeHex(short)
    Option(result)
  }

  override def hashUrlEncoder(in: String): String = h.encodeHex(in)
}


class HashIdsLong(h: Hashids) extends Hash[Long] {
  override def hashUrlDecoder(short: String): Option[Long] = h.decode(short).headOption

  override def hashUrlEncoder(in: Long): String = h.encode(in)
}