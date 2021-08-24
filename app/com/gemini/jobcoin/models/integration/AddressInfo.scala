package com.gemini.jobcoin.models.integration

import play.api.libs.json.{Format, Json}

case class AddressInfo(
  balance: String,
  transactions: Seq[Transaction]
)

case class Transaction(
  timestamp: String,
  toAddress: String,
  amount: String,
  fromAddress: Option[String] = None
)

object Transaction{
  implicit lazy val format: Format[Transaction] = Json.format[Transaction]
}

object AddressInfo{
  implicit lazy val format: Format[AddressInfo] = Json.format[AddressInfo]
}
