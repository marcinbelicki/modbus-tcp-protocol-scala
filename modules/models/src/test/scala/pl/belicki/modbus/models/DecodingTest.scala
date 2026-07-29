package pl.belicki.modbus.models

import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.function.ReadCoils

import java.nio.ByteBuffer
import java.util.HexFormat

class DecodingTest extends AnyWordSpecLike {

  "ReadCoils" must {
    "be properly decoded and encoded" in {
      val Right(request) = ReadCoils.decodeRequest(ByteBuffer.wrap(HexFormat.of().parseHex("00FF0001"))): @unchecked

      request shouldBe ReadCoils.Request(255, 1)

      val Right(byteBuffer) = request.toByteBuffer: @unchecked

      byteBuffer.array() shouldBe HexFormat.of().parseHex("0100FF0001")
    }
  }

}
