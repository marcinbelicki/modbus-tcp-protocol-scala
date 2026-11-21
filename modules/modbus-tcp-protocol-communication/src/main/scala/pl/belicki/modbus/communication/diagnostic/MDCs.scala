package pl.belicki.modbus.communication.diagnostic

import org.apache.pekko.event.Logging.{MDC, emptyMDC}

import java.net.InetSocketAddress

object MDCs {

  def remote(remoteAddress: InetSocketAddress): (String, String) =
    "remote" -> s"R:${remoteAddress.getHostString}:${remoteAddress.getPort}|"
}
