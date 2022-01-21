package de.nplusc.izc.senabitwiggler;

public class LongHeader {

    /*assuming 24 bytes but going to play it safe and storing the excess bytes, too*/
    private String version;
    private byte[] version_raw;

    private long MagicShit; /*0x19 0x28 0xc5 0xE6*/
    /*related to the random_id in the headers
    read as BCD here and then that number as hex in the file. some variants got mangled IDs, no rule so far.
     */
    private long random_id;

    /*long count, not needed since the records[] tells that count*/

    private HeaderRecord[] headerRecords;


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public byte[] getVersion_raw() {
        return version_raw;
    }

    public void setVersion_raw(byte[] version_raw) {
        this.version_raw = version_raw;
    }

    public long getMagicShit() {
        return MagicShit;
    }

    public void setMagicShit(long magicShit) {
        MagicShit = magicShit;
    }

    public long getRandom_id() {
        return random_id;
    }

    public void setRandom_id(long random_id) {
        this.random_id = random_id;
    }

    public HeaderRecord[] getHeaderRecords() {
        return headerRecords;
    }

    public void setHeaderRecords(HeaderRecord[] headerRecords) {
        this.headerRecords = headerRecords;
    }
}
