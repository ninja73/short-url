package url.short.store

import url.short._

import scala.collection.concurrent.Map
import scala.concurrent.{ExecutionContext, Future}

object store {
  type Id = String
  type TargetURL = String

  trait Store[F[_]] {
    def get(id: Id): F[Option[TargetURL]]

    def add(id: Id, targetUrl: TargetURL): F[Either[AppError, Id]]
  }

  object Store {
    def apply[F[_]](implicit DS: Store[F]): Store[F] = DS
  }

  final case class InMemoryStore(data: Map[Id, TargetURL])

  implicit class StoreFutureOps(state: InMemoryStore)(implicit ec: ExecutionContext) extends Store[Future] {
    override def get(hash: Id): Future[Option[TargetURL]] = {
      Future(state.data.get(hash))
    }

    override def add(hash: Id, targetUrl: TargetURL): Future[Either[AppError, Id]] = {
      Future(state.data.putIfAbsent(hash, targetUrl)).map {
        case Some(v) if v != targetUrl => Left(ConflictUrl(v))
        case _ => Right(hash)
      }
    }
  }

}

