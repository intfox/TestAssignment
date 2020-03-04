package repository

import cats.effect.Sync
import cats.syntax.all._
import domain._

import scala.collection.concurrent.TrieMap

class ElementsRepository[F[_] : Sync] {
  import Sort._
  private val storage = TrieMap.empty[String, Element]

  def create(element: Element): F[Unit] = Sync[F].delay(
    if(storage.contains(element.name)) Error.ElementWithThatNameExist().asLeft
    else {
      storage += element.name -> element
      ().asRight
    }).rethrow

  def delete(name: String): F[Element] = Sync[F].delay(
    if(!storage.contains(name)) Error.ElementWithThatNameNotExist().asLeft
    else {
      val element = storage(name)
      storage -= name
      element.asRight
    }).rethrow

  def get(name: String): F[Element] =
    Sync[F].delay( storage.get(name).toRight(Error.ElementWithThatNameNotExist()) ).rethrow

  def list(pageSize: Int, pageNumber: Int, order: Option[Sort]): F[List[Element]] = Sync[F].delay(
    if(storage.size >= (pageNumber - 1) * pageSize) (order match {
      case Some(Sort(NAME, order)) => storage.toList.map(_._2).sortBy(_.name)(order match {
        case ASC => implicitly[Ordering[String]]
        case DESC => implicitly[Ordering[String]].reverse
      })
      case Some(Sort(NUMBER, order)) => storage.toList.map(_._2).sortBy(_.number)(order match {
        case ASC => implicitly[Ordering[Int]]
        case DESC => implicitly[Ordering[Int]].reverse
      })
      case None => storage.toList.map(_._2)
    }).slice(pageSize * (pageNumber - 1), pageSize * (pageNumber - 1) + pageSize).asRight
    else Error.OutOfPage().asLeft
  ).rethrow
}

object Sort {
  sealed trait Order
  object DESC extends Order
  object ASC extends Order

  sealed trait Column
  object NAME extends Column
  object NUMBER extends Column
}

case class Sort(column: Sort.Column, order: Sort.Order = Sort.ASC)