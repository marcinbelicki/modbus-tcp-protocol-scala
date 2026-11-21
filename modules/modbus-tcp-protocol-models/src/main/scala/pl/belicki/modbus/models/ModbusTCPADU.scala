package pl.belicki.modbus.models

import pl.belicki.modbus.models.function.{Message, ModbusFunction}
import pl.belicki.modbus.models.function.decoder.ModbusMessageDecoder

import java.nio.ByteBuffer

case class ModbusTCPADU[M <: Message](
    header: MBAPHeader,
    message: M
) {

  private lazy val size = MBAPHeader.SIZE + message.fullSize

  def toByteBuffer: Either[String, ByteBuffer] = {
    val byteBuffer = ByteBuffer.allocate(size)
    val length     = message.fullSize - java.lang.Byte.BYTES
    for {
      _ <- MBAPHeader.LengthValidator.validate(length)
      _ = header.encode(length.toShort, byteBuffer)
      bufferFromMessage <- message.toByteBuffer
    } yield byteBuffer.put(bufferFromMessage)
  }
}

object ModbusTCPADU {
  val MAX_LENGTH: Int = 260

  trait DecodingResponse[+E, +M <: ModbusFunction#FunctionMessage]

  case class DecodingFailure[E](e: E) extends DecodingResponse[E, Nothing]
  case class DecodingFailureWithHeader[E](header: MBAPHeader, e: E) extends DecodingResponse[E, Nothing]
  case class Succeeded[M <: ModbusFunction#FunctionMessage](modbusTCPADU: ModbusTCPADU[M]) extends DecodingResponse[Nothing, M]

  def decode[E, M <: ModbusFunction#FunctionMessage](decoder: ModbusMessageDecoder[E, M])(data: ByteBuffer): DecodingResponse[E, M] = {
    if (data.remaining() > MAX_LENGTH) return DecodingFailure(decoder.dataTooLong)
    MBAPHeader.decode(decoder)(data) match {
      case Left(error)   => DecodingFailure(error)
      case Right(header) =>
        decoder.decode(data) match {
          case Left(error)  => DecodingFailureWithHeader(header, error)
          case Right(value) => Succeeded(ModbusTCPADU(header, value))
        }
    }
  }
}
