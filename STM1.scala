//import java.util.concurrent._
import scala.actors.Actor
import scala.actors.Actor._
import scala.concurrent.stm._

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

package stm.example6{
  
  object AccountManager{

    def transfer(from: Account, to: Account, amount: Int): (Account, Account) = {
      if(from.balance > amount){
        (from.debit(amount), to.credit(amount))
      } else throw new RuntimeException("!")
    }

  }

  case class Account(accountNo: String, balance: Int){
    def credit(amount: Int) = this.copy(balance = (balance + amount))
    def debit(amount: Int) = this.copy(balance = (balance - amount))
  }
  
}

package stm.example7{
  
  object AccountManager{

    val accountsRef = Ref(Map("from" -> Account("from", 500), "to" -> Account("to", 500) ))

    def transfer(fromNo: String, toNo: String, amount: Int): Unit = {
      atomic{ implicit t =>
        var accounts = accountsRef()
        val (from, to) = (accounts(fromNo), accounts(toNo))
        if(from.balance > amount){
          accounts = accounts + (to.accountNo -> to.credit(amount))
          accounts = accounts + (from.accountNo -> from.debit(amount))
          accountsRef() = accounts
        }
      }
    }

  }

  case class Account(accountNo: String, balance: Int){
    def credit(amount: Int) = this.copy(balance = (balance + amount))
    def debit(amount: Int) = this.copy(balance = (balance - amount))
  }
}

package stm.example8{
  
  object AccountManager{

    val accountsRef = Ref(Map("from" -> Account("from", 500), "to" -> Account("to", 500) ))

    def transfer(fromNo: String, toNo: String, amount: Int): Unit = {
      var accounts = atomic{ implicit t => accountsRef() }
      val from = accounts(fromNo)
      val to = accounts(toNo)

      if(from.balance > amount){
        accounts = accounts + (to.accountNo -> to.credit(amount))
        accounts = accounts + (from.accountNo -> from.debit(amount))
        atomic{ implicit t => accountsRef() = accounts }
      }
    }

  }

  case class Account(accountNo: String, balance: Int){
    def credit(amount: Int) = this.copy(balance = (balance + amount))
    def debit(amount: Int) = this.copy(balance = (balance - amount))
  }
}

package stm.example9{
  
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
}