package repository

import cats.effect.Sync
import cats.syntax.all._

import domain._

import scala.collection.concurrent.TrieMap

class ElementsRepository[F[_] : Sync] {
  private val storage = TrieMap.empty[String, Element]

  def create(element: Element): F[Unit] =
    if(storage.contains(element.name)) Error.ElementWithThatNameExist().raiseError[F, Unit] else Sync[F].delay( storage += element.name -> element )

  def delete(name: String): F[Element] =
    if(!storage.contains(name)) Error.ElementWithThatNameNotExist().raiseError[F, Element]
    else Sync[F].delay{
      val element = storage(name)
      storage -= name
      element
    }

  def get(name: String): F[Element] =
    Sync[F].delay( storage.get(name).toRight(Error.ElementWithThatNameNotExist()) ).rethrow

  def list(pageSize: Int, offset: Int, order: Option[Sort]): F[List[Element]] = ???
}

object Sort {
  sealed trait Order
  object DESC extends Order
  object ASC extends Order

  sealed trait Column
  object NAME extends Column
  object NUMBER extends Column
}

case class Sort(order: Sort.Order, column: Sort.Column)