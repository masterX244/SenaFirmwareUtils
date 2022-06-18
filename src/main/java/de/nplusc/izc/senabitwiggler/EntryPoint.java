package de.nplusc.izc.senabitwiggler;

import java.io.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(mixinStandardHelpOptions = true, version = "Sena Firmware Hacking Utiility")
public class EntryPoint implements Runnable
{
    private static final Logger l = LogManager.getLogger();

    @Parameters(index = "0", description = "Mode for the Program. Valid values: ${COMPLETION-CANDIDATES}")
    private Modes mode;


    @Parameters(index = "1", description = "Firmware File to dissect/reassemble")
    private File input;

    @Parameters(index = "2", description = "Disassembled Data Folder")
    private File output;

    @Parameters(index = "3", arity = "0..1", description = "Headset ID. Any value if not in the Prompt unpacking mode.")
    private String headset;

    @Option(names = { "-d", "--deep" }, description = "Deep Dissect. Splits everything down and reassembles from those low-level modules. Also yields a partial disassembly.")
    private boolean weNeedToGoDeeper;
    @Option(names = { "-v", "--verbose" }, description = "Snitch enabling")
    public static boolean verbose;



    public static String SoxPath = "";
    public static String BlueLabPath = "";

    public static Configuration c;

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
                c=config;
                y.dump(config, new FileWriter(cfg));
            } catch (IOException e) {
                l.catching(e);
            }
        }
        else
        {
            Configuration config = new Configuration();
            config.setBlueLabPath("C:\\ADK_CSR867x.WIN4.3.1.5\\tools\\bin\\");
            config.setSoxPath("sox");
            try {
                y.dump(config, new FileWriter(cfg));
                l.error("Configuration needed. Check the generated config.yml");
            } catch (IOException e) {
                l.error("Failed to initialize config");
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
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager"); //HACK to catch java.util.logging loggers
        LoggerContext cx = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = cx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        if (verbose)
        {
            loggerConfig.setLevel(Level.TRACE);
        }
        else
        {
            loggerConfig.setLevel(Level.INFO);
        }


        switch(mode)
        {
            case ExtractSenaBin:
                try {
                    Utils.makeSureThatOutFolderIsCreated(output.getPath());
                    FirmwareWrapperExtraction.extractFirmwareLong(input,output.getPath());
                } catch (InputInvalidException e) {
                    l.error("Zarf! File was bad");
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
            case FlashFS512x:
                FlashFSUnWiggler.unpackFSQCC512x(input,output);
                break;
            case FlashFSCSR86xx:
                FlashFSUnWiggler.unpackCSRFS(input,output);
                break;
            case DfuS512x:
                FlashFSUnWiggler.unpackQCC512DFU(input,output);
                break;
            case ScanForSenaFirmware:
                FirmwareAutoDumper.pullFirmwares(input,output,weNeedToGoDeeper);
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
    ResignDFU,
    FlashFS512x,
    FlashFSCSR86xx,
    DfuS512x,
    ScanForSenaFirmware
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
