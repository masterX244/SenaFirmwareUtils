package de.nplusc.izc.senabitwiggler;

public class VMImageHeader {
    private byte[] header;
    private int unknownMagic;
    private long sizeCodeInWords;
    private int szConstantsInWords;
    private int szGlobalsInWords;
    private int szStack; // only 1 byte used;
    private int addressMain;
    private short unknownFlag;
    private int syscallCompatId;
    private byte[] trapSet;
    private String trapSetStringlied;
    private long sizeFileInWords;
    private short chksum; // xorsum, result at end of file should be zero;
    private int unknown_parameter_b;
    private long etcetcaddress;
    private int unknown_twiddled_bits;

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public int getUnknownMagic() {
        return unknownMagic;
    }

    public void setUnknownMagic(int unknownMagic) {
        this.unknownMagic = unknownMagic;
    }

    public long getSizeCodeInWords() {
        return sizeCodeInWords;
    }

    public void setSizeCodeInWords(long sizeCodeInWords) {
        this.sizeCodeInWords = sizeCodeInWords;
    }

    public int getSzConstantsInWords() {
        return szConstantsInWords;
    }

    public void setSzConstantsInWords(int szConstantsInWords) {
        this.szConstantsInWords = szConstantsInWords;
    }

    public int getSzGlobalsInWords() {
        return szGlobalsInWords;
    }

    public void setSzGlobalsInWords(int szGlobalsInWords) {
        this.szGlobalsInWords = szGlobalsInWords;
    }

    public int getSzStack() {
        return szStack;
    }

    public void setSzStack(int szStack) {
        this.szStack = szStack;
    }

    public int getAddressMain() {
        return addressMain;
    }

    public void setAddressMain(int addressMain) {
        this.addressMain = addressMain;
    }

    public short getUnknownFlag() {
        return unknownFlag;
    }

    public void setUnknownFlag(short unknownFlag) {
        this.unknownFlag = unknownFlag;
    }

    public long getSizeFileInWords() {
        return sizeFileInWords;
    }

    public void setSizeFileInWords(long sizeFileInWords) {
        this.sizeFileInWords = sizeFileInWords;
    }

    public short getChksum() {
        return chksum;
    }

    public void setChksum(short chksum) {
        this.chksum = chksum;
    }

    public int getUnknown_parameter_b() {
        return unknown_parameter_b;
    }

    public void setUnknown_parameter_b(int unknown_parameter_b) {
        this.unknown_parameter_b = unknown_parameter_b;
    }

    public long getEtcetcaddress() {
        return etcetcaddress;
    }

    public void setEtcetcaddress(long etcetcaddress) {
        this.etcetcaddress = etcetcaddress;
    }

    public int getUnknown_twiddled_bits() {
        return unknown_twiddled_bits;
    }

    public void setUnknown_twiddled_bits(int unknown_twiddled_bits) {
        this.unknown_twiddled_bits = unknown_twiddled_bits;
    }

    public byte[] getTrapSet() {
        return trapSet;
    }

    public void setTrapSet(byte[] trapSet) {
        this.trapSet = trapSet;
    }

    public String getTrapSetStringlied() {
        return trapSetStringlied;
    }

    public void setTrapSetStringlied(String trapSetStringlied) {
        this.trapSetStringlied = trapSetStringlied;
    }

    public int getSyscallCompatId() {
        return syscallCompatId;
    }

    public void setSyscallCompatId(int syscallCompatId) {
        this.syscallCompatId = syscallCompatId;
    }
}
