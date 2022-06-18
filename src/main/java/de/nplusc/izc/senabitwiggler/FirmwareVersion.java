package de.nplusc.izc.senabitwiggler;

import java.util.Date;

public class FirmwareVersion {
    private Date ServerCreationDate;
    private String Filename;
    private int major;
    private int minor;
    private int patch;
    private boolean hidden;

    private MatrixState NotificationState = MatrixState.TODO;

    public MatrixState getNotificationState() {
        return NotificationState;
    }

    public void setNotificationState(MatrixState notificationState) {
        NotificationState = notificationState;
    }

    // signifies that a version is not a valid one and only "inserted" due to the guesser going over it. prevents re-guessing;
    private boolean filler;

    public boolean isFiller() {
        return filler;
    }

    public void setFiller(boolean filler) {
        this.filler = filler;
    }

    public Date getServerCreationDate() {
        return ServerCreationDate;
    }

    public void setServerCreationDate(Date serverCreationDate) {
        ServerCreationDate = serverCreationDate;
    }

    public String getFilename() {
        return Filename;
    }

    public void setFilename(String filename) {
        Filename = filename;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getPatch() {
        return patch;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
