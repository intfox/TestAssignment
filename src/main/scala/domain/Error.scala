package domain

import pureconfig.error.ConfigReaderFailures

sealed trait Error extends Throwable

object Error {
  case class ElementWithThatNameExist() extends Throwable("Element with that name already exist.") with Error
  case class ElementWithThatNameNotExist() extends Throwable("Element with that name not exist.") with Error
  case class OutOfPage() extends Throwable("That page not exist.") with Error
  case class ErrorLoadConfig(failures: ConfigReaderFailures) extends Throwable(failures.toList.mkString("Errors config reader: ", ", ", ".")) with Error
}