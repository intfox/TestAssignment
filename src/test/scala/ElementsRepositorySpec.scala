import cats.effect.IO
import domain.Element
import org.scalatest.funsuite.AnyFunSuite
import repository.{ElementsRepository, Sort}

class ElementsRepositorySpec extends AnyFunSuite {
  test("Test create and get") {
    val repository = new ElementsRepository[IO]
    (for {
      _ <- repository.create(Element("Element1", 0))
      getElement <- repository.get("Element1")
    } yield assert(getElement == Element("Element1", 0))).unsafeRunSync()
  }

  test("Test pagination.") {
    val repository = new ElementsRepository[IO]

    val sortedElementsByName = List(Element("A", 123), Element("Bad", 44), Element("Bdf", 45), Element("FQE", 44))

    (for {
      _ <- repository.create(sortedElementsByName(2))
      _ <- repository.create(sortedElementsByName(0))
      _ <- repository.create(sortedElementsByName(1))
      _ <- repository.create(sortedElementsByName(3))
      result <- repository.list(10, 1, Some(Sort(Sort.NAME, Sort.ASC)))
      result2Elements <- repository.list(2, 1, None)
      reslut2Elements2page <- repository.list(2, 2, None)
      resultSortDESC <- repository.list(4, 1, Some(Sort(Sort.NAME, Sort.DESC)))
    } yield assert(result == sortedElementsByName && result2Elements.size == 2 && resultSortDESC == sortedElementsByName.reverse && reslut2Elements2page.size == 2)).unsafeRunSync()
  }
}
