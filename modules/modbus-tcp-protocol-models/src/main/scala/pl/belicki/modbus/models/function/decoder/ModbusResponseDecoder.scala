package pl.belicki.modbus.models.function.decoder

import pl.belicki.modbus.models.ModbusTCPADU.MAX_LENGTH
import pl.belicki.modbus.models.function.eit.EncapsulatedInterfaceTransport
import pl.belicki.modbus.models.function._
import pl.belicki.modbus.models.util.SpacedHex.SpacedHex

import java.nio.ByteBuffer

class ModbusResponseDecoder(
    protected val supportedFunctions: List[ModbusFunction]
) extends ModbusMessageDecoder[String, ModbusFunction#Response] {

  override protected def noFunctionCodeError: String = "The functionCode not found."

  override protected def notSupportedFunctionError(functionCode: Byte): String = f"The functionCode: 0x$functionCode%02X is not supported."

  override protected def decodeMessage(modbusFunction: ModbusFunction, byteBuffer: ByteBuffer): Result = modbusFunction.decodeResponse(byteBuffer)

  override def dataTooLong: String = s"The received data is too long. Maximal number of bytes is: $MAX_LENGTH"

  override def dataTooShort: String = "Too little bytes for decoding the header."

  override def lengthIncorrect: String = "The length in the response doesn't match the actual response."
}

object ModbusResponseDecoder {
  val ERROR_CODE_ADDITION: Byte = 0x80.toByte

  def apply(supportedFunctions: ModbusFunction*) = new ModbusResponseDecoder(supportedFunctions.toList)

  val ALL_FUNCTIONS: ModbusResponseDecoder = apply(
    ReadCoils,
    ReadDiscreteInputs,
    ReadHoldingRegisters,
    ReadInputRegisters,
    WriteSingleCoil,
    WriteSingleRegister,
    WriteMultipleCoils,
    WriteMultipleRegisters,
    ReadFileRecord,
    WriteFileRecord,
    MaskWriteRegister,
    ReadWriteMultipleRegisters,
    ReadFIFOQueue,
    EncapsulatedInterfaceTransport
  )
}
