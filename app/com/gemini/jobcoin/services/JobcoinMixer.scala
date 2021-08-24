package com.gemini.jobcoin.services

import com.gemini.jobcoin.clients.JobcoinClient
import com.gemini.jobcoin.models.PayoutAccount
import com.gemini.jobcoin.models.integration.{Transaction, TransactionError, TransactionRequest, TransactionResponse}
import org.joda.time.DateTime

import javax.inject.{Inject, Singleton}
import java.util.UUID.randomUUID
import scala.concurrent.{ExecutionContext, Future}

import scala.collection.mutable.ListBuffer
import scala.math.Ordering.Implicits.infixOrderingOps



@Singleton
class JobcoinMixer @Inject() (client: JobcoinClient)(implicit ec: ExecutionContext) {
  private var ownedAddresses: Map[String, Seq[String]] = Map.empty
  private var toDeposit: ListBuffer[PayoutAccount] = ListBuffer.empty
  private var houseAccount: String = "houseAccount"
  private var timeLastChecked: DateTime = DateTime.now().minusDays(1)

  def addAddress(withdrawlAddresses: Seq[String]): String = {
    val depositAddress = randomUUID().toString  //TODO call address endpoint and confirm address available
    //val depositAddress = "testDeposit"
    ownedAddresses += (depositAddress -> withdrawlAddresses)
    depositAddress
  }

  def getNewTransactions(): Future[Seq[Transaction]] = {
    client.getTransactions().map{
      transactions =>
        val x = DateTime.parse(transactions.head.timestamp)
        val newTransactions = transactions.filter( x => DateTime.parse(x.timestamp) >= timeLastChecked)
        timeLastChecked = DateTime.now()  //TODO this should be stored in a table or other non-volatile storage
        newTransactions
    }
  }

  def transferToHouse(transactions: Seq[Transaction]) = {
    val ownedTransactions = transactions.filter(x => ownedAddresses.isDefinedAt(x.toAddress))
    Future.sequence(ownedTransactions.map{
      transaction =>
        val req: TransactionRequest = TransactionRequest(transaction.toAddress, houseAccount, transaction.amount)
        client.postTransaction(req).map{
          case Right(rsp: TransactionResponse) =>
            val toPay: PayoutAccount = PayoutAccount(transaction.toAddress, transaction.amount.toFloat, ownedAddresses(transaction.toAddress))
            toDeposit += toPay
          case Left(err: TransactionError)  =>
            print(s"err: ${err.toString}")
        }
    })
  }

  def transferToCustomers() = {
    val payoutPercerntage:Double = .25
    val minimumPayout:Double = 1.00
    val transactionPercentage:Double = .01
    toDeposit.filter(account => account.balance >1.0).map{
      account: PayoutAccount =>
        val minimumPayment = if (account.balance * payoutPercerntage > minimumPayout)  account.balance * payoutPercerntage
          else minimumPayout
        val transactionFee = minimumPayment * transactionPercentage
        val amountToPay = (minimumPayout - transactionFee) / account.accounts.length
        val newBalance = account.balance - amountToPay - transactionFee
        account.accounts.map{
          address =>
            val req = TransactionRequest(houseAccount, address, amountToPay.toString)
            client.postTransaction(req)  //TODO we should actually check that the request succeeds before reducing the balance
        }
      account.copy(balance = newBalance)
    }
  }

  def mixTransactions() ={
    for {
      newTransactions:Seq[Transaction] <- getNewTransactions
      _ <-  transferToHouse(newTransactions)
    } yield transferToCustomers()
  }

}
