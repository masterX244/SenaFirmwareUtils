package de.nplusc.izc.senabitwiggler.DataStructures;

public class QualCommWrapper {
    public byte[] header;
    public byte[] footer;
    public QualCommHeaderRecord[] files;

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte[] getFooter() {
        return footer;
    }

    public void setFooter(byte[] footer) {
        this.footer = footer;
    }

    public QualCommHeaderRecord[] getFiles() {
        return files;
    }

    public void setFiles(QualCommHeaderRecord[] files) {
        this.files = files;
    }


}
