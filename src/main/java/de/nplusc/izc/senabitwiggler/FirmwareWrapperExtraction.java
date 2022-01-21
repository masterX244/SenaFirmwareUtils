package de.nplusc.izc.senabitwiggler;

import com.google.common.base.Charsets;
import com.google.common.primitives.Longs;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FirmwareWrapperExtraction {
    public static void assembleFirmware(File output, String inputfolder)
    {
        Yaml y = new Yaml();
        try {
            LongHeader h = y.loadAs(new FileReader(new File(inputfolder+File.separator+"header.yml")),LongHeader.class);
            RandomAccessFile f = new RandomAccessFile(output,"rw");
            f.write(h.getVersion_raw());
            f.write(Utils.LongToRawBytes(h.getMagicShit()));
            f.write(Utils.LongToRawBytes(h.getRandom_id()));
            HeaderRecord[] records = h.getHeaderRecords();
            int count = records.length;
            f.write(Utils.LongToRawBytes(count));
            long offset = records[0].getOffset();
            for(int i=0;i<count;i++)
            {
                HeaderRecord r = records[i];
                File handle = new File(inputfolder+File.separator+r.getFilename());
                r.setLength(handle.length());
                r.setOffset(offset+0);
                offset+=handle.length();

                RandomAccessFile file = new RandomAccessFile(handle,"r");
                byte[] bfr = new byte[(int)handle.length()];
                file.read(bfr);
                byte[] newmd5 = MessageDigest.getInstance("MD5").digest(bfr);
                r.setMd5sum(newmd5);
            }

            for(int i=0;i<count;i++)
            {
                HeaderRecord r = records[i];
                f.write(Utils.LongToRawBytes(r.getShortflag_1()));
                f.write(Utils.LongToRawBytes(r.getShortflag_2()));
                f.write(Utils.LongToRawBytes(r.getOffset()));
                f.write(Utils.LongToRawBytes(r.getLength()));
                f.write(r.getMd5sum());
            }

            for(int i=0;i<count;i++)
            {
                HeaderRecord r = records[i];
                f.write(Utils.LongToRawBytes(r.getShortflag_1()));
                f.write(Utils.LongToRawBytes(r.getShortflag_2()));
                f.write(Utils.LongToRawBytes(r.getOffset()));
                f.write(Utils.LongToRawBytes(r.getLength()));
                f.write(Utils.LongToRawBytes(r.getFlag_1()));
                f.write(Utils.LongToRawBytes(r.getFlag_2()));
                f.write(Utils.LongToRawBytes(r.getFlag_3()));
                f.write(Utils.LongToRawBytes(r.getFlag_4()));
                f.write(Utils.LongToRawBytes(r.getFlag_5()));
                f.write(Utils.LongToRawBytes(r.getUnknown_id()));
                f.write(r.getPadding());

                byte[] filenamebytes = r.getFilename().getBytes(Charsets.US_ASCII);
                byte[] filename = new byte[128];
                for(int j=0;j<128;j++)
                {
                    if(j<filenamebytes.length)
                    {
                        filename[j]=filenamebytes[j];
                    }
                    else
                    {
                        filename[j]=0;
                    }
                }
                f.write(filename);
                f.write(r.getMd5sum());
            }

            for(int i=0;i<count;i++)
            {
                HeaderRecord r = records[i];
                File handle = new File(inputfolder+File.separator+r.getFilename());
                RandomAccessFile file = new RandomAccessFile(handle,"r");
                byte[] bfr = new byte[(int)handle.length()];
                file.read(bfr);
                f.write(bfr);
            }
            f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void extractFirmwareLong(File input, String output) throws InputInvalidException {
        byte filler = 0x00;



        try (RandomAccessFile f = new RandomAccessFile(input,"r")) {
            LongHeader hdr = new LongHeader();
            byte[] headermagic = new byte[24];
            f.read(headermagic);
            String versionName = new String(headermagic, Charsets.US_ASCII).split("\0")[0]; //hack, can be done since strings are shorty. https://stackoverflow.com/a/8843313/1405227
            hdr.setVersion_raw(headermagic);
            hdr.setVersion(versionName);
            //12 more bytes
            byte[] magic = new byte[4];
            f.read(magic);

            hdr.setMagicShit(Longs.fromBytes(filler,filler,filler,filler,magic[3],magic[2],magic[1],magic[0]));

            byte[] unknown_id = new byte[4];

            f.read(unknown_id);

            hdr.setRandom_id(Longs.fromBytes(filler,filler,filler,filler,unknown_id[3],unknown_id[2],unknown_id[1],unknown_id[0]));

            byte[] count_raw = new byte[4];
            f.read(count_raw);

            long count_l = (Longs.fromBytes(filler,filler,filler,filler,count_raw[3],count_raw[2],count_raw[1],count_raw[0]));

            if(count_l>Integer.MAX_VALUE)
            {
                throw new InputInvalidException();
            }
            int count = (int)count_l;
            HeaderRecord[] records = new HeaderRecord[count];
            byte[] skip = new byte[count*32]; /*skipping over shorty header, repeats content with long one*/
            f.read(skip);
            for(int i=0;i<count;i++)
            {
                HeaderRecord record = new HeaderRecord();


                byte[] flags1 = new byte[4];
                f.read(flags1);

                record.setShortflag_1(Longs.fromBytes(filler,filler,filler,filler,flags1[3],flags1[2],flags1[1],flags1[0]));

                byte[] flags2 = new byte[4];
                f.read(flags2);

                record.setShortflag_2(Longs.fromBytes(filler,filler,filler,filler,flags2[3],flags2[2],flags2[1],flags2[0]));

                byte[] offset = new byte[4];
                f.read(offset);

                record.setOffset(Longs.fromBytes(filler,filler,filler,filler,offset[3],offset[2],offset[1],offset[0]));

                byte[] length = new byte[4];
                f.read(length);

                record.setLength(Longs.fromBytes(filler,filler,filler,filler,length[3],length[2],length[1],length[0]));



                long[] flags = new long[5];
                for(int j=0;j<5;j++)
                {
                    byte[] flags_raw = new byte[4];
                    f.read(flags_raw);

                    flags[j] = (Longs.fromBytes(filler,filler,filler,filler,flags_raw[3],flags_raw[2],flags_raw[1],flags_raw[0]));
                }

                record.setFlag_1(flags[0]);
                record.setFlag_2(flags[1]);
                record.setFlag_3(flags[2]);
                record.setFlag_4(flags[3]);
                record.setFlag_5(flags[4]);

                byte[] unknown = new byte[4];
                f.read(unknown);

                record.setUnknown_id(Longs.fromBytes(filler,filler,filler,filler,unknown[3],unknown[2],unknown[1],unknown[0]));

                byte[] padding = new byte[128];

                f.read(padding);

                record.setPadding(padding);

                byte[] filename_raw = new byte[128];

                f.read(filename_raw);

                String filename = new String(filename_raw, Charsets.US_ASCII).split("\0")[0]; //hack, can be done since strings are shorty. https://stackoverflow.com/a/8843313/1405227
                record.setFilename(filename);

                byte[] md5 = new byte[16];

                f.read(md5);

                record.setMd5sum(md5);

                records[i]=record;

            }
            hdr.setHeaderRecords(records);
            Yaml y = new Yaml();

            y.dump(hdr, new FileWriter(new File(output+File.separator+"header.yml")));

            for(int i=0;i<records.length;i++)
            {
                HeaderRecord r = records[i];
                String target = output+File.separator+r.getFilename();
                long offset = r.getOffset();
                int len = (int)r.getLength();
                byte[] file = new byte[len];
                f.seek(offset);
                f.read(file);
                RandomAccessFile out = new RandomAccessFile(target,"rw");
                out.write(file);
                out.close();

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
