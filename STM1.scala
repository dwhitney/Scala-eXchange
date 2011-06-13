import java.util.concurrent._
import scala.actors.Actor
import scala.actors.Actor._

package stm.example1{
  
  class Account(val accountNo: String, var balance: Int){
    def credit(amount: Int) = balance = balance + amount
    def debit(amount: Int) = balance = balance - amount
  }

}

package stm.example2{
  
  class Account(val accountNo: String, beginningBalance: Int){
    private var _balance = beginningBalance
    def balance = this.synchronized{ _balance }
    def credit(amount: Int) = this.synchronized{ _balance = _balance + amount }
    def debit(amount: Int) = this.synchronized { _balance = _balance - amount }
  }

}

package stm.example3{
  
  object AccountManager{

    def transfer(from: Account, to: Account, amount: Int): Unit = {
      if(from.balance > amount){
        to.credit(amount)
        from.debit(amount)
      }else throw new RuntimeException("Balance too low!")
    }

  }
  
  class Account(val accountNo: String, beginningBalance: Int){
    private var _balance = beginningBalance
    def balance = this.synchronized{ _balance }
    def credit(amount: Int) = this.synchronized{ _balance = _balance + amount }
    def debit(amount: Int) = this.synchronized { _balance = _balance - amount }
  }
  
  
}

package stm.example4{
  
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
  
  
}

package stm.example5{
  
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
          case Balance => reply(_balance)
        }
      }
    }
  }
  
}