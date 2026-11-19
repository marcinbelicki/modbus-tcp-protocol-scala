package pl.belicki.modbus.communication.diagnostic

import org.apache.pekko.event.Logging.{MDC, emptyMDC}

import java.net.InetSocketAddress

object MDCs {

  def address(inetSocketAddress: InetSocketAddress): (String, String) =
    "remote" -> s"R:${inetSocketAddress.getHostString}:${inetSocketAddress.getPort}|"}
