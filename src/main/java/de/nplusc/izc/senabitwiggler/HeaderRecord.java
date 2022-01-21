package de.nplusc.izc.senabitwiggler;

public class HeaderRecord {
    /*Short Header and Long Header*/
    /*16 bytes*/
    private long shortflag_1;
    private long shortflag_2;
    private long offset;
    private long length;
    private byte[] md5sum;




    // Long Header Only
    /*Has to stay unchanged, saving it into the yml*/
    /*
    shortflag_1
    shortflag_2
    offset
    length
    */
    private long flag_1;
    private long flag_2;
    private long flag_3;
    private long flag_4;
    private long flag_5;

    private long unknown_id;
    /*128 bytes*/
    private byte[] padding;
    private String filename;
    /*filename padded to 128 bytes*/
    /*md5sum*/


    /*Serializer stuff for snakeyaml*/

    public long getShortflag_1() {
        return shortflag_1;
    }

    public void setShortflag_1(long shortflag_1) {
        this.shortflag_1 = shortflag_1;
    }

    public long getShortflag_2() {
        return shortflag_2;
    }

    public void setShortflag_2(long shortflag_2) {
        this.shortflag_2 = shortflag_2;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte[] getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(byte[] md5sum) {
        this.md5sum = md5sum;
    }

    public long getFlag_1() {
        return flag_1;
    }

    public void setFlag_1(long flag_1) {
        this.flag_1 = flag_1;
    }

    public long getFlag_2() {
        return flag_2;
    }

    public void setFlag_2(long flag_2) {
        this.flag_2 = flag_2;
    }

    public long getFlag_3() {
        return flag_3;
    }

    public void setFlag_3(long flag_3) {
        this.flag_3 = flag_3;
    }

    public long getFlag_4() {
        return flag_4;
    }

    public void setFlag_4(long flag_4) {
        this.flag_4 = flag_4;
    }

    public long getFlag_5() {
        return flag_5;
    }

    public void setFlag_5(long flag_5) {
        this.flag_5 = flag_5;
    }

    public long getUnknown_id() {
        return unknown_id;
    }

    public void setUnknown_id(long unknown_id) {
        this.unknown_id = unknown_id;
    }

    public byte[] getPadding() {
        return padding;
    }

    public void setPadding(byte[] padding) {
        this.padding = padding;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public void setMd5String(String md5)
    {
        //HACK, NOP
    }
    public String getMd5String()
    {
        return Utils.bytesToHex(md5sum);
    }
}
