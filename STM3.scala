package stm.example

import java.util.concurrent._
import scala.util.Random._

object AccountManager{
  
  def transfer(from: Account, to: Account, amount: Int): Unit = {
    val (lockOne, lockTwo) = if(from.accountNo < to.accountNo) (from, to) else (to, from)
    lockOne.synchronized{
      lockTwo.synchronized{
        if(from.balance > amount){
          to.credit(amount)
          from.debit(amount)
        }
      }
    }
  }
  
}

class Account(val accountNo: String, beginningBalance: Int){
  private var _balance = beginningBalance
  def balance = this.synchronized{ _balance }
  def credit(amount: Int) = this.synchronized{ _balance = _balance + amount }
  def debit(amount: Int) = this.synchronized { _balance = _balance - amount }
}

object STM extends App{
  val account1 = new Account("1", 500)
  val account2 = new Account("2", 500)
  
  val threadPool = Executors.newCachedThreadPool

  def go = {
    for(i <- 0 until (1000)){
      if(nextInt(2) % 2 == 0) AccountManager.transfer(account1, account2, nextInt(10))
      else AccountManager.transfer(account2, account1, nextInt(10))
    }
  }
  
  val futures = for(_ <- 1 to 100) yield threadPool.submit(new Callable[Unit](){ def call = go })
  futures.foreach(_.get)
  
  println("Account1 Balance: " + account1.balance)
  println("Account2 Balance: " + account2.balance)
  println("Total Balance: " + (account1.balance + account2.balance))
  threadPool.shutdown
}