package pl.belicki.modbus.models

import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.function.ReadCoils

import java.nio.ByteBuffer
import java.util.HexFormat

class DecodingTest extends AnyWordSpecLike {

  "ReadCoils" must {
    "be properly decoded" in {
      val request = ReadCoils.decodeRequest(ByteBuffer.wrap(HexFormat.of().parseHex("00FF0001")))

      request shouldBe Right(ReadCoils.Request(255, 1))
    }
  }

}
