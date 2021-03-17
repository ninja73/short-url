package url.short.store

import java.util.concurrent.atomic.AtomicLong

import url.short._
import url.short.hash.Hash

import scala.collection.concurrent.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.hashing.MurmurHash3

object store {

  trait Store[T, S, F[_]] {
    def get(state: S, id: String)(implicit hash: Hash[T]): F[Option[String]]

    def add(state: S, targetUrl: String)(implicit hash: Hash[T]): F[Either[AppError, String]]
  }

  object Store {
    def apply[T, S, F[_]](implicit DS: Store[T, S, F]): Store[T, S, F] = DS
  }

  implicit class StoreOps[T, S, F[_]](s: S) {
    def get(id: String)(implicit DS: Store[T, S, F], hash: Hash[T]): F[Option[String]] = DS.get(s, id)

    def add(targetUrl: String)(implicit DS: Store[T, S, F], hash: Hash[T]): F[Either[AppError, String]] = DS.add(s, targetUrl)
  }

  final case class InMemoryStore(data: Map[String, String])

  implicit val StoreFutureOps: Store[String, InMemoryStore, Future] = new Store[String, InMemoryStore, Future] {
    override def get(state: InMemoryStore, id: String)(implicit hash: Hash[String]): Future[Option[String]] = {
      Future(state.data.get(id))
    }

    override def add(state: InMemoryStore, targetUrl: String)(implicit hash: Hash[String]): Future[Either[AppError, String]] = {
      Future(hash.hashUrlEncoder(MurmurHash3.stringHash(targetUrl).toHexString))
        .map(id =>
          state.data.putIfAbsent(id, targetUrl) match {
            case Some(v) if v != targetUrl => Left(ConflictUrl(v))
            case _ => Right(id)
          }
        )
    }
  }

  final case class InMemoryExactStore(ids: AtomicLong, data: Map[String, String])

  implicit val StoreExactFutureOps: Store[Long, InMemoryExactStore, Future] = new Store[Long, InMemoryExactStore, Future] {
    override def get(state: InMemoryExactStore, id: String)(implicit hash: Hash[Long]): Future[Option[String]] = {
      Future(state.data.get(id))
    }

    override def add(state: InMemoryExactStore, targetUrl: String)(implicit hash: Hash[Long]): Future[Either[AppError, String]] = {
      Future(hash.hashUrlEncoder(state.ids.getAndIncrement()))
        .map(id =>
          state.data.synchronized {
            state.data.find { case (_, v) => v == targetUrl } match {
              case Some((k, _)) => Right(k)
              case None =>
                state.data.put(id, targetUrl).map(k => Right(k)).getOrElse(Left(Unreachable))
            }
          }
        )
    }
  }
}

