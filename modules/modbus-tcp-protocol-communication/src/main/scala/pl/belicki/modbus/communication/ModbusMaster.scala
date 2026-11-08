package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, ActorLogging, DiagnosticActorLogging}
import org.apache.pekko.io.Tcp
import org.apache.pekko.io.Tcp.Received

class ModbusMaster extends Actor with DiagnosticActorLogging {
//
//  def apply =
//    Behaviors.withMdc(Map("a" -> "b"))(Behaviors.receive[Tcp.Event] {
//      case (context, Received(data)) =>
//
//    })
//
//  override def preStart(): Unit = {
//    log.clearMDC()
//    super.preStart()
//  }
//
  override def receive: Receive = ???
}
