/*package stm.example

import scala.concurrent.stm._
import java.util.concurrent.{Executors, Callable}

object AccountManager{
  
  val accountsRef = Ref(Map("from" -> new Account("from", 500), "to" -> new Account("to", 500) ))
  
  def transfer(fromNo: String, toNo: String, amount: Int): Unit = {
    val accounts = atomic{ implicit t => 
      var accounts = accountsRef() 
      val from = accounts(fromNo)
      val to = accounts(toNo)
      if(from.balance > amount){
        from.debit(amount)
        to.credit(amount)
        accounts = accounts + (from.accountNo -> from)
        accounts = accounts + (to.accountNo -> to)
      }
      accountsRef() = accounts
    }
  }
  
}

class Account(val accountNo: String, beginningBalance: Int){
  private var _balance = beginningBalance
  def balance = this.synchronized{ _balance }
  def credit(amount: Int) = this.synchronized{ _balance = _balance + amount }
  def debit(amount: Int) = this.synchronized { _balance = _balance - amount }
  override def toString = "Account(%s,%s)".format(accountNo, _balance)
}

object STM extends App{
  import scala.util.Random._
  val threadPool = Executors.newFixedThreadPool(2)

  def go = {
    for(i <- 0 until (10000000)){
      if(nextInt(2) % 2 == 0) AccountManager.transfer("to", "from", 10)
      else AccountManager.transfer("from", "to", 10)
    }
  }
  
  val future1 = threadPool.submit(new Callable[Unit](){ def call = go })
  val future2 = threadPool.submit(new Callable[Unit](){ def call = go})

  future1.get
  future2.get
  
  println("Balance: " + atomic{implicit t => AccountManager.accountsRef()})
  threadPool.shutdown
}*/