package pl.belicki.modbus.models.util

import java.util.HexFormat

object SpacedHex {

  private val format = HexFormat.ofDelimiter(" ")

  def parseHex(hex: String): Array[Byte] = format.parseHex(hex)

}
