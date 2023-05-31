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
import java.util.function.Consumer;

public class FirmwareAutoDumper
{
    private static final Logger l = LogManager.getLogger();
    static Statefile state = null;
    static File outfolder_base;
    public static final boolean IA_UPLOAD_ENABLED = false;
    public static final boolean DUMMY_MODE = false;
    public static PrintStream dummydownloadfake = null;
    public static HashMap<String,FirmwareVersion> supprCache = new HashMap<>();

    private static final String[] hackfixsuffixes = new String[]
            {"-rc","rc","-build","build"};

    private static final String[] fileextensionfuckyous = new String[]
            {"Pro.img"};

    private static final Map<String,List<String>> forceVersionsForCheck = new HashMap<>();

    private static final String[][] indexes = new String[][]
            {{
        "main","http://firmware.sena.com/senabluetoothmanager/Firmware"},{
        "harley","http://firmware.sena.com/senabluetoothmanager/harleyFirmware"},{
        "cradlemain","http://firmware.sena.com/senabluetoothmanager/WiFiCradle/bt_img/Firmware"},{
        "cradleharley","http://firmware.sena.com/senabluetoothmanager/WiFiCradle/bt_img/harleyFirmware"}};


    public static void pullFirmwares(File statefile, File outfolder, boolean deepmode)
    {
        outfolder_base=outfolder;
        try {
            if (DUMMY_MODE) {
                dummydownloadfake = new PrintStream(new FileOutputStream(new File(outfolder, "dummy.log")));
            }



            Representer representer = new Representer();
            representer.addClassTag(Firmware.class, new Tag("!FW"));
            representer.addClassTag(FirmwareVersion.class, new Tag("!FWV"));


            Yaml y = new Yaml(representer, new DumperOptions());

            if (statefile.exists()) {
                try {
                    Constructor constructor = new Constructor();
                    constructor.addTypeDescription(new TypeDescription(Firmware.class, "!FW"));
                    constructor.addTypeDescription(new TypeDescription(FirmwareVersion.class, "!FWV"));

                    Yaml internal = new Yaml(constructor);
                    state = (Statefile) internal.load(new FileReader(statefile));
                } catch (FileNotFoundException e) {
                    l.catching(e);
                }
            } else {
                state = new Statefile();
                state.setFirmwares(new LinkedHashMap<>());
            }

            BufferedReader fakerfile = new BufferedReader(new FileReader(statefile.toString()+".forced"));
            fakerfile.lines().forEach((line)->
            {
                String[] linesplitted = line.split("::");
                if(linesplitted.length!=2)
                {
                    l.warn("Malformed Line at forcefile: "+line);
                }
                else
                {
                    forceVersionsForCheck.putIfAbsent(linesplitted[0],new ArrayList<>());
                    List<String> fakeindex = forceVersionsForCheck.get(linesplitted[0]);
                    fakeindex.add(linesplitted[1]);
                }
            });

            state.getFirmwares().forEach((k,v)->
            {
                v.getVersions().entrySet().removeIf((e)->{
                    if(
                            (!e.getValue().isFiller())
                            && e.getValue().getServerCreationDate().equals(new Date(0)) // suspicious crap versions from a bugged run. nuking them
                            )
                    {
                        l.info("Removed a suspicious version: "+e.getKey());
                        return true;
                    };

                    return false;
                });
            });


            File outfolder_tmp = new File(outfolder, "tmp");

            File outfolder_archivarius = new File(outfolder, "archivarius");
            outfolder_archivarius.mkdirs();

            File archiveorgCSV = new File(outfolder_archivarius,"upload.csv");

            for(String[] index:indexes)
            {
                if (!DUMMY_MODE) {
                    new File(outfolder_tmp, "Firmware").delete();
                }
                File downloadFolderBase = new File(outfolder_base, "downloadstage");
                downloadFolderBase.mkdirs();
                File fw = new File(outfolder_tmp, "Firmware");
                outfolder_tmp.mkdirs();
                if (!fw.exists()) // should only happen in dummy mode or if something zarfed out while dev
                {
                    Utils.runTool(outfolder_tmp,
                            "wget",
                            index[1],
                            "-O",
                            "Firmware"
                    );
                }

                BufferedReader br = new BufferedReader(new FileReader(fw));
                final HashMap<String, String> basenamelookup = new LinkedHashMap<>();
                // filename --> real basename

                Consumer<String> linehandler = (line_raw) ->
                {
                    String realExt = null;
                    String line = line_raw;
                    for(String suffixcheck:fileextensionfuckyous)
                    {
                        if(line_raw.endsWith(suffixcheck))
                        {
                            realExt = suffixcheck;
                        }
                    }



                    String[] linesplitted = line.split(":");

                    String FWFileName;
                    String FWBAsename = "ZÖINKS!";
                    if (linesplitted.length == 4) {
                        FWBAsename = linesplitted[0] + "-" + linesplitted[2];
                        FWFileName = linesplitted[3];
                    } else if (linesplitted.length == 3) {
                        FWBAsename = linesplitted[0] + "-NOLNG";
                        FWFileName = linesplitted[2];
                    } else {
                        return;
                    }

                    if (basenamelookup.containsKey(FWFileName)) {
                        return; //languageless file, no need to handle multiple times.
                    } else {
                        basenamelookup.put(FWFileName, FWBAsename);
                    }


                    if (!state.getFirmwares().containsKey(FWBAsename)) {
                        Firmware tmp = new Firmware();
                        tmp.setDeviceId(FWBAsename);
                        tmp.setInitialDLDone(false);
                        tmp.setVersions(new LinkedHashMap<>());
                        state.getFirmwares().put(FWBAsename, tmp);
                    }

                    Firmware f = state.getFirmwares().get(FWBAsename);

                    try {
                        l.info("Processing line:" + line + "\n Basename:" + FWBAsename + "of index "+index[0]);
                        int[] fwnumber = splitVersionAndPullFW(
                                FWFileName,
                                f.getVersions(),
                                deepmode,
                                index[0], "Sus Version with File line: "+line,
                                realExt);
                        f.setMajor(fwnumber[0]);
                        f.setMinor(fwnumber[1]);
                        f.setPatch(fwnumber[2]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                br.lines().forEach(linehandler);

                //forcelist entires = index entries with index name prepended with a ::, prefix already removed at this point
                var forceToCheck = forceVersionsForCheck.getOrDefault(index[0],new ArrayList<String>()); //empty for no forcelist entries
                forceToCheck.forEach(linehandler);
                br.close();
                y.dump(state, new FileWriter(statefile));
                if (!DUMMY_MODE) {
                    new File(outfolder_tmp, "Firmware").delete();
                    pushToMatrix();

                }

            }


            if(DUMMY_MODE)
            {
                dummydownloadfake.close();
            }
            generateIACSV(archiveorgCSV,outfolder);
            y.dump(state,new FileWriter(statefile));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private static void generateIACSV(File destination, File outbase)
    {
        if(EntryPoint.c.getArchiveOrgCollection()==null)
        {
            return; //kein archive.org file da nicht configuriert
        }
        HashSet<String> csvlines = new HashSet<>();
        state.getFirmwares().forEach((k,v)->
        {

            String[] keysplitted = k.split("-");

            String key_delanguaged = keysplitted[0];
            if(keysplitted.length>=2)
            {
                for(int i=1;i<keysplitted.length-1;i++)
                {
                    key_delanguaged = key_delanguaged+"-"+keysplitted[i];
                }
            }
            //key_delanguaged.replace(".","_");
            final String itemname = "sena_firmware_files_"+key_delanguaged;
            v.getVersions().forEach((k2,v2)->
            {
                if((!v2.isIAUploaded()&&(!v2.isFiller())))
                {
                    v2.setIAUploaded(true);
                    String filename = new File(outbase,v2.getFilename()).getAbsoluteFile().toString();
                    csvlines.add(itemname+","+filename+",firmware,"+EntryPoint.c.getArchiveOrgCollection()+",software");
                }
            });
        });
        try (FileOutputStream w = new FileOutputStream(destination);PrintStream p = new PrintStream(w);)
        {
            p.println("identifier,file,subject,collection,mediatype");
            String[] lines = csvlines.toArray(new String[csvlines.size()]);
            for(String line :lines)
            {
                p.println(line);
            }
        } catch (IOException e) {
            l.warn("IO Error on generateIACSV");
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
                    String lastSuffix = v2.isRc_hackfix()?v2.getHackfix():".";
                    String version = v2.getMajor()+"."+v2.getMinor()+lastSuffix+v2.getPatch();
                    String index = v2.getFirmwareLocation();
                    Date ts = v2.getServerCreationDate();
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                    df.setTimeZone(tz);
                    String date = df.format(ts);
                    String msg = "Detected new Version: "+version+" for "+k+" uploaded at: "+date+ "on index "+index;

                    if(v2.isHidden())
                    {
                        msg = "Guessed new hidden Version: "+version+" for "+k+" uploaded at: "+date+ "on index "+index;
                    }
                    if(v2.getSusVersion()!=null)
                    {
                        msg = msg +"\n"+v2.getSusVersion();
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



    private static int[] splitVersionAndPullFW(
            String filename,
            HashMap<String,
            FirmwareVersion> knownVersions,
            boolean deepmode,String indexUsed,
            String useForSusVersion,
            String realext) throws IOException {
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
        boolean rchackfix = false;
        String hackfixsuffix = ".";
        for(String suffixcheck:hackfixsuffixes)
        {
            if(version.contains(suffixcheck))
            {
                rchackfix=true;
                version = version.replace(suffixcheck,".");
                hackfixsuffix = suffixcheck;
            }
        }

        String[] vsplit = version.split("\\.");

        if(realext != null) //Hackfix again since Sena got no consistency in the file naming scheme... ,,|,,
        {
            int suffixlen = realext.split("\\.").length;
            ArrayList tmp = new ArrayList<String>(vsplit.length);
            tmp.addAll(Arrays.asList(vsplit));
            vsplit = (String[])(tmp.subList(0,vsplit.length-(suffixlen-1)).toArray(new String[1]));
            vsplit[vsplit.length-1]=realext;
        }

        String suffix = "";
        int major=0,minor=0,patch=0;
        major=Integer.valueOf(vsplit[0]);
        if(vsplit.length==5&&rchackfix)  // another hackfix since sena tries to trololol me..., ,,|,,
        {
            minor = Integer.valueOf(vsplit[1]);
            patch = Integer.valueOf(vsplit[2]);
            suffix = suffix = hackfixsuffix+vsplit[3]+"."+vsplit[4];
            rchackfix=false;
        }
        else
        {
            if(vsplit.length==4)
            {

                minor = Integer.valueOf(vsplit[1]);
                try
                {
                    patch = Integer.valueOf(vsplit[2]);
                    suffix = "."+vsplit[3];
                }
                catch(NumberFormatException ex)
                {
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
        }

        String prefix = magic[0]+(fuckingoddity?"_":"-")+"v";
        if(hackfix50R_SR)
        {
            prefix = magic[0]+"SRv";
        }
        //suffix = suffix+".img";

        scanDownwards(major,minor,patch,prefix,suffix,knownVersions,rchackfix,hackfixsuffix,indexUsed, useForSusVersion);

        //scanning upwards for "poking" for undocumented stuff
        if(deepmode)
        {
            //String synthesizedFileName = prefix+(major)+"."+minor+"."+(patch)+suffix;
            String synthesizedFileName = prefix+(major+1)+"."+0+"."+(0)+suffix;
            String synthesizedFileNameShort = prefix+(major+1)+"."+0+suffix;
            peekNext(synthesizedFileName,synthesizedFileNameShort,major+1,0,0,knownVersions,indexUsed);

            synthesizedFileName = prefix+(major)+"."+(minor+1)+"."+(0)+suffix;
            synthesizedFileNameShort = prefix+(major)+"."+(minor+1)+suffix;
            peekNext(synthesizedFileName,synthesizedFileNameShort,major,minor+1,0,knownVersions,indexUsed);

            synthesizedFileName = prefix+(major)+"."+minor+"."+(patch+1)+suffix;
            peekNext(synthesizedFileName,null,major,minor,patch+1,knownVersions,indexUsed);
        }

        return new int[]{major,minor,patch};
    }

    private static void scanDownwards(
            int major,
            int minor,
            int patch,
            String prefix,
            String suffix,
            HashMap<String,FirmwareVersion> knownVersions,
            boolean rchackfix,
            String hackfixsuffix,
            String indexUsed,
            String useForSusVersion
            ) throws IOException {
        l.info(major+"--"+minor+"--"+patch);
        List<FirmwareVersion> temp = new ArrayList<>();
        l.info(prefix+"."+major+"."+minor+"."+patch+"."+suffix);

        if(major==0) //sus version, FUCK YOU AGAIN SENA, XREF: https://repo.nplusc.de/SenaBitWiggler/AutoXml/commit/f17a35d283871ec9357ae94fe561e8a998993b8f
        {
            String lastSuffix = rchackfix?hackfixsuffix:".";
            String synthesizedFileName = prefix+(major)+"."+minor+lastSuffix+(patch)+suffix;
            boolean nopeOutVersion = false;
            var fwVersion = knownVersions.getOrDefault(synthesizedFileName,null);
            if(fwVersion!=null&&fwVersion.isFiller())
            {
                nopeOutVersion=true;
            }
            if(!knownVersions.containsKey(synthesizedFileName)||nopeOutVersion)
            {
                FirmwareVersion f = getFirmware(major,minor,patch,false,synthesizedFileName,rchackfix,hackfixsuffix,indexUsed);
                f.setSusVersion(useForSusVersion);
                knownVersions.put(synthesizedFileName,f);
            }
        }

        while(major>=1&&minor>=0&&patch>=0) //older versions won't be bruteforced, check for existence in the index prevents a redownload.
        {
            String lastSuffix = rchackfix?hackfixsuffix:".";
            String synthesizedFileName = prefix+(major)+"."+minor+lastSuffix+(patch)+suffix;

            if(knownVersions.containsKey(synthesizedFileName)) //skipping redownload and setting it as "not hidden" if it was a hidden version
            {
                knownVersions.get(synthesizedFileName).setHidden(false);
            }
            else
            {
                FirmwareVersion f = getFirmware(major,minor,patch,false,synthesizedFileName,rchackfix,hackfixsuffix,indexUsed);
                knownVersions.put(synthesizedFileName,f);
            }
            if(patch==0)
            {
                if(!rchackfix)
                {
                    synthesizedFileName = prefix+(major)+"."+minor+suffix;
                }
                if(knownVersions.containsKey(synthesizedFileName)) //skipping redownload and setting it as "not hidden" if it was a hidden version
                {
                    knownVersions.get(synthesizedFileName).setHidden(false);
                }
                else
                {
                    FirmwareVersion f2 = getFirmware(major,minor,patch,false,synthesizedFileName,rchackfix,hackfixsuffix,indexUsed);
                    knownVersions.put(synthesizedFileName,f2);
                }
                if(rchackfix)
                {
                    break; //rc-versions need different fudgery...
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


    private static FirmwareVersion getFirmware(int major, int minor, int patch, boolean hidden, String fwname,boolean rchackfix,String hackfixsuffix,String indexUsed) throws IOException {
        FirmwareVersion f = new FirmwareVersion();
        f.setFilename(fwname);
        f.setHidden(hidden);
        f.setMajor(major);
        f.setMinor(minor);
        f.setPatch(patch);
        f.setRc_hackfix(rchackfix);
        f.setFirmwareLocation(indexUsed);
        f.setHackfix(hackfixsuffix);
        File downloadFolderBase = new File(outfolder_base,"downloadstage");
        if(supprCache.containsKey(fwname)) //leaves a "fake entry" when 2 different deviceIDs would lead to the same filename
        {
            l.info("Fakerfuck");
            FirmwareVersion f2 =  supprCache.get(fwname);
            f.setFiller(f2.isFiller());
            f.setServerCreationDate(f2.getServerCreationDate());
            return f;
        }
        supprCache.put(fwname,f); // banning a filename from rescan/redownload
        //if(true) return f;
        boolean originalLocationSuccessful = false;
        if(!DUMMY_MODE)
        {
            Utils.runTool(downloadFolderBase,
                    "wget",
                    "http://firmware.sena.com/senabluetoothmanager/"+fwname
            );
            originalLocationSuccessful = new File(downloadFolderBase,fwname).exists();
            if(!originalLocationSuccessful)
            {
                // new alternative location for additional images used by the WiFi cradle. Checking there if something does not exist at original
                Utils.runTool(downloadFolderBase,
                        "wget",
                        "http://firmware.sena.com/senabluetoothmanager/WiFiCradle/bt_img/"+fwname
                );
            }
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
            if(dlf.lastModified() <100000) // suspicious undefined date
            {
                f.setFiller(true);
                f.setServerCreationDate(new Date(0));
            }
            else
            {
                f.setAlternativeLocation(!originalLocationSuccessful);
                f.setFiller(false);
                f.setServerCreationDate(new Date(dlf.lastModified()));
                if(!dst.exists())
                {
                    Files.copy(dlf.toPath(),dst.toPath());
                }
            }
        }
        else
        {
            f.setFiller(true);
            f.setServerCreationDate(new Date(0));
        }
        return f;
    }

    private static void peekNext(
            String synthesizedFileName,
            String synthesizedFileNameShort,
            int major,
            int minor,
            int patch,
            HashMap<String,FirmwareVersion> knownVersions,
            String indexUsed
    ) throws IOException {
        l.info("peekstate:");
        l.info(knownVersions.containsKey(synthesizedFileName));
        l.info(knownVersions.containsKey(synthesizedFileNameShort));
        if(!(knownVersions.containsKey(synthesizedFileName)||knownVersions.containsKey(synthesizedFileNameShort))) // only re-poking if nothing was there yet.
        {
            FirmwareVersion nextMajor = getFirmware(major,minor,patch,true,synthesizedFileName,false,".",indexUsed);
            if(nextMajor.isFiller())
            {
                if(synthesizedFileNameShort != null)
                {
                    nextMajor = getFirmware(major,minor,patch,true,synthesizedFileNameShort,false,".",indexUsed);
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
