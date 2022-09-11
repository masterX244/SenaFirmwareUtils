package de.nplusc.izc.senabitwiggler;

import io.github.ma1uta.matrix.client.StandaloneClient;
import io.github.ma1uta.matrix.client.model.room.CreateRoomRequest;
import io.github.ma1uta.matrix.event.content.RoomPowerLevelsContent;
import io.github.ma1uta.matrix.impl.exception.MatrixException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import javax.swing.plaf.nimbus.State;
import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FirmwareAutoDumper
{
    private static final Logger l = LogManager.getLogger();
    static Statefile state = null;
    static File outfolder_base;
    public static final boolean IA_UPLOAD_ENABLED = false;
    public static final boolean DUMMY_MODE = false;
    public static PrintStream dummydownloadfake = null;
    public static HashMap<String,FirmwareVersion> supprCache = new HashMap<>();

    public static void pullFirmwares(File statefile, File outfolder, boolean deepmode)
    {
        outfolder_base=outfolder;
        try{
            if(DUMMY_MODE)
            {
                dummydownloadfake = new PrintStream(new FileOutputStream(new File(outfolder,"dummy.log")));
            }

            Representer representer = new Representer();
            representer.addClassTag(Firmware.class, new Tag("!FW"));
            representer.addClassTag(FirmwareVersion.class, new Tag("!FWV"));


            Yaml y = new Yaml(representer, new DumperOptions());

            if(statefile.exists())
            {
                try {
                    Constructor constructor = new Constructor();
                    constructor.addTypeDescription(new TypeDescription(Firmware.class, "!FW"));
                    constructor.addTypeDescription(new TypeDescription(FirmwareVersion.class, "!FWV"));

                    Yaml internal = new Yaml(constructor);
                    state = (Statefile) internal.load(new FileReader(statefile));
                } catch (FileNotFoundException e) {
                    l.catching(e);
                }
            }
            else
            {
                state = new Statefile();
                state.setFirmwares(new LinkedHashMap<>());
            }

            File outfolder_tmp = new File(outfolder,"tmp");
            if(!DUMMY_MODE)
            {
                new File(outfolder_tmp,"Firmware").delete();
            }
            File downloadFolderBase = new File(outfolder_base,"downloadstage");
            downloadFolderBase.mkdirs();
            File fw = new File(outfolder_tmp,"Firmware");
            outfolder_tmp.mkdirs();
            if(!fw.exists()) // should only happen in dummy mode or if something zarfed out while dev
            {
            Utils.runTool(outfolder_tmp,
                    "wget",
                    "http://firmware.sena.com/senabluetoothmanager/Firmware"
                    );
            }

            BufferedReader br = new BufferedReader(new FileReader(fw));
            final HashMap<String,String> basenamelookup = new LinkedHashMap<>();
            // filename --> real basename
            br.lines().forEach((line)->
                {
                    String[] linesplitted = line.split(":");

                    String FWFileName;
                    String FWBAsename = "ZÖINKS!";
                    if(linesplitted.length==4)
                    {
                        FWBAsename = linesplitted[0]+"-"+linesplitted[2];
                        FWFileName = linesplitted[3];
                    }
                    else if(linesplitted.length==3)
                    {
                        FWBAsename = linesplitted[0]+"-NOLNG";
                        FWFileName = linesplitted[2];
                    }
                    else
                    {
                       return;
                    }

                    if(basenamelookup.containsKey(FWFileName))
                    {
                        return; //languageless file, no need to handle multiple times.
                    }
                    else
                    {
                        basenamelookup.put(FWFileName,FWBAsename);
                    }


                    if(!state.getFirmwares().containsKey(FWBAsename))
                    {
                        Firmware tmp = new Firmware();
                        tmp.setDeviceId(FWBAsename);
                        tmp.setInitialDLDone(false);
                        tmp.setVersions(new LinkedHashMap<>());
                        state.getFirmwares().put(FWBAsename,tmp);
                    }

                    Firmware f = state.getFirmwares().get(FWBAsename);

                    try {
                        l.info("Processing line:"+line+"\n Basename:"+FWBAsename);
                        int[] fwnumber = splitVersionAndPullFW(FWFileName,f.getVersions(),deepmode);
                        f.setMajor(fwnumber[0]);
                        f.setMinor(fwnumber[1]);
                        f.setPatch(fwnumber[2]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            br.close();
            y.dump(state,new FileWriter(statefile));
            if(!DUMMY_MODE)
            {
                new File(outfolder_tmp,"Firmware").delete();
                pushToMatrix();
            }
            if(DUMMY_MODE)
            {
                dummydownloadfake.close();
            }

            y.dump(state,new FileWriter(statefile));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private static void pushToMatrix()
    {
        if(!EntryPoint.c.isMatrixEnabled())
        {
            //skip all if matrix is off.
            return;
        }
        StandaloneClient mxClient = new StandaloneClient.Builder().domain(EntryPoint.c.getMatrixDomain()).build();
        mxClient.auth().login(EntryPoint.c.getMatrixUser(), EntryPoint.c.getMatrixPassword().toCharArray());
        if(state.getGeneralRoomId()==null)
        {
            l.info("Create Matrix Room General");
            CreateRoomRequest r = new CreateRoomRequest();
            r.setName("Sena Full Logstream");
            r.setRoomAliasName("SenaFirmwareAll");
            RoomPowerLevelsContent lvls = new RoomPowerLevelsContent();
            lvls.setEventsDefault((byte)1); //readonly except for the BitWiggler
            r.setPowerLevelContentOverride(lvls);
            r.setVisibility("public");
            state.setGeneralRoomId(mxClient.room().create(r).getRoomId());
        }

        if(state.getOtherRoomId()==null)
        {
            l.info("Create Matrix Room Catchall");
            CreateRoomRequest r = new CreateRoomRequest();
            r.setName("Sena Other Logstream");
            r.setRoomAliasName("SenaFirmwareOther");
            RoomPowerLevelsContent lvls = new RoomPowerLevelsContent();
            lvls.setEventsDefault((byte)1); //readonly except for the BitWiggler
            r.setPowerLevelContentOverride(lvls);
            r.setVisibility("public");
            state.setOtherRoomId(mxClient.room().create(r).getRoomId());
        }

        state.getFirmwares().forEach((k,v)->
        {
            if(v.getRoomid()==null)
            {
                l.info("Create Matrix Room for:"+k);
                CreateRoomRequest r = new CreateRoomRequest();
                r.setName("Sena Logstream: "+v.getDeviceId());
                r.setRoomAliasName("SenaStream"+k);
                RoomPowerLevelsContent lvls = new RoomPowerLevelsContent();
                lvls.setEventsDefault((byte)1); //readonly except for the BitWiggler
                r.setPowerLevelContentOverride(lvls);
                r.setVisibility("public");
                while(v.getRoomid()==null)
                {
                    try {

                        v.setRoomid(mxClient.room().create(r).getRoomId());
                    }
                    catch(MatrixException e)
                    {

                    }
                    if(v.getRoomid()==null)
                    {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            List<String> keys = new ArrayList<>(v.getVersions().keySet());
            keys.sort((x,y)->
            {
                var vr = v.getVersions();
                var version1 = vr.get(x);
                var version2 = vr.get(y);
                if(version1.getMajor()< version2.getMajor()) {
                    return -1;
                }
                if(version1.getMajor()> version2.getMajor()) {
                    return 1;
                }
                if(version1.getMinor()< version2.getMinor()) {
                    return -1;
                }
                if(version1.getMinor()> version2.getMinor()) {
                    return 1;
                }
                if(version1.getPatch()< version2.getPatch()) {
                    return -1;
                }
                if(version1.getPatch()> version2.getPatch()) {
                    return 1;
                }
                return 0;
            });
            for (var v2p : keys) {
                final var v2 = v.getVersions().get(v2p); //HAXX
                LinkedList<Runnable> messageActions = new LinkedList<>();

                if(!v2.isFiller())
                {
                    String version = v2.getMajor()+"."+v2.getMinor()+"."+v2.getPatch();
                    Date ts = v2.getServerCreationDate();
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                    df.setTimeZone(tz);
                    String date = df.format(ts);
                    String msg = "Detected new Version: "+version+" for "+k+" uploaded at: "+date;

                    if(v2.isHidden())
                    {
                        msg = "Guessed new hidden Version: "+version+" for "+k+" uploaded at: "+date;
                    }
                    if((!v2.isHidden()&&v2.getNotificationState()==MatrixState.SENT_PRELIM)||v2.getNotificationState()==MatrixState.TODO)
                    {
                        final String msg2 = msg;
                        messageActions.add(()->
                        {
                            l.info("Push: "+msg2);
                            mxClient.event().sendMessage(v.getRoomid(), msg2);
                            mxClient.event().sendMessage(state.getGeneralRoomId(), msg2);
                            v2.setNotificationState(v2.isHidden()?MatrixState.SENT_PRELIM:MatrixState.SENT);
                        });
                    }
                }
                messageActions.iterator().forEachRemaining(runnable -> runnable.run());
            }
        });
        mxClient.auth().logout();
    }



    private static int[] splitVersionAndPullFW(String filename,HashMap<String,FirmwareVersion> knownVersions, boolean deepmode) throws IOException {
        String[] magic = filename.split("-v");
        boolean fuckingoddity = false;
        boolean hackfix50R_SR = false;
        if(magic.length==1)
        {
            fuckingoddity = true;
            magic = filename.split("_v");
            if(magic.length==1)
            {
                hackfix50R_SR = true;
                magic = filename.split("SRv");

            }
        }
        String version = magic[1];
        String[] vsplit = version.split("\\.");

        String suffix = "";
        int major=0,minor=0,patch=0;
        major=Integer.valueOf(vsplit[0]);
        if(vsplit.length==4)
        {
            minor = Integer.valueOf(vsplit[1]);
            patch = Integer.valueOf(vsplit[2].substring(0,1));
            if(vsplit[2].length()>1)
            {
                suffix = vsplit[2].substring(1)+"."+vsplit[3];
            }
            else
            {
                suffix = "."+vsplit[3];
            }
        }
        else
        {
            if(vsplit[1].length()>1)
            {
                minor = Integer.valueOf(vsplit[1].substring(0,1));
                suffix = vsplit[1].substring(1)+"."+vsplit[2];
            }
            else
            {
                minor = Integer.valueOf(vsplit[1].substring(0,1));
                suffix = "."+vsplit[2];
            }
        }

        String prefix = magic[0]+(fuckingoddity?"_":"-")+"v";
        if(hackfix50R_SR)
        {
            prefix = magic[0]+"SRv";
        }
        //suffix = suffix+".img";

        scanDownwards(major,minor,patch,prefix,suffix,knownVersions);

        //scanning upwards for "poking" for undocumented stuff
        if(deepmode)
        {
            //String synthesizedFileName = prefix+(major)+"."+minor+"."+(patch)+suffix;
            String synthesizedFileName = prefix+(major+1)+"."+0+"."+(0)+suffix;
            String synthesizedFileNameShort = prefix+(major+1)+"."+0+suffix;
            peekNext(synthesizedFileName,synthesizedFileNameShort,major+1,0,0,knownVersions);

            synthesizedFileName = prefix+(major)+"."+(minor+1)+"."+(0)+suffix;
            synthesizedFileNameShort = prefix+(major)+"."+(minor+1)+suffix;
            peekNext(synthesizedFileName,synthesizedFileNameShort,major,minor+1,0,knownVersions);

            synthesizedFileName = prefix+(major)+"."+minor+"."+(patch+1)+suffix;
            peekNext(synthesizedFileName,null,major,minor,patch+1,knownVersions);
        }

        return new int[]{major,minor,patch};
    }

    private static void scanDownwards(
            int major,
            int minor,
            int patch,
            String prefix,
            String suffix,
            HashMap<String,FirmwareVersion> knownVersions) throws IOException {
        l.info(major+"--"+minor+"--"+patch);
        List<FirmwareVersion> temp = new ArrayList<>();
        l.info(prefix+"."+major+"."+minor+"."+patch+"."+suffix);

        while(major>=1&&minor>=0&&patch>=0) //older versions won't be bruteforced, check for existence in the index prevents a redownload.
        {
            String synthesizedFileName = prefix+(major)+"."+minor+"."+(patch)+suffix;

            if(knownVersions.containsKey(synthesizedFileName)) //skipping redownload and setting it as "not hidden" if it was a hidden version
            {
                knownVersions.get(synthesizedFileName).setHidden(false);
            }
            else
            {
                FirmwareVersion f = getFirmware(major,minor,patch,false,synthesizedFileName);
                knownVersions.put(synthesizedFileName,f);
            }
            if(patch==0)
            {
                synthesizedFileName = prefix+(major)+"."+minor+suffix;
                if(knownVersions.containsKey(synthesizedFileName)) //skipping redownload and setting it as "not hidden" if it was a hidden version
                {
                    knownVersions.get(synthesizedFileName).setHidden(false);
                }
                else
                {
                    FirmwareVersion f2 = getFirmware(major,minor,patch,false,synthesizedFileName);
                    knownVersions.put(synthesizedFileName,f2);
                }
            }
            patch--;
            if(patch<0)
            {
                patch=9;
                minor--;
            }
            if(minor<0)
            {
                minor=9;
                major--;
            }
        }/**/
    }


    private static FirmwareVersion getFirmware(int major, int minor, int patch, boolean hidden, String fwname) throws IOException {
        FirmwareVersion f = new FirmwareVersion();
        f.setFilename(fwname);
        f.setHidden(hidden);
        f.setMajor(major);
        f.setMinor(minor);
        f.setPatch(patch);
        File downloadFolderBase = new File(outfolder_base,"downloadstage");
        if(supprCache.containsKey(fwname)) //leaves a "fake entry" when 2 different deviceIDs would lead to the same filename
        {
            l.info("Fakerfuck");
            FirmwareVersion f2 =  supprCache.get(fwname);
            f.setFiller(false);
            f.setServerCreationDate(f2.getServerCreationDate());
            return f;
        }
        supprCache.put(fwname,f); // banning a filename from rescan/redownload
        //if(true) return f;
        if(!DUMMY_MODE)
        {
            Utils.runTool(downloadFolderBase,
                    "wget",
                    "http://firmware.sena.com/senabluetoothmanager/"+fwname
            );
        }
        else
        {
            dummydownloadfake.println("http://firmware.sena.com/senabluetoothmanager/"+fwname);
        }
        l.info("New Version spotted:"+fwname);
        File dlf = new File(downloadFolderBase,fwname);
        File dst = new File(outfolder_base,fwname);
        if(dlf.exists())
        {
            f.setFiller(false);
            f.setServerCreationDate(new Date(dlf.lastModified()));
            if(!dst.exists())
            {
                Files.copy(dlf.toPath(),dst.toPath());
            }
        }
        else
        {
            f.setFiller(true);
            f.setServerCreationDate(new Date(0));
        }
        return f;
    }

    private static void peekNext(String synthesizedFileName, String synthesizedFileNameShort, int major, int minor, int patch, HashMap<String,FirmwareVersion> knownVersions) throws IOException {
        l.info("peekstate:");
        l.info(knownVersions.containsKey(synthesizedFileName));
        l.info(knownVersions.containsKey(synthesizedFileNameShort));
        if(!(knownVersions.containsKey(synthesizedFileName)||knownVersions.containsKey(synthesizedFileNameShort))) // only re-poking if nothing was there yet.
        {
            FirmwareVersion nextMajor = getFirmware(major,minor,patch,true,synthesizedFileName);
            if(nextMajor.isFiller())
            {
                if(synthesizedFileNameShort != null)
                {
                    nextMajor = getFirmware(major,minor,patch,true,synthesizedFileNameShort);
                    synthesizedFileName = synthesizedFileNameShort;
                }
            }
            l.info("peekstate:"+nextMajor.isFiller());
            if(!nextMajor.isFiller())
            {
                l.info("SUCCESS for:"+synthesizedFileName);
                knownVersions.put(synthesizedFileName,nextMajor);
            }
        }
    }

}
