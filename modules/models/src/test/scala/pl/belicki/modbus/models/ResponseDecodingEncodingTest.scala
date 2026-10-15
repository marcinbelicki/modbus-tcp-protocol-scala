package pl.belicki.modbus.models

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.function.eit.{DefinedObjectId, EncapsulatedInterfaceTransport, ReadDeviceIdCode}
import pl.belicki.modbus.models.function._
import pl.belicki.modbus.models.util.SpacedHex

class ResponseDecodingEncodingTest extends AnyWordSpecLike {

  private val hexWithRequests =
    Table(
      ("hex",            "expectedRequest"),
      ("01 03 CD 6B 05", ReadCoils.Response(SpacedHex.parseHex("CD 6B 05"))),
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
          SpacedHex(hex) shouldBe byteBuffer.array()

          println(
            s"${Console.GREEN}Properly decoded/encoded ${expectedResponse.function.getClass.getSimpleName} request from hex: $hex to $expectedResponse ${Console.RESET}"
          )
      }
    }

  }

}
