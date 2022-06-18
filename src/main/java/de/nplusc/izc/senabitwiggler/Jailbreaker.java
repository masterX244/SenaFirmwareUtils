package de.nplusc.izc.senabitwiggler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Jailbreaker {

    private static final Logger l = LogManager.getLogger();
    private static String nvscmd = EntryPoint.BlueLabPath+"nvscmd";
    private static String pscli = EntryPoint.BlueLabPath+"pscli";

    private static String dfuunbuild = EntryPoint.BlueLabPath+"dfuunbuild";
    private static String dfubuild = EntryPoint.BlueLabPath+"dfubuild";
    private static String dfusign = EntryPoint.BlueLabPath+"dfusign";

    public static void dumpFlash(String file, File folder)
    {
        folder.mkdirs();
        File internaldump = new File(folder,file+"internal.xuv");
        File externaldump = new File(folder,file+"external.xuv");
        File externalpartitioned = new File(folder,file+"external.ptn");
        File psr = new File(folder,file+"psr");
        try {
            Utils.runTool(nvscmd,"-nvstype","int","dump",internaldump.getPath());
            Utils.runTool(nvscmd,"dump",externaldump.getPath());
            Utils.runTool(nvscmd,"dump",externalpartitioned.getPath());
            Utils.runTool(pscli,"dump",psr.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void jailbreak()
    {
        try {
            Utils.runTool(pscli,"merge",EntryPoint.APPDIR+File.separator+"jailbreak"+File.separator+"jailbreak.psr");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resignDFU(String file, File outputFolder)
    {
        File scratch = new File(outputFolder,"temp");
        scratch.mkdirs();
        String dfuinput = new File(file).getAbsolutePath();
        String dfuname = new File(file).getName();
        String dfuout = new File(outputFolder,dfuname).getAbsolutePath();
        try {
            Utils.runTool(scratch,dfuunbuild,"-v","-f",dfuinput,"-o","extracted");
            Utils.runTool(scratch,dfusign,"-v", "-o","extracted0000signed.fs","-h","extracted0000.fs", "-ka",EntryPoint.APPDIR+File.separator+"jailbreak"+File.separator+"jailbreak.private.key");
            File oldfs = new File(scratch,"extracted0000.fs");
            oldfs.delete();
            File newfs = new File(scratch,"extracted0000signed.fs");
            newfs.renameTo(oldfs);
            Utils.runTool(scratch,dfubuild,"-c","extracted.cl","-f",dfuout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
