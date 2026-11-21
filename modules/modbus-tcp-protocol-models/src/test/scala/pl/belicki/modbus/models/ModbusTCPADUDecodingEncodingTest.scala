package pl.belicki.modbus.models

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.ModbusTCPADU.Succeeded
import pl.belicki.modbus.models.function.decoder.ModbusRequestDecoder
import pl.belicki.modbus.models.function.{ReadCoils, ReadDiscreteInputs, ReadHoldingRegisters}
import pl.belicki.modbus.models.util.SpacedHex.{SpacedHex, hexToArray}

import java.nio.ByteBuffer

class ModbusTCPADUDecodingEncodingTest extends AnyWordSpecLike {

  private val hexWithADUs =
    Table(
      ("hex",                                 "expectedADU"),
      ("15 01 00 00 00 06 FF 03 00 04 00 01", ModbusTCPADU(MBAPHeader(0x1501, 0x0000, 0xff.toByte), ReadHoldingRegisters.Request(0x0004, 0x0001))),
    )

  "ModbusTCPADU" must {
    "be properly decoded and encoded for response" in {
      forAll(hexWithADUs) {
        (hex, expectedADU) =>
          val Succeeded(actualADU) = ModbusTCPADU.decode(ModbusRequestDecoder.ALL_FUNCTIONS)(ByteBuffer.wrap(hex)): @unchecked

          actualADU shouldBe expectedADU
      }

    }
  }
}
