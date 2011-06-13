package stm.example44

import java.util.concurrent._
import scala.actors.Actor
import scala.actors.Actor._

object AccountManager{
  
  def transfer(from: Account, to: Account, amount: Int): Unit = {
    val balance = (from !? Balance).asInstanceOf[Int]
    if(balance > amount){
      to ! Credit(amount)
      from ! Debit(amount)
    }
  }
  
}

case class Credit(amount: Int)
case class Debit(amount: Int)
case object Balance

class Account(val accountNo: String, beginningBalance: Int) extends Actor{
  private var _balance = beginningBalance
  
  def act = {
    loop{
      receive{
        case Credit(amount) => _balance = _balance + amount
        case Debit(amount) => _balance = _balance - amount
        case Balance => Thread.sleep(1); reply(_balance)
      }
    }
  }
}

object STM extends App{
  import scala.util.Random._
  val account1 = new Account("1", 500)
  val account2 = new Account("2", 500)
  account1.start
  account2.start
  
  val threadPool = Executors.newFixedThreadPool(2)

  def go = {
    for(i <- 0 until (1000)){
      if(nextInt(2) % 2 == 0) AccountManager.transfer(account1, account2, 10)
      else AccountManager.transfer(account2, account1, 110)
    }
  }
  
  val future1 = threadPool.submit(new Callable[Unit](){ def call = go })
  val future2 = threadPool.submit(new Callable[Unit](){ def call = go})

  future1.get
  future2.get
  
  println("Balance: " + (account1 !? Balance))
  
  threadPool.shutdown
}
