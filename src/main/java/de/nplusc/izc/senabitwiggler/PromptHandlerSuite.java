package de.nplusc.izc.senabitwiggler;

import org.yaml.snakeyaml.Yaml;

import javax.sound.midi.Patch;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PromptHandlerSuite {
    public static void handlePrompts(File firmware, File outfolder, String headsetid,boolean deep) throws InputInvalidException {
        URL rsrc = EntryPoint.class.getResource("/PromptConfigs/"+headsetid+".prompts.yml");
        if(rsrc==null)
        {
            URL url = EntryPoint.class.getResource("/PromptConfigs/");
            // HAXX
            EntryPoint.class.getResourceAsStream("/PromptConfigs/");
            Path path = null;
            try {
                path = Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            System.out.println("Invalid Headset reference: Valid values are");
            try {
                Files.walk(path, 1).forEach(p ->
                {
                    String fn = p.getFileName().toString();
                    if(fn.endsWith(".prompts.yml"))
                    {
                        System.out.println(fn.replace(".prompts.yml",""));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            throw new InputInvalidException();
        }
        File scratch = new File(outfolder,"internal");
        File prompts = new File(outfolder,"prompts");
        File firmwaredir = new File(outfolder,"firmware");
        File exe = new File(outfolder,"exe");
        scratch.mkdirs();
        prompts.mkdirs();
        firmwaredir.mkdirs();
        exe.mkdirs();
        File SenaUnwrap = new File(scratch,"senatransport");
        File DFUUnwrap = new File(scratch,"dfuunwrap");
        File PromptsInPrm = new File(scratch,"prm");
        SenaUnwrap.mkdirs();
        DFUUnwrap.mkdirs();
        PromptsInPrm.mkdirs();
        FirmwareWrapperExtraction.extractFirmwareLong(firmware,SenaUnwrap.getPath());




        try (InputStream in = EntryPoint.class.getResourceAsStream("/PromptConfigs/"+headsetid+".prompts.yml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            HashMap<String,String> config = (HashMap<String, String>) new Yaml().load(reader);
            String file = config.get("file");
            File xuvin = new File(SenaUnwrap,file);
            File xuvout = new File(scratch,file+".xuv");
            String xuv2bin = EntryPoint.BlueLabPath+"xuv2bin";
            String unpacker = EntryPoint.BlueLabPath+"unpackfile";
            runTool(xuv2bin,"-e", xuvin.getPath(), xuvout.getPath());
            runTool(unpacker, xuvout.getPath(), PromptsInPrm.getPath());

            if(deep)
            {
                //TODO array for multiples!!
                File dfu = new File(SenaUnwrap,config.get("maincpu"));
                runTool(DFUUnwrap,EntryPoint.BlueLabPath+"dfuunbuild","-v","-f", dfu.getAbsolutePath(), "-o", "unwrap");
                runTool(EntryPoint.BlueLabPath+"unpackfile", new File(DFUUnwrap,"unwrap0000.fs").getPath(),firmwaredir.getPath());
                File fwXuv = new File(firmwaredir,"vm.app");
                File fwUnXuv = new File(scratch,"exe.unxuv");
                runTool(xuv2bin,"-d", fwXuv.getPath(), fwUnXuv.getPath());

                VmAppFIleExtraction.extractVmImage(fwUnXuv, exe.getPath());


                XAPDisAsm.Disassemble(new File(exe,"code.bin").getPath(),new File(exe,"app.disasm").getPath());
            }


            File[] fileprompts = PromptsInPrm.listFiles();
            for(File f : fileprompts)
            {
                String fn = f.getName().replace(".prm","");
                String mode = config.get("ALL");
                if(config.containsKey(fn))
                {
                    mode = config.get(fn);
                }
                List<String> soxIn = new ArrayList<>();
                switch(mode.toLowerCase(Locale.ROOT))
                {
                    case "pcm":

                        soxIn = Arrays.asList(new String[]{"-t","raw", "-r","8000","-c","1","-e","signed-integer","-b", "16"});
                        break;
                    case "ima":
                        soxIn = Arrays.asList(new String[]{"-t","ima", "-r","8000","-c","1","-e","ima-adpcm","-b","4"});
                        break;
                    default:
                        throw new InputInvalidException();
                }
                List<String> cmd = new ArrayList<>();
                cmd.add("sox");
                cmd.addAll(soxIn);
                cmd.add(f.getPath());
                cmd.add("-e");
                cmd.add("signed-integer");
                cmd.add("-b 16");
                cmd.add(new File(prompts,fn+".wav").getPath());

                runTool(cmd.toArray(new String[0]));


                // für raw pcm: for f in *.prm; do sox -t raw -r 8000 -c 1 -e signed-integer -b 16 $f -e signed-integer -b 16 out2.$f.wav; done
                // für ima adpcm: for f in *.prm; do sox -t ima -r 8000 -c 1 -e ima-adpcm -b 4 $f -e signed-integer -b 16 out.$f.wav; done
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void assembleWithNewPrompts(File firmware, File outfolder, String headsetid) throws InputInvalidException {
        {
            URL rsrc = EntryPoint.class.getResource("/PromptConfigs/" + headsetid + ".prompts.yml");
            if (rsrc == null) {
                URL url = EntryPoint.class.getResource("/PromptConfigs/");
                // HAXX
                EntryPoint.class.getResourceAsStream("/PromptConfigs/");
                Path path = null;
                try {
                    path = Paths.get(url.toURI());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                System.out.println("Invalid Headset reference: Valid values are");
                try {
                    Files.walk(path, 1).forEach(p ->
                    {
                        String fn = p.getFileName().toString();
                        if (fn.endsWith(".prompts.yml")) {
                            System.out.println(fn.replace(".prompts.yml", ""));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                throw new InputInvalidException();
            }

            File scratch = new File(outfolder,"internal");
            File prompts = new File(outfolder,"prompts");
            scratch.mkdirs();
            prompts.mkdirs();
            File SenaUnwrap = new File(scratch,"senatransport");
            File DFUUnwrap = new File(scratch,"dfuunwrap"); //Needed for some not yet supported variants
            File PromptsInPrm = new File(scratch,"prm"); //Needed for some not yet supported variants
            SenaUnwrap.mkdirs();
            DFUUnwrap.mkdirs();
            PromptsInPrm.mkdirs();





            try (InputStream in = EntryPoint.class.getResourceAsStream("/PromptConfigs/"+headsetid+".prompts.yml");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                HashMap<String,String> config = (HashMap<String, String>) new Yaml().load(reader);
                String file = config.get("file");
                File xuvin = new File(SenaUnwrap,file);
                File xuvout = new File(scratch,file+".xuv");
                String xuv2bin = EntryPoint.BlueLabPath+"xuv2bin";
                String unpacker = EntryPoint.BlueLabPath+"packfile";
                File[] fileprompts = PromptsInPrm.listFiles();
                for(File f : fileprompts)
                {
                    String fn = f.getName().replace(".prm","");
                    String mode = config.get("ALL");
                    if(config.containsKey(fn))
                    {
                        mode = config.get(fn);
                    }
                    List<String> soxIn = new ArrayList<>();
                    switch(mode.toLowerCase(Locale.ROOT))
                    {
                        case "pcm":

                            soxIn = Arrays.asList(new String[]{"-t","raw", "-r","8000","-c","1","-e","signed-integer","-b", "16"});
                            break;
                        case "ima":
                            soxIn = Arrays.asList(new String[]{"-t","ima", "-r","8000","-c","1","-e","ima-adpcm","-b","4"});
                            break;
                        default:
                            throw new InputInvalidException();
                    }
                    List<String> cmd = new ArrayList<>();
                    cmd.add("sox");
                    cmd.add(new File(prompts,fn+".wav").getPath());
                    cmd.addAll(soxIn);
                    cmd.add(f.getPath());

                    runTool(cmd.toArray(new String[0]));


                    // für raw pcm: for f in *.prm; do sox -t raw -r 8000 -c 1 -e signed-integer -b 16 $f -e signed-integer -b 16 out2.$f.wav; done
                    // für ima adpcm: for f in *.prm; do sox -t ima -r 8000 -c 1 -e ima-adpcm -b 4 $f -e signed-integer -b 16 out.$f.wav; done
                }


                runTool(unpacker, PromptsInPrm.getPath(),xuvout.getPath());
                runTool(xuv2bin,"-d", xuvout.getPath(),xuvin.getPath());

                FirmwareWrapperExtraction.assembleFirmware(firmware,SenaUnwrap.getPath());

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


    private static void runTool(String... args) throws IOException {

        runTool(null,args);
    }



    private static void runTool(File workdir, String... args) throws IOException {
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
