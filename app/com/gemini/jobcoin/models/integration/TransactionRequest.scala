package com.gemini.jobcoin.models.integration

import play.api.libs.json.{Format, Json}

case class TransactionRequest(
  fromAddress: String,
  toAddress: String,
  amount: String
)

object TransactionRequest{
  implicit lazy val format: Format[TransactionRequest] = Json.format[TransactionRequest]
}
