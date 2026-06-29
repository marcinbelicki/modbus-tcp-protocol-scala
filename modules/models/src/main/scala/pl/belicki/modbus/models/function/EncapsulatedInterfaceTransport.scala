package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.function.EncapsulatedInterfaceTransport.CANopenGeneralReference.Request

import java.nio.ByteBuffer

object EncapsulatedInterfaceTransport extends ModbusFunction(0x2b) {

  abstract class SubFunction(_code: Int) {

    val code: Byte = _code.toByte

    abstract class Request extends EncapsulatedInterfaceTransport.Request {
      val subFunction: SubFunction = SubFunction.this
    }

    def initialDecodeState: DecodeState

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
        case (byte, subFunctions)       => throw new IllegalStateException(f"Too many subFunctions: $subFunctions for code: 0x$byte%02X")
      }

  }

  object ReadDeviceIdentification extends SubFunction(0x0e) {

    abstract class

    case class Request(
        deviceIdCode: Byte,
        objectId: Short
    ) extends super.Request

    private object Initial extends DecodeState {
      override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
        val byte
      }

      override def toReq: Either[Error, Request] = Right(Request())
    }

    override def initialDecodeState: DecodeState = ???
  }

  object CANopenGeneralReference extends SubFunction(0x0d) {

    case class Request() extends super.Request

    private object Initial extends DecodeState {
      override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = ExceptionCode.SERVER_DEVICE_FAILURE

      override def toReq: Either[Error, Request] = Right(Request())
    }

    override def initialDecodeState: DecodeState = Initial
  }

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 1) return ExceptionCode.ILLEGAL_DATA_VALUE

      val code = byteBuffer.get()

      SubFunction.subFunctionByCode.get(code) match {
        case Some(subFunction) =>
        case
      }
    }

    override def toReq: Either[Error, Request] = ???
  }

}
