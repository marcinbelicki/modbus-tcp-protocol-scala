package pl.belicki.modbus.models

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.function.{MaskWriteRegister, ModbusRequestDecoder, ReadCoils, ReadDiscreteInputs, ReadFileRecord, ReadHoldingRegisters, ReadInputRegisters, WriteFileRecord, WriteMultipleCoils, WriteMultipleRegisters, WriteSingleCoil, WriteSingleRegister}
import pl.belicki.modbus.models.util.SpacedHex

class DecodingEncodingTest extends AnyWordSpecLike {

  private val hexWithRequests =
    Table(
      ("hex",                                             "expectedRequest"),
      ("01 00 13 00 13",                                  ReadCoils.Request(19, 19)),
      ("02 00 C4 00 16",                                  ReadDiscreteInputs.Request(196, 22)),
      ("03 00 6B 00 03",                                  ReadHoldingRegisters.Request(107, 3)),
      ("04 00 08 00 01",                                  ReadInputRegisters.Request(8, 1)),
      ("05 00 AC FF 00",                                  WriteSingleCoil.Request(172, value = true)),
      ("06 00 01 00 03",                                  WriteSingleRegister.Request(1, 3)),
      ("0F 00 13 00 0A 02 CD 01",                         WriteMultipleCoils.Request(19, 10, SpacedHex.parseHex("CD 01"))),
      ("10 00 01 00 02 04 00 0A 01 02",                   WriteMultipleRegisters.Request(1, SpacedHex.parseHex("00 0A 01 02"))),
      ("14 0E 06 00 04 00 01 00 02 06 00 03 00 09 00 02", ReadFileRecord.Request(List(ReadFileRecord.SubRequest(4, 1, 2), ReadFileRecord.SubRequest(3, 9, 2)))),
      ("15 0D 06 00 04 00 07 00 03 06 AF 04 BE 10 0D", WriteFileRecord.Request(List(WriteFileRecord.SubRequest(4, 1, Array.empty), WriteFileRecord.SubRequest(3, 9, Array.empty)))),
    )

  private val decoder = ModbusRequestDecoder.ALL_FUNCTIONS

  "ModbusRequestDecoder" must {
    "properly decode given hex" in {

      // given
      forAll(hexWithRequests) {
        (hex, expectedRequest) =>
          // when
          val Right(decodedRequest) = decoder.decodeHexRequest(hex): @unchecked

          // then
          decodedRequest shouldBe expectedRequest

          // when
          val Right(byteBuffer) = decodedRequest.toByteBuffer: @unchecked
          // then
          SpacedHex.parseHex(hex) shouldBe byteBuffer.array()

          println(
            s"${Console.GREEN}Properly decoded/encoded ${expectedRequest.function.getClass.getSimpleName} request from hex: $hex to $expectedRequest ${Console.RESET}"
          )
      }
    }

  }

}
