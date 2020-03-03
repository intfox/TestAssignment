package domain

import io.circe.generic.JsonCodec

@JsonCodec case class Element(name: String, number: Int)
