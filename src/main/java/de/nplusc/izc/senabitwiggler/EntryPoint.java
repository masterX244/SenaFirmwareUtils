package de.nplusc.izc.senabitwiggler;

import java.io.*;

import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(mixinStandardHelpOptions = true, version = "Sena Firmware Hacking Utiility")
public class EntryPoint implements Runnable
{
    @Parameters(index = "0", description = "Mode for the Program. Valid values: ${COMPLETION-CANDIDATES}")
    private Modes mode;


    @Parameters(index = "1", description = "Firmware File to dissect/reassemble")
    private File input;

    @Parameters(index = "2", description = "Disassembled Data Folder")
    private File output;

    @Parameters(index = "3", arity = "0..1", description = "Headset ID. Any value if not in the Prompt unpacking mode")
    private String headset;

    @Option(names = { "-d", "--deep" }, description = "Deep Dissect. Splits everything down and reassembles from those low-level modules. Also yields a partial disassembly.")
    private boolean weNeedToGoDeeper;
    @Option(names = { "-v", "--verbose" }, description = "Snitch enabling")
    public static boolean verbose;



    public static String SoxPath = "";
    public static String BlueLabPath = "";

    public static final String APPDIR = new File(EntryPoint.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParent();

    public static void main(String[] args)
    {
        Yaml y = new Yaml();
        File cfg = new File(APPDIR+File.separator+"config.yml");
        if(cfg.exists())
        {
            try {
                Configuration config = y.loadAs(new FileReader(cfg),Configuration.class);
                SoxPath=config.getSoxPath();
                BlueLabPath=config.getBlueLabPath();
            } catch (FileNotFoundException e) {
                System.err.println("Hurz");
                e.printStackTrace();
            }
        }
        else
        {
            Configuration config = new Configuration();
            config.setBlueLabPath("C:\\ADK_CSR867x.WIN4.3.1.5\\tools\\bin\\");
            config.setSoxPath("sox");
            try {
                y.dump(config, new FileWriter(cfg));
                System.err.println("Configuration needed. Check the generated config.yml");
            } catch (IOException e) {
                System.err.println("Failed to initialize config");
                e.printStackTrace();
            }
            System.exit(0);
        }

        CommandLine cl = new CommandLine(new EntryPoint());
        cl.setCaseInsensitiveEnumValuesAllowed(true);
        cl.execute(args);
    }

    @Override
    public void run() {
        //TODO check and init config file
        switch(mode)
        {
            case ExtractSenaBin:
                try {
                    Utils.makeSureThatOutFolderIsCreated(output.getPath());
                    FirmwareWrapperExtraction.extractFirmwareLong(input,output.getPath());
                } catch (InputInvalidException e) {
                    System.out.println("Zarf! File was bad");
                    e.printStackTrace();
                }
                break;
            case ImportSenaBin:
                FirmwareWrapperExtraction.assembleFirmware(input,output.getPath());
                break;
            case ExtractVMImage:
                VmAppFIleExtraction.extractVmImage(input,output.getPath());
                break;
            case ImportVMImage:
                throw new UnsupportedOperationException("Not Implemented yet");
                //break;
            case DisassembleXAP:
                XAPDisAsm.Disassemble(input.getPath(),output.getPath());
                break;
            case ExtractForPrompts:
                try {
                    PromptHandlerSuite.handlePrompts(input,output,headset,weNeedToGoDeeper);
                } catch (InputInvalidException e) {
                    e.printStackTrace();
                }
                break;
            case ReassembleForPrompts:
                try {
                    PromptHandlerSuite.assembleWithNewPrompts(input,output,headset);
                } catch (InputInvalidException e) {
                    e.printStackTrace();
                }
                break;
            case Jailbreak:
                Jailbreaker.jailbreak();
                break;
            case DumpFlashes:
                Jailbreaker.dumpFlash(input.getName(),output);
                break;
            case ResignDFU:
                Jailbreaker.resignDFU(input.getPath(),output);
                break;
        }
    }
}

enum Modes
{
    ExtractSenaBin,
    ImportSenaBin,
    ExtractVMImage,
    ImportVMImage,
    DisassembleXAP,
    ExtractForPrompts,
    ReassembleForPrompts,
    Jailbreak,
    DumpFlashes,
    ResignDFU
}

// http://www.tinyosshop.com/download/ADK_CSR867x.WIN4.3.1.5.zip für die tools

// C:\ADK_CSR867x.WIN4.3.1.5\tools\bin\XUV2BIN.exe -e vp.bin vp.xuv

//C:\ADK_CSR867x.WIN4.3.1.5\tools\bin\ unpackfile.exe vp.xuv out
// resultat: out mit prm-dateien. bei SRL2 sind das raw-PCMs, Mono, 16bit 8khz. leider keine header zum rumverzinken....
// C:\ADK_CSR867x.WIN4.3.1.5\tools\bin\packfile.exe out vp2.xuv
// C:\ADK_CSR867x.WIN4.3.1.5\tools\bin\XUV2BIN.exe -d vp2.xuv vp2.bin
// danach die vp.bin mit vp2.bin austauschen und repacken



// für raw pcm: for f in *.prm; do sox -t raw -r 8000 -c 1 -e signed-integer -b 16 $f -e signed-integer -b 16 out2.$f.wav; done
// für ima adpcm: for f in *.prm; do sox -t ima -r 8000 -c 1 -e ima-adpcm -b 4 $f -e signed-integer -b 16 out.$f.wav; done
