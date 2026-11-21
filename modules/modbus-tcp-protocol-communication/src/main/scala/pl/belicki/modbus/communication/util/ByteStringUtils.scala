package pl.belicki.modbus.communication.util

import org.apache.pekko.actor.{DiagnosticActorLogging, PoisonPill}
import org.apache.pekko.event.LoggingAdapter
import org.apache.pekko.io.Tcp
import org.apache.pekko.io.Tcp.{ErrorClosed, Write}
import org.apache.pekko.util.ByteString
import pl.belicki.modbus.models.ModbusTCPADU
import pl.belicki.modbus.models.function.Message
import pl.belicki.modbus.models.util.SpacedHex

import scala.language.implicitConversions

object ByteStringUtils {

  def formatByteString(byteString: ByteString): String = {
    val byteBuffer = byteString.asByteBuffer
    val remaining  = new Array[Byte](byteBuffer.remaining())
    byteBuffer.get(remaining)
    SpacedHex.stringFromArray(remaining)
  }

  implicit class ByteStringExt(byteString: ByteString) {
    def spacedHex: String =
      formatByteString(byteString)
  }

  implicit class ByteStringHelper(val sc: StringContext) extends AnyVal {
    def b(args: Any*): String = {
      val argsMapped = args.map {
        case byteString: ByteString => formatByteString(byteString)
        case other                  => other
      }
      sc.s(argsMapped: _*)
    }
  }

  trait MessageToByteString {
    this: DiagnosticActorLogging =>

    object WriteOrClose {
      def apply(message: ModbusTCPADU[_]): Any =
        message.toByteBuffer match {
          case Right(byteBuffer) =>
            Write(ByteString.fromByteBuffer(byteBuffer))
          case Left(error) =>
            log.error(s"There was a problem while trying to encode the message: $error (message: $message)")
            self ! PoisonPill
            PoisonPill
        }

    }

  }

}
