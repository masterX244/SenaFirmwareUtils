package de.nplusc.izc.senabitwiggler;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.yaml.snakeyaml.Yaml;

import java.io.*;

public class VmAppFIleExtraction {
    public static void extractVmImage(File input, String output)
    {
        byte filler = 0x00;
        try (RandomAccessFile f = new RandomAccessFile(input,"r")) {
            VMImageHeader h = new VMImageHeader();

            long len = f.length()/2;
            if(len>Integer.MAX_VALUE)
            {
                System.out.println("Ugggh, File too big");
                return;
            }

            short[] checksumarray = new short[(int)len];

            for(int i=0;i<len;i++)
            {
                checksumarray[i]=f.readShort();
            }
            if(Utils.xorsum(checksumarray)!=0)
            {
                System.err.println("Smells like Dead Beef, the data seems to be corrupt");
                System.exit(1);
            }
            f.seek(0);



            byte[] magic = new byte[8];
            f.read(magic);

            h.setHeader(magic);

            byte[] shortPants = new byte[2];
            byte[] integer = new byte[4];
            f.read(shortPants);
            h.setUnknownMagic(Ints.fromBytes(filler,filler,shortPants[0],shortPants[1]));

            f.read(integer);
            h.setSizeCodeInWords(Longs.fromBytes(filler,filler,filler,filler,integer[0],integer[1],integer[2],integer[3]));
            f.read(shortPants);
            h.setSzConstantsInWords(Ints.fromBytes(filler,filler,shortPants[0],shortPants[1]));
            f.read(shortPants);
            h.setSzGlobalsInWords(Ints.fromBytes(filler,filler,shortPants[0],shortPants[1]));
            f.read(shortPants);
            h.setSzStack(Ints.fromBytes(filler,filler,shortPants[0],shortPants[1]));
            f.read(shortPants);
            h.setAddressMain(Ints.fromBytes(filler,filler,shortPants[0],shortPants[1]));
            f.read(shortPants);
            h.setUnknownFlag(Shorts.fromBytes(shortPants[0],shortPants[1]));
            f.read(shortPants);
            h.setSyscallCompatId(Ints.fromBytes(filler,filler,shortPants[0],shortPants[1]));
            byte[] trapsets = new byte[8];
            f.read(trapsets);
            h.setTrapSet(trapsets);
            h.setTrapSetStringlied(Utils.bytesToHex(trapsets));
            f.read(integer);
            h.setSizeFileInWords(Longs.fromBytes(filler,filler,filler,filler,integer[0],integer[1],integer[2],integer[3]));
            f.read(shortPants);
            h.setChksum(Shorts.fromBytes(shortPants[0],shortPants[1]));
            f.read(shortPants);
            h.setUnknown_parameter_b(Shorts.fromBytes(shortPants[0],shortPants[1]));
            f.read(integer);
            h.setEtcetcaddress(Longs.fromBytes(filler,filler,filler,filler,integer[0],integer[1],integer[2],integer[3]));

            f.read(shortPants);
            h.setUnknown_twiddled_bits(Shorts.fromBytes(shortPants[0],shortPants[1]));

            Yaml y = new Yaml();

            y.dump(h, new FileWriter(new File(output+File.separator+"header.yml")));

            f.seek(0);

            RandomAccessFile hdrRaw = new RandomAccessFile(output+File.separator+"header.bin","rw");
            byte[] header = new byte[0x30];
            f.read(header);
            hdrRaw.write(header);
            byte[] code = new byte[(int)h.getSizeCodeInWords()*2];
            f.read(code);
            RandomAccessFile codeRaw = new RandomAccessFile(output+File.separator+"code.bin","rw");
            codeRaw.write(code);

            byte[] constants = new byte[h.getSzConstantsInWords()*2];
            f.read(constants);
            RandomAccessFile dataRaw = new RandomAccessFile(output+File.separator+"data.bin","rw");
            dataRaw.write(constants);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
