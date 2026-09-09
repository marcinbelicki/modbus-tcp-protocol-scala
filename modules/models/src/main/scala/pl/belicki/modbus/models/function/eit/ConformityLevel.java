package pl.belicki.modbus.models.function.eit;

public enum ConformityLevel {
    BasicIdentification(0x01),
    RegularIdentification(0x02),
    ExtendedIdentification(0x03);

    private final byte code;

    ConformityLevel(int _code) {
        this.code = (byte) _code;
    }

    public final byte getCode() {
        return this.code;
    }
}

