package pl.belicki.modbus.models

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.util.EnumUtil

class EnumUtilTest extends AnyWordSpecLike {

  "EnumUtil" must {
    "work properly for ExceptionCode for illegal function" in {
      // when
      val Right(exceptionCode) = EnumUtil.ExceptionCode.getOrElseIllegal(0x01): @unchecked

      // then
      exceptionCode shouldBe ExceptionCode.ILLEGAL_FUNCTION
    }
  }

}
