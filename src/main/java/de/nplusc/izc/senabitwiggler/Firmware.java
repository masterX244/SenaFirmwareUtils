package de.nplusc.izc.senabitwiggler;

import java.util.HashMap;

public class Firmware {
    private String DeviceId;
    private HashMap<String, FirmwareVersion> versions;
    private boolean InitialDLDone;

    private int major;
    private int minor;
    private int patch;

    private String roomid;

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
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

    public HashMap<String, FirmwareVersion> getVersions() {
        return versions;
    }

    public void setVersions(HashMap<String, FirmwareVersion> versions) {
        this.versions = versions;
    }

    public boolean isInitialDLDone() {
        return InitialDLDone;
    }

    public void setInitialDLDone(boolean initialDLDone) {
        InitialDLDone = initialDLDone;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }
}
