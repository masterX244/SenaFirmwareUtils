package de.nplusc.izc.senabitwiggler;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class FlashFSUnWiggler {
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

                System.out.println(sizeFile);
                System.err.println("ZOINKS!!!, mismatch");
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
                System.out.println(sizeAndFlags);
                System.out.println(size);

                fmd.offset_fname=size;
                if((sizeAndFlags&0xFF000000)<0)
                {
                    System.out.println("dir");
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

                    System.out.println("Reading:"+fmd.parentpath+fmd.filename+"("+fmd.length+")@"+fmd.offset);
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

                System.out.println(sizeFile);
                System.err.println("ZOINKS!!!, mismatch");
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
            System.out.println(countFileRecords);
            int countFileRecordsXXX = (Ints.fromBytes(javaisDipshit[1],javaisDipshit[0],javaisDipshit[3],javaisDipshit[2])&0x00FFFFFF);
            System.out.println(countFileRecordsXXX);
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
                System.out.println("SZFlg>>"+sizeAndFlags);
                System.out.println("SZExtr>>"+size);

                fmd.offset_fname=size;
                if((sizeAndFlags&0xFF000000)<0)
                {
                    System.out.println("dir");
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
                            System.out.println("WTF?");
                        }
                        files[subfiles].parentpath=fmd.parentpath+fmd.filename;
                    }
                }
                else
                {

                    System.out.println("Reading:"+fmd.parentpath+fmd.filename+"("+fmd.length+")@"+fmd.offset);
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
        System.out.println("0x00!");
        try (RandomAccessFile f = new RandomAccessFile(firmware,"r"))
        {
            outfolder.mkdirs();
            f.read(new byte[8]); //skip header
            int hdrsize = f.readInt();
            f.read(new byte[hdrsize]);
            // 1346458196
            // 1145132097
            int i = 0;
            while(true)
            {
                if(f.readInt() != 1346458196)
                {
                    System.out.println("Zarf!");
                    break; //end of file
                }
                if(f.readInt() != 1145132097)
                {
                    System.out.println("Zarf2!");
                    break; //end of file
                }
                int sizeData = f.readInt();
                System.out.println("Reading" +sizeData);
                byte[] innerFile = new byte[sizeData];
                f.read(innerFile);
                RandomAccessFile fo = new RandomAccessFile(new File(outfolder,i+".dat"),"rw");
                fo.write(innerFile);
                fo.close();
                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
