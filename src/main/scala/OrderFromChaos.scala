import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.Predef._
import scala.util.{Success, Failure, Random}

case class Deposit(value: Int)
case object DisplayBalance
case object ResumeMessageProcessing

class BankAccount {
  private var accountBalance = 0.0

  def incrementBalance(deposit:Double) = {
    val previousBalance = accountBalance
    Thread.sleep(Random.nextInt(5) * 50)
    accountBalance = previousBalance + deposit
  }

  def balance = accountBalance
}

// An example of a broken actor where multiple messages being
//    processed simultaneously causes invalid results.

class ChaosActor extends Actor {
  import scala.concurrent.future
  private val bankAccount = new BankAccount()

  def receive = {
    case Deposit(value) =>
      future {
        bankAccount.incrementBalance(value)
      }
    case DisplayBalance => println(s"Final balance is: ${bankAccount.balance}")
  }
}

// An example of an ordered actor where multiple messages being
//    processed in order does not cause invalid results.

class OrderedActor extends Actor with Stash with ActorLogging {
  import scala.concurrent.future
  private val bankAccount = new BankAccount()

  def receive = {
    case Deposit(value) =>
      context.become(waiting, discardOld = false)
      future {
        bankAccount.incrementBalance(value)
      }.onComplete{
        case Failure(e) =>
          log.error(e, "An error has occurred")
          self ! ResumeMessageProcessing
        case Success(v) =>
          self ! ResumeMessageProcessing
      }
    case DisplayBalance => println(s"Final balance is: ${bankAccount.balance}")
  }

  def waiting: Receive = {
    case ResumeMessageProcessing =>
      context.unbecome()
      unstashAll()

    case _ ⇒ stash()
  }
}

object DepositOrderTest extends App {
  override def main(args: Array[String]) {

    val config = ConfigFactory.parseString("""
      akka{
        actor {
          queued-dispatcher {
    	      mailbox-type =”akka.dispatch.UnboundedDequeueBasedMailBox”
          }
        }
      }""")

    implicit val actorSystem = ActorSystem("OrderedActorSystem", ConfigFactory.load(config))

    val actor = {
      if (args.length == 0) {
        println("Using Default: OrderedActor")
        actorSystem.actorOf(Props(new OrderedActor()))
      } else {
        args(0) match {
        case "ChaosActor" =>
          println("Using ChaosActor")
          actorSystem.actorOf(Props(new ChaosActor()))
        case "OrderedActor" =>
          println("Using OrderedActor")
          actorSystem.actorOf(Props(new OrderedActor()))
        case _ =>
          println("Using Default: OrderedActor")
          actorSystem.actorOf(Props(new OrderedActor()))
        }
      }
    }

    (0 until 30).foreach(cnt => actor ! Deposit(50))
    Thread.sleep(10000)
    actor ! DisplayBalance
    actorSystem.shutdown()
  }
}
