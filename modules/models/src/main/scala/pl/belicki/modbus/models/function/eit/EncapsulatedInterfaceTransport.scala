package pl.belicki.modbus.models.function.eit

import pl.belicki.modbus.models.{EnumUtil, ExceptionCode}
import pl.belicki.modbus.models.function.ModbusFunction

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

    case class Request(
        deviceIdCode: ReadDeviceIdCode,
        objectId: ObjectId
    ) extends super.Request

    private object Initial extends DecodeState {
      override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
        if (byteBuffer.remaining() != 2) return ExceptionCode.ILLEGAL_DATA_VALUE

        for {
          readDeviceIdCode <- ReadDeviceIdCode.getOrElseIllegal(byteBuffer.get())
          objectId = ObjectId(byteBuffer.get())
        } yield FinalState(Request(readDeviceIdCode, objectId))

      }

      override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
    }

    private case class FinalState(request: Request) extends DecodeState {
      override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = ExceptionCode.ILLEGAL_DATA_VALUE

      override def toReq: Either[Error, Request] = Right(request)
    }

    override def initialDecodeState: DecodeState = Initial
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
        case Some(subFunction) => Right(subFunction.initialDecodeState)
        case None              => ExceptionCode.ILLEGAL_DATA_VALUE
      }
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  object ReadDeviceIdCode extends EnumUtil[ReadDeviceIdCode, Byte] {
    override protected def getCode(e: ReadDeviceIdCode): Byte = e.getCode

    override protected def viewCode(a: Byte): String = String.format("%02X", a)
  }

  override def initialDecodeState: DecodeState = Initial

}
