package pl.belicki.modbus.communication.msg

import org.apache.pekko.actor.Actor
import pl.belicki.modbus.communication.diagnostic.MDCs

import java.net.InetSocketAddress

case class WithRemote[A](remoteAddress: InetSocketAddress, message: A)

object WithRemote {
  def apply(receive: Actor.Receive): Actor.Receive = {
    case WithRemote(_, receive(unit)) => unit
  }

  def mdc(message: Any): Map[String, String] = message match {
    case WithRemote(remoteAddress: InetSocketAddress, _) => Map(MDCs.remote(remoteAddress))
    case _                                               => Map.empty
  }
}
