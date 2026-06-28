package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object EncapsulatedInterfaceTransport extends ModbusFunction(0x2b) {

  abstract class SubFunction(_code: Int) {

    val code: Byte = _code.toByte

    abstract class Request extends EncapsulatedInterfaceTransport.Request {
      val subFunction: SubFunction = SubFunction.this
    }

  }

  object SubFunction {
    private val subFunctions = List(
      ReadDeviceIdentification,
      CANopenGeneralReference
    )

    val subFunctionByCode: Map[Byte, SubFunction] = subFunctions
      .groupBy(_.code)
      .map {
        case (byte, subFunction :: Nil) => (byte, subFunction)
        case (byte, subFunctions)       => throw new IllegalStateException(f"Too many subFunctions: $subFunctions for code code: 0x$byte%02X")
      }

  }

  object ReadDeviceIdentification extends SubFunction(0x0e) {

    case class Request(
        deviceIdCode: Byte,
        objectId: Short
    ) extends super.Request

  }

  object CANopenGeneralReference extends SubFunction(0x0d) {

    case class Request() extends super.Request

  }

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 1) return ExceptionCode.ILLEGAL_DATA_VALUE

      val code =
    }

    override def toReq: Either[Error, Request] = ???
  }

}
