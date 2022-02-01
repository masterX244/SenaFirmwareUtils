package de.nplusc.izc.senabitwiggler;

import com.google.common.primitives.Longs;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;

public class Utils {
    public static byte[] LongToRawBytes(long l)
    {
        byte[] tmp = Longs.toByteArray(l);
        return new byte[]{tmp[7],tmp[6],tmp[5],tmp[4]};
    }

    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        String hexxit = new String(hexChars).toLowerCase(Locale.ROOT);
        //hexxit = hexxit.replaceAll("^0+", "");
        return hexxit;
    }

    public static void makeSureThatOutFolderIsCreated(String output)
    {
        File od = (new File(output));
        if(!(od.exists()&&od.isDirectory()))
        {
            if(!od.mkdirs())
            {
                System.err.println("WTF! somethint ate shit");
                return;
            }
        }
    }

    public static short xorsum(short[] filewords)
    {
        short xorsum = 0;
        for(int i=0;i<filewords.length;i++)
        {
            xorsum ^= filewords[i];
        }
        return xorsum;
    }

    public static void runTool(String... args) throws IOException {

        runTool(null,args);
    }



    public static void runTool(File workdir, String... args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(args);
        if(workdir !=null&&workdir.isDirectory())
        {
            pb.directory(workdir);
        }
        pb.inheritIO();
        Process runme = pb.start();
        try {
            runme.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
