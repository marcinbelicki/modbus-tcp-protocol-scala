package pl.belicki.modbus.models

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.function._
import pl.belicki.modbus.models.util.SpacedHex.{SpacedHex, hexToArray}

class ResponseDecodingEncodingTest extends AnyWordSpecLike {

  private val hexWithRequests =
    Table(
      ("hex",                     "expectedRequest"),
      ("01 03 CD 6B 05",          ReadCoils.Response("CD 6B 05")),
      ("02 03 AC DB 35",          ReadDiscreteInputs.Response("AC DB 35")),
      ("03 06 02 2B 00 00 00 64", ReadHoldingRegisters.Response("02 2B 00 00 00 64")),
      ("04 02 00 0A",             ReadInputRegisters.Response("00 0A")),
      ("05 00 AC FF 00",          WriteSingleCoil.Response(172, value = true)),
      ("06 00 01 00 03",          WriteSingleRegister.Response(1, 3)),
      ("0F 00 13 00 0A",          WriteMultipleCoils.Response(19, 10)),
      ("10 00 01 00 02",          WriteMultipleRegisters.Response(1, 2)),
      (
        "14 0C 05 06 0D FE 00 20 05 06 33 CD 00 40",
        ReadFileRecord.Response(
          List(
            ReadFileRecord.SubResponse(
              "0D FE 00 20"
            ),
            ReadFileRecord.SubResponse(
              "33 CD 00 40"
            )
          )
        )
      ),
      (
        "15 0D 06 00 04 00 07 00 03 06 AF 04 BE 10 0D",
        WriteFileRecord.Response(
          List(
            WriteFileRecord.SubResponse(4, 7, "06 AF 04 BE 10 0D")
          )
        )
      ),
      (
        "16 00 04 00 F2 00 25",
        MaskWriteRegister.Response(
          0x04,
          0x00f2.toShort,
          0x0025.toShort
        )
      ),
      (
        "17 0C 00 FE 0A CD 00 01 00 03 00 0D 00 FF",
        ReadWriteMultipleRegisters.Response(
          "00 FE 0A CD 00 01 00 03 00 0D 00 FF"
        )
      )
    )

  private val decoder = ModbusResponseDecoder.ALL_FUNCTIONS

  "ModbusResponseDecoder" must {
    "properly decode given hex" in {

      // given
      forAll(hexWithRequests) {
        (hex, expectedResponse) =>
          // when
          val Right(decodedResponse) = decoder.decodeHexResponse(hex): @unchecked

          // then
          decodedResponse shouldBe expectedResponse

          // when
          val Right(byteBuffer) = decodedResponse.toByteBuffer: @unchecked
          // then
          hex.spacedHex shouldBe byteBuffer.array()

          println(
            s"${Console.GREEN}Properly decoded/encoded ${expectedResponse.function.getClass.getSimpleName} request from hex: $hex to $expectedResponse ${Console.RESET}"
          )
      }
    }

  }

}
