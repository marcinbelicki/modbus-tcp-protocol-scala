package pl.belicki.modbus.communication.util

import org.apache.pekko.util.ByteString
import pl.belicki.modbus.models.util.SpacedHex

object SpacedHexByteString {

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

}
