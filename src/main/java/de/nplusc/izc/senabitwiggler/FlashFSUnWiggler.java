package de.nplusc.izc.senabitwiggler;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import de.nplusc.izc.senabitwiggler.DataStructures.QualCommHeaderRecord;
import de.nplusc.izc.senabitwiggler.DataStructures.QualCommWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlashFSUnWiggler {
    private static final Logger l = LogManager.getLogger();
    private static class FileMetadata
    {
        private int offset;
        private boolean is_dir;
        private int length;
        private int offset_fname;
        private String filename;
        private String parentpath;
    }
    public static void unpackFSQCC512x(File firmware, File outfolder)
    {
        try (RandomAccessFile f = new RandomAccessFile(firmware,"r")) {
            byte[] javaisDipshit = new byte[4];
            byte[] javaShorty = new byte[2];
            int magic = f.readInt(); //ignored
            f.read(javaisDipshit);
            int sizeFile = Ints.fromBytes(javaisDipshit[3],javaisDipshit[2],javaisDipshit[1],javaisDipshit[0]);
            if(sizeFile>f.length())
            {

                l.info(sizeFile);
                l.info("ZOINKS!!!, mismatch");
                return;
            }
            f.read(javaisDipshit);
            int countFileRecords = Ints.fromBytes(javaisDipshit[3],javaisDipshit[2],javaisDipshit[1],javaisDipshit[0]);
            FileMetadata[] files = new FileMetadata[countFileRecords];
            for(int i=0;i<countFileRecords;i++)
            {
                FileMetadata fmd = new FileMetadata();
                f.read(javaisDipshit);
                int sizeAndFlags = Ints.fromBytes(javaisDipshit[3],javaisDipshit[2],javaisDipshit[1],javaisDipshit[0]);
                int size = sizeAndFlags&0x00FFFFFF;
                l.info(sizeAndFlags);
                l.info(size);

                fmd.offset_fname=size;
                if((sizeAndFlags&0xFF000000)<0)
                {
                    l.info("dir");
                    fmd.is_dir=true;
                }
                f.read(javaisDipshit);
                fmd.offset=Ints.fromBytes(javaisDipshit[3],javaisDipshit[2],javaisDipshit[1],javaisDipshit[0]);
                f.read(javaisDipshit);
                fmd.length=Ints.fromBytes(javaisDipshit[3],javaisDipshit[2],javaisDipshit[1],javaisDipshit[0]);
                files[i]=fmd;
            }
            for(int i=0;i<countFileRecords;i++)
            {
                FileMetadata fmd = files[i];
                if(fmd.offset_fname==0)
                {
                    fmd.filename="";
                    fmd.parentpath="";
                }
                else
                {
                    f.seek(fmd.offset_fname);
                    f.read(javaShorty);
                    short fnlength = Shorts.fromBytes(javaShorty[1],javaShorty[0]);
                    byte[] fname = new byte[fnlength];
                    f.read(fname);
                    String s = File.separator+new String(fname);
                    fmd.filename=s;
                }
                if(fmd.is_dir)
                {
                    new File(outfolder+fmd.parentpath+fmd.filename).mkdirs();

                    for(int j= 0;j< fmd.length;j++)
                    {
                        int subfiles = fmd.offset+j-1;
                        files[subfiles].parentpath=fmd.parentpath+fmd.filename;
                    }
                }
                else
                {

                    l.info("Reading:"+fmd.parentpath+fmd.filename+"("+fmd.length+")@"+fmd.offset);
                    byte[] filecontent = new byte[fmd.length];
                    if(fmd.length>0)
                    {
                        f.seek(fmd.offset);
                        f.read(filecontent);

                        RandomAccessFile out = new RandomAccessFile(outfolder+fmd.parentpath+fmd.filename,"rw");
                        out.write(filecontent);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void unpackCSRFS(File firmware, File outfolder)
    {
        try (RandomAccessFile f = new RandomAccessFile(firmware,"r")) {
            int filler = -1;
            byte[] fillershort = new byte[]{(byte)0xff,(byte)0xff};
            RandomAccessFile boobs = new RandomAccessFile(new File(outfolder,"debug.dat"),"rw");

            byte[] javaisDipshit = new byte[4];
            byte[] javaShorty = new byte[2];
            int magic = f.readInt(); //ignored


            f.read(javaisDipshit);
            boobs.write(filler);
            int sizeFile = Ints.fromBytes(javaisDipshit[0],javaisDipshit[1],javaisDipshit[2],javaisDipshit[3]);
            //old shit calculates in words and not in bytes on sizes
            if((sizeFile*2)>f.length())
            {

                l.info(sizeFile);
                l.info("ZOINKS!!!, mismatch");
                return;
            }
            byte[] background = new byte[(int)f.length()];
            for(int i=0;i<background.length;i++)
            {
                background[i]=0;
            }
            boobs.seek(0);
            boobs.write(background);
            boobs.seek(0);
            boobs.write(filler);
            boobs.write(filler);



            f.read(javaisDipshit);
            int countFileRecords = (Ints.fromBytes(javaisDipshit[2],javaisDipshit[3],javaisDipshit[0],javaisDipshit[1]) &0x00FFFFFF);
            l.info(countFileRecords);
            int countFileRecordsXXX = (Ints.fromBytes(javaisDipshit[1],javaisDipshit[0],javaisDipshit[3],javaisDipshit[2])&0x00FFFFFF);
            l.info(countFileRecordsXXX);
            boobs.write(fillershort);
            boobs.seek(10);
            f.seek(10);
            byte[] fakeFileRecord = new byte[12];
            for(int i=0;i<12;i++)
            {
                fakeFileRecord[i]=(byte)0xee;
            }
            FileMetadata[] files = new FileMetadata[countFileRecords];
            for(int i=0;i<countFileRecords;i++)
            {
                FileMetadata fmd = new FileMetadata();
                f.read(javaisDipshit);
                int sizeAndFlags = Ints.fromBytes(javaisDipshit[0],javaisDipshit[1],javaisDipshit[2],javaisDipshit[3]);
                int size = sizeAndFlags&0x00FFFFFF;
                l.info("SZFlg>>"+sizeAndFlags);
                l.info("SZExtr>>"+size);

                fmd.offset_fname=size;
                if((sizeAndFlags&0xFF000000)<0)
                {
                    l.info("dir");
                    fmd.is_dir=true;
                }
                f.read(javaisDipshit);
                fmd.offset=Ints.fromBytes(javaisDipshit[0],javaisDipshit[1],javaisDipshit[2],javaisDipshit[3]);
                f.read(javaisDipshit);
                fmd.length=Ints.fromBytes(javaisDipshit[0],javaisDipshit[1],javaisDipshit[2],javaisDipshit[3]);
                files[i]=fmd;
                boobs.write(fakeFileRecord);
            }
            for(int i=0;i<countFileRecords;i++)
            {
                FileMetadata fmd = files[i];
                if(fmd.offset_fname==0)
                {
                    fmd.filename="";
                    fmd.parentpath="";
                }
                else
                {
                    f.seek(fmd.offset_fname*2); //words again...
                    f.read(javaShorty);
                    short fnlength = Shorts.fromBytes(javaShorty[0],javaShorty[1]);
                    byte[] fname = new byte[fnlength*2];
                    f.read(fname);
                    byte[] fname_real = new byte[fnlength];
                    for(int fi=0;fi<fname_real.length;fi++)
                    {
                        fname_real[fi]=fname[fi*2+1]; //zapping each upper bit...
                    }
                    for(int j=0;j<fname.length;j++)
                    {
                        fname[j]=(byte) 0xCC;
                    }
                    boobs.seek(fmd.offset_fname*2); //words again...
                    boobs.write(new byte[]{(byte)0xDD,(byte)0xDD});
                    boobs.write(fname);
                    String s = File.separator+new String(fname_real);
                    fmd.filename=s;
                }
                if(fmd.is_dir)
                {
                    new File(outfolder+fmd.parentpath+fmd.filename).mkdirs();

                    for(int j= 0;j< fmd.length;j++)
                    {
                        int subfiles = fmd.offset+j-1;
                        if(subfiles==-1)
                        {
                            l.info("WTF?");
                        }
                        files[subfiles].parentpath=fmd.parentpath+fmd.filename;
                    }
                }
                else
                {

                    l.info("Reading:"+fmd.parentpath+fmd.filename+"("+fmd.length+")@"+fmd.offset);
                    byte[] filecontent = new byte[fmd.length];
                    if(fmd.length>0)
                    {
                        f.seek(fmd.offset*2);
                        int readme = f.read(filecontent);
                        RandomAccessFile out = new RandomAccessFile(outfolder+fmd.parentpath+fmd.filename,"rw");
                        out.write(filecontent);
                        if(readme<filecontent.length)
                        {
                            throw new IndexOutOfBoundsException("ZARF");
                        }
                        for(int k=0;k<filecontent.length;k++)
                        {
                            filecontent[k]=(byte)0xBB;
                        }
                        boobs.seek(fmd.offset*2);
                        boobs.write(filecontent);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public static void unpackQCC512DFU(File firmware, File outfolder)
    {
        QualCommWrapper w = new QualCommWrapper();
        l.info("0x00!");
        try (RandomAccessFile f = new RandomAccessFile(firmware,"r"))
        {
            outfolder.mkdirs();
            f.read(new byte[8]); //skip header
            int hdrsize = f.readInt();
            byte[] header = new byte[hdrsize];
            f.read(header);
            w.header=header;
            // 1346458196
            // 1145132097
            int i = 0;
            List<QualCommHeaderRecord> files = new ArrayList<>();
            while(true)
            {
                QualCommHeaderRecord r = new QualCommHeaderRecord();
                if(f.readInt() != 1346458196)
                {
                    l.info("Zarf!");
                    break; //end of file
                }
                if(f.readInt() != 1145132097)
                {
                    l.info("Zarf2!");
                    break; //end of file
                }
                int sizeData = f.readInt();
                r.size = sizeData;
                l.info("Reading" +sizeData);
                short location = f.readShort();
                short sublocation = f.readShort();
                r.location=location;
                r.sublocation=sublocation;
                String filename = location+"_"+sublocation+".dat";
                r.filename=filename;
                byte[] innerFile = new byte[sizeData-4];

                f.read(innerFile);
                RandomAccessFile fo = new RandomAccessFile(new File(outfolder,filename),"rw");
                fo.write(innerFile);
                fo.close();
                i++;
                files.add(r);
            }
            int bytesLeft = 4+((int)(f.length()-f.getFilePointer()));
            byte[] rest = new byte[bytesLeft];
            f.seek(f.getFilePointer()-4);
             f.read(rest);
             w.footer=rest;

            w.files = files.toArray(new QualCommHeaderRecord[files.size()]);
            Yaml y = new Yaml();

            y.dump(w, new FileWriter(new File(outfolder,"header.yml")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void repackQualcommWrapper(File newfirmware, File inputfolder)
    {
        QualCommWrapper header;
        try {
             header = new Yaml().loadAs(new FileReader(new File(inputfolder,File.separator+"header.yml")),QualCommWrapper.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        l.info("0x00!");

        if(newfirmware.exists())
        {
            l.error("Refusing to overwrite a existing file!!!");
            return;
        }

        try (RandomAccessFile f = new RandomAccessFile(newfirmware,"rw"))
        {
            f.writeInt(0x41505055);
            f.writeInt(0x48445232);
            f.writeInt(header.header.length);
            f.write(header.header);

            // 1346458196
            // 1145132097

            for(QualCommHeaderRecord r:header.files)
            {
                RandomAccessFile f2 = new RandomAccessFile(new File(inputfolder,r.filename),"r");
                int size = (int) f2.length();
                byte[] file = new byte[size];
                f2.read(file);
                f.writeInt(1346458196);
                f.writeInt(1145132097);
                f.writeInt(size+4);
                f.writeShort(r.location);
                f.writeShort(r.sublocation);
                f.write(file);
                f2.close();
            }
            f.write(header.footer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }







}
