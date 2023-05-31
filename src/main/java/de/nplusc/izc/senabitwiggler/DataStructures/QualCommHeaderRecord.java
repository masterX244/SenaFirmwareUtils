package de.nplusc.izc.senabitwiggler.DataStructures;

public class QualCommHeaderRecord {
    public int size;
    public String filename;

    public short location;
    public short sublocation;

    public short getLocation() {
        return location;
    }

    public void setLocation(short location) {
        this.location = location;
    }

    public short getSublocation() {
        return sublocation;
    }

    public void setSublocation(short sublocation) {
        this.sublocation = sublocation;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
