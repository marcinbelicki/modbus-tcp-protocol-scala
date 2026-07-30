package pl.belicki.modbus.models

import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.function.ReadCoils
import pl.belicki.modbus.models.util.SpacedHex

import java.nio.ByteBuffer
import java.util.HexFormat

class DecodingTest extends AnyWordSpecLike {

  "ReadCoils" must {
    "be properly decoded and encoded" in {
      val Right(request) = ReadCoils.decodeHexRequest("00 FF 00 01"): @unchecked

      request shouldBe ReadCoils.Request(255, 1)

      val Right(byteBuffer) = request.toByteBuffer: @unchecked

      byteBuffer.array() shouldBe SpacedHex.parseHex("01 00 FF 00 01")
    }

    "be properly decoded and encoded for example from documentation" in {
      val Right(request) = ReadCoils.decodeHexRequest("00 13 00 13")
      request shouldBe ReadCoils.Request(19, 19)
    }
  }

}
