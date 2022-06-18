package de.nplusc.izc.senabitwiggler;

import java.util.HashMap;
import java.util.Set;

public class Statefile {

    private HashMap<String, Firmware> firmwares;

    public HashMap<String, Firmware> getFirmwares() {
        return firmwares;
    }

    public void setFirmwares(HashMap<String, Firmware> firmwares) {
        this.firmwares = firmwares;
    }

    private String generalRoomId;

    private String otherRoomId;

    public String getGeneralRoomId() {
        return generalRoomId;
    }

    public void setGeneralRoomId(String generalRoomId) {
        this.generalRoomId = generalRoomId;
    }

    public String getOtherRoomId() {
        return otherRoomId;
    }

    public void setOtherRoomId(String otherRoomId) {
        this.otherRoomId = otherRoomId;
    }
}
