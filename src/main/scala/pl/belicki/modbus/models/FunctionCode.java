package pl.belicki.modbus.models;

public enum FunctionCode {
    ReadCoils,
    ReadDiscreteInputs,
    ReadHoldingRegisters,
    ReadInputRegisters,
    WriteSingleCoil,
    WriteSingleRegister,
    ReadExceptionStatus
}
