package pl.belicki.modbus.models.util

import java.util.HexFormat
import scala.language.implicitConversions

object SpacedHex {

  private val format = HexFormat.ofDelimiter(" ").withUpperCase()

  def parseHex(hex: String): Array[Byte] = format.parseHex(hex)
  def apply(hex: String): Array[Byte]    = parseHex(hex)

  def stringFromArray(bytes: Array[Byte]): String = format.formatHex(bytes)

  implicit class SpacedHex(hex: String) {
    def spacedHex: Array[Byte] = parseHex(hex)
  }

  implicit def hexToArray(hex: String): Array[Byte] = parseHex(hex)

}
