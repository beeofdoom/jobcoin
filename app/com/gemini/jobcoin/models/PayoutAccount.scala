package com.gemini.jobcoin.models

case class PayoutAccount(
  depositAccount: String,
  balance: Double,
  accounts: Seq[String]
)
