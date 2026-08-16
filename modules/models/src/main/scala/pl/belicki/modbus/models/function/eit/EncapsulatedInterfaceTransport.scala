package pl.belicki.modbus.models.function.eit

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.function.{ModbusError, ModbusFunction}
import pl.belicki.modbus.models.util.EnumUtil

import java.nio.ByteBuffer

object EncapsulatedInterfaceTransport extends ModbusFunction(0x2b) {

  abstract class SubFunction(_code: Int) {

    val code: Byte = _code.toByte

    abstract class Request extends EncapsulatedInterfaceTransport.Request {
      val subFunction: SubFunction = SubFunction.this

      protected def encodeRest(byteBuffer: ByteBuffer): Either[String, ByteBuffer]

      protected def baseSize: Int

      override lazy val size: Int = java.lang.Byte.BYTES + baseSize

      override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = {
        byteBuffer.put(code)

        for {
          _ <- encodeRest(byteBuffer)
        } yield byteBuffer

      }
    }

    def initialDecodeState: RequestDecodeState

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
    ) extends super.Request {
      override protected def encodeRest(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
        Right {
          byteBuffer.put(deviceIdCode.getCode)
          byteBuffer.put(objectId.getCode)
        }

      override protected lazy val baseSize: Int = java.lang.Byte.BYTES * 2
    }

    private object Initial extends RequestDecodeState {
      override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
        if (byteBuffer.remaining() != 2) return ExceptionCode.ILLEGAL_DATA_VALUE

        for {
          readDeviceIdCode <- ReadDeviceIdCode.getOrElseIllegal(byteBuffer.get())
          objectId = ObjectId(byteBuffer.get())
        } yield RequestFinalState(Request(readDeviceIdCode, objectId))

      }

      override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
    }

    override def initialDecodeState: RequestDecodeState = Initial
  }

  object CANopenGeneralReference extends SubFunction(0x0d) {

    case class Request() extends super.Request {
      override protected def encodeRest(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = Right(byteBuffer)

      override protected lazy val baseSize: Int = 0
    }

    private object Initial extends RequestDecodeState {
      override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = ExceptionCode.SERVER_DEVICE_FAILURE

      override def toReq: Either[ModbusError, Request] = Right(Request())
    }

    override def initialDecodeState: RequestDecodeState = Initial
  }

  type REQ = Request

  private object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < 1) return ExceptionCode.ILLEGAL_DATA_VALUE

      val code = byteBuffer.get()

      SubFunction.subFunctionByCode.get(code) match {
        case Some(subFunction) => Right(subFunction.initialDecodeState)
        case None              => ExceptionCode.ILLEGAL_DATA_VALUE
      }
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  object ReadDeviceIdCode extends EnumUtil[ReadDeviceIdCode, Byte] {
    override protected def getCode(e: ReadDeviceIdCode): Byte = e.getCode

    override protected def viewCode(a: Byte): String = String.format("%02X", a)
  }

  override def initialDecodeState: RequestDecodeState = Initial

  override def validateRequest(request: Request): Either[String, Request] = Right(request)
}
