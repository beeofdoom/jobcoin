package com.gemini.jobcoin.models.integration

import play.api.libs.json.{Format, Json}

case class TransactionResponse(
  status: String
)

case class TransactionError(
  error: String
)

object TransactionResponse{
  implicit lazy val format: Format[TransactionResponse] = Json.format[TransactionResponse]
}

object TransactionError{
  implicit lazy val format: Format[TransactionError] = Json.format[TransactionError]
}