package de.nplusc.izc.senabitwiggler;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

interface OpCodeMangler
{
    String mangleOpCode(short modifier, short[] opcode, int startAddress);
}

public class XAPDisAsm {
    private static final Logger l = LogManager.getLogger();
    private static HashMap<String,String> labels = new HashMap<>();

    private static OpCodeMangler brxl = (modifier,opcode,address)->{
        return OpcodeAddressRangeToString(address,1)+": brxl";
    };
    private static OpCodeMangler invalid =  (modifier,opcode,address)->{
        String badOpCode ="";
        boolean modified=false;
        if(modifier!=0x00)
        {
            modified=true;
            badOpCode += (Utils.bytesToHex(Shorts.toByteArray(modifier)));
        };
        for(int i=0;i<opcode.length;i++)
        {
            badOpCode += (Utils.bytesToHex(Shorts.toByteArray(opcode[i])));
        }
        String invalidCode = OpcodeAddressRangeToString(address,opcode.length+(modified?1:0))+":Invalid OpCode: "+badOpCode;
        l.info(invalidCode);
        return invalidCode;
    };
    public static OpCodeMangler[] manglers = new OpCodeMangler[256];
        static {


            OpCodeMangler ld =  (modifier,opcode,address)->{
                if(modifier!=0x00)
                {
                    //return invalid.mangleOpCode(modifier,opcode,address);
                };

                byte[] opcodeValues = new byte[opcode.length];
                byte opcodeReal = 0x00; // only checking lower byte though
                for(int i=0;i<opcode.length;i++)
                {
                    byte[] opcodeWord = Shorts.toByteArray(opcode[i]);
                    l.trace(opcodeWord[0]);
                    opcodeValues[i]=opcodeWord[0];
                    opcodeReal=(opcodeWord[1]); //stompy stomp, only keeps the last
                }
                String rawValue = Utils.bytesToHex(opcodeValues);
                l.trace(rawValue);

                int valueUnsigned = XAPDisAsmGeneratedCode.unsignedCounts.get(rawValue);
                int valueSigned =XAPDisAsmGeneratedCode.signedCounts.get(rawValue);
                String register = "";

                String value = "";
                switch(opcodeReal)
                {
                    case 0x0e:
                        register="xh";
                        value = ParamManglerIndirectY(valueSigned);
                        break;


                    case 0x10:
                        register="AH";
                        value = ParamManglerImmediate(valueUnsigned);
                        break;
                    case 0x11:
                        register="AH";
                        value = ParamManglerAddress(valueUnsigned);
                        break;
                    case 0x12:
                        register="AH";
                        value = ParamManglerIndirectX(valueSigned);
                        break;
                    case 0x13:
                        register="AH";
                        value = ParamManglerIndirectY(valueSigned);
                        break;


                    case 0x14:
                        register="AL";
                        value = ParamManglerImmediate(valueUnsigned);
                        break;
                    case 0x15:
                        register="AL";
                        value = ParamManglerAddress(valueUnsigned);
                        break;
                    case 0x16:
                        register="AL";
                        value = ParamManglerIndirectX(valueSigned);
                        break;
                    case 0x17:
                        register="AL";
                        value = ParamManglerIndirectY(valueSigned);
                        break;

                    case 0x18:
                        register="X";
                        value = ParamManglerImmediate(valueUnsigned);
                        break;
                    case 0x19:
                        register="X";
                        value = ParamManglerAddress(valueUnsigned);
                        break;
                    case 0x1a:
                        register="X";
                        value = ParamManglerIndirectX(valueSigned);
                        break;
                    case 0x1b:
                        register="X";
                        value = ParamManglerIndirectY(valueSigned);
                        break;

                    case 0x1c:
                        register="Y";
                        value = ParamManglerImmediate(valueUnsigned);
                        break;
                    case 0x1d:
                        register="Y";
                        value = ParamManglerAddress(valueUnsigned);
                        break;
                    case 0x1e:
                        register="Y";
                        value = ParamManglerIndirectX(valueSigned);
                        break;
                    case 0x1f:
                        register="Y";
                        value = ParamManglerIndirectY(valueSigned);
                        break;
                }
                return OpcodeAddressRangeToString(address,opcode.length)+":ld: "+register+" "+value;
            };

            OpCodeMangler st =  (modifier,opcode,address)->{
                if(modifier!=0x00)
                {
                    //return invalid.mangleOpCode(modifier,opcode,address);
                };

                byte[] opcodeValues = new byte[opcode.length];
                byte opcodeReal = 0x00; // only checking lower byte though
                for(int i=0;i<opcode.length;i++)
                {
                    byte[] opcodeWord = Shorts.toByteArray(opcode[i]);
                    opcodeValues[i]=opcodeWord[0];
                    opcodeReal=(opcodeWord[1]); //stompy stomp, only keeps the last
                }
                String rawVal = Utils.bytesToHex(opcodeValues);
                l.trace(rawVal);
                int valueUnsigned = XAPDisAsmGeneratedCode.unsignedCounts.get(rawVal);
                int valueSigned =XAPDisAsmGeneratedCode.signedCounts.get(Utils.bytesToHex(opcodeValues));
                String register = "";

                String value = "";
                switch(opcodeReal)
                {
                    case 0x0a:
                        register="xh";
                        value = ParamManglerIndirectY(valueSigned);
                        break;


                    case 0x21:
                        register="AH";
                        value = ParamManglerAddress(valueUnsigned);
                        break;
                    case 0x22:
                        register="AH";
                        value = ParamManglerIndirectX(valueSigned);
                        break;
                    case 0x23:
                        register="AH";
                        value = ParamManglerIndirectY(valueSigned);
                        break;


                    case 0x25:
                        register="AL";
                        value = ParamManglerAddress(valueUnsigned);
                        break;
                    case 0x26:
                        register="AL";
                        value = ParamManglerIndirectX(valueSigned);
                        break;
                    case 0x27:
                        register="AL";
                        value = ParamManglerIndirectY(valueSigned);
                        break;

                    case 0x29:
                        register="X";
                        value = ParamManglerAddress(valueUnsigned);
                        break;
                    case 0x2a:
                        register="X";
                        value = ParamManglerIndirectX(valueSigned);
                        break;
                    case 0x2b:
                        register="X";
                        value = ParamManglerIndirectY(valueSigned);
                        break;

                    case 0x2d:
                        register="Y";
                        value = ParamManglerAddress(valueUnsigned);
                        break;
                    case 0x2e:
                        register="Y";
                        value = ParamManglerIndirectX(valueSigned);
                        break;
                    case 0x2f:
                        register="Y";
                        value = ParamManglerIndirectY(valueSigned);
                        break;
                }
                return OpcodeAddressRangeToString(address,opcode.length)+":st: "+register+" "+value;
            };

            OpCodeMangler add = GetBasicMangler("add",(short)0x30);
            OpCodeMangler nadd = GetBasicMangler("nadd",(short)0x70);
            OpCodeMangler addc = GetBasicMangler("addc",(short)0x40);
            OpCodeMangler sub = GetBasicMangler("sub",(short)0x50);
            OpCodeMangler subc = GetBasicMangler("sub",(short)0x60);
            OpCodeMangler or = GetBasicMangler("or",(short)0xb0);
            OpCodeMangler and = GetBasicMangler("and",(short)0xc0);
            OpCodeMangler xor = GetBasicMangler("xor",(short)0xd0);
            OpCodeMangler cmp = GetBasicMangler("cmp",(short)0x80);
            OpCodeMangler bra = GetBraMangler("bra",(short)0xe0,true);
            OpCodeMangler blt = GetBraMangler("blt",(short)0xe4,false);
            OpCodeMangler bne = GetBraMangler("bne",(short)0xf0,false);
            OpCodeMangler beq = GetBraMangler("beq",(short)0xf4,false);
            OpCodeMangler bcc = GetBraMangler("bcc",(short)0xf8,false);
            OpCodeMangler bcs = GetBraMangler("bcs",(short)0xfc,false);
            OpCodeMangler tst = GetBraMangler("tst",(short)0x98,false);
            OpCodeMangler bsr = GetBraMangler("bsr",(short)0x9c,false);


            OpCodeMangler xmult = GetModifiedMangler("smult","umult",(short)0x90);
            OpCodeMangler xdiv = GetModifiedMangler("sdiv","udiv",(short)0x94);
            OpCodeMangler xsr = GetModifiedMangler("asr","lsr",(short)0xA4);
            OpCodeMangler xsl = GetModifiedMangler("asl","lsl",(short)0xA0);


            OpCodeMangler enterlleavel = (modifier,opcode,address)->{
                if(modifier!=0x00)
                {
                    //return invalid.mangleOpCode(modifier,opcode,address);
                };
                byte[] opcodeValues = new byte[opcode.length];
                short opcodeReal = 0x00; // only checking lower byte though
                for(int i=0;i<opcode.length;i++)
                {
                    byte[] opcodeWord = Shorts.toByteArray(opcode[i]);
                    opcodeValues[i]=opcodeWord[0];
                    opcodeReal=(short)((opcode[i]-0x00)&0xFF); //stompy stomp, only keeps the last
                }

                int valueStack = XAPDisAsmGeneratedCode.unsignedCounts.get(Utils.bytesToHex(opcodeValues));
                String label = "";

                String value = "";
                switch(opcodeReal)
                {
                    case 0x0b:
                        label="enterl";
                        break;
                    case 0x0f:
                        label="leavel";
                        break;
                }

                return OpcodeAddressRangeToString(address,opcode.length)+":"+label+":"+ParamManglerImmediate(valueStack);
            };

            OpCodeMangler bxx = (modifier,opcode,address)->{
                if(modifier!=0x00)
                {
                    //return invalid.mangleOpCode(modifier,opcode,address);
                };
                byte[] opcodeValues = new byte[opcode.length];
                short opcodeReal = 0x00; // only checking lower byte though
                for(int i=0;i<opcode.length;i++)
                {
                    byte[] opcodeWord = Shorts.toByteArray(opcode[i]);
                    opcodeValues[i]=opcodeWord[0];
                    opcodeReal=(short)((opcode[i]-0x00)&0xFF); //stompy stomp, only keeps the last
                }

                int valueUnsigned = XAPDisAsmGeneratedCode.unsignedCounts.get(Utils.bytesToHex(opcodeValues));
                int valueSigned =XAPDisAsmGeneratedCode.signedCounts.get(Utils.bytesToHex(opcodeValues));
                String label = "";

                String value = "";
                switch(opcodeReal)
                {
                    case 0x20:
                        label="bgt";
                        value = ParamManglerAddress(valueSigned);
                        break;
                    case 0x24:
                        label="bge";
                        value = ParamManglerAddress(valueSigned);
                        break;
                    case 0x28:
                        label="blt";
                        value = ParamManglerAddress(valueSigned);
                        break;
                    case 0x2c:
                        label="bcz";
                        value = ParamManglerAddress(valueSigned);
                        break;
                    case 0xe8:
                        label="bpl";
                        value = ParamManglerAddress(valueSigned);
                        break;
                    case 0xec:
                        label="bmi";
                        value = ParamManglerAddress(valueSigned);
                        break;
                }
                String potentialTargetAddress=addressToString(address+opcode.length-1+valueSigned);
                return OpcodeAddressRangeToString(address,opcode.length)+":"+label+":"+value+"; Target="+potentialTargetAddress+" OR "+addressToString(valueUnsigned);
            };





            manglers[0x0] = (modifier,opcode,address)->{
                if(modifier!=0x00)
                {
                    return invalid.mangleOpCode(modifier,opcode,address);
                };
                return OpcodeAddressRangeToString(address,opcode.length)+": nop";
            };
            manglers[0x1]=invalid;
            manglers[0x2]=invalid;
            manglers[0x3]=invalid;
            manglers[0x4]=invalid;
            manglers[0x5]=invalid;
            manglers[0x6]=invalid;
            manglers[0x7]=invalid;
            manglers[0x8]=invalid;
            manglers[0x9]=(modifier,opcode,address)->{

                return addressToString(address)+": Modifier, this should not be visible at all in the disassembly!!!!!, should not happen";
            };
            manglers[0xa]=st;
            manglers[0xb]=enterlleavel;
            manglers[0xc]=invalid;
            manglers[0xd]=invalid;
            manglers[0xe]=ld;
            manglers[0xf]=enterlleavel;


            manglers[0x10]=ld;
            manglers[0x11]=ld;
            manglers[0x12]=ld;
            manglers[0x13]=ld;
            manglers[0x14]=ld;
            manglers[0x15]=ld;
            manglers[0x16]=ld;
            manglers[0x17]=ld;
            manglers[0x18]=ld;
            manglers[0x19]=ld;
            manglers[0x1a]=ld;
            manglers[0x1b]=ld;
            manglers[0x1c]=ld;
            manglers[0x1d]=ld;
            manglers[0x1e]=ld;
            manglers[0x1f]=ld;

            manglers[0x20]=bxx; //BGT
            manglers[0x21]=st;
            manglers[0x22]=st;
            manglers[0x23]=st;
            manglers[0x24]=bxx;//bge
            manglers[0x25]=st;
            manglers[0x26]=st;
            manglers[0x27]=st;
            manglers[0x28]=bxx; //ble
            manglers[0x29]=st;
            manglers[0x2a]=st;
            manglers[0x2b]=st;
            manglers[0x2c]=bxx; //BCZ
            manglers[0x2d]=st;
            manglers[0x2e]=st;
            manglers[0x2f]=st;


            manglers[0x30]=add;
            manglers[0x31]=add;
            manglers[0x32]=add;
            manglers[0x33]=add;
            manglers[0x34]=add;
            manglers[0x35]=add;
            manglers[0x36]=add;
            manglers[0x37]=add;
            manglers[0x38]=add;
            manglers[0x39]=add;
            manglers[0x3a]=add;
            manglers[0x3b]=add;
            manglers[0x3c]=add;
            manglers[0x3d]=add;
            manglers[0x3e]=add;
            manglers[0x3f]=add;

            manglers[0x40]=addc;
            manglers[0x41]=addc;
            manglers[0x42]=addc;
            manglers[0x43]=addc;
            manglers[0x44]=addc;
            manglers[0x45]=addc;
            manglers[0x46]=addc;
            manglers[0x47]=addc;
            manglers[0x48]=addc;
            manglers[0x49]=addc;
            manglers[0x4a]=addc;
            manglers[0x4b]=addc;
            manglers[0x4c]=addc;
            manglers[0x4d]=addc;
            manglers[0x4e]=addc;
            manglers[0x4f]=addc;



            manglers[0x50]=sub;
            manglers[0x51]=sub;
            manglers[0x52]=sub;
            manglers[0x53]=sub;
            manglers[0x54]=sub;
            manglers[0x55]=sub;
            manglers[0x56]=sub;
            manglers[0x57]=sub;
            manglers[0x58]=sub;
            manglers[0x59]=sub;
            manglers[0x5a]=sub;
            manglers[0x5b]=sub;
            manglers[0x5c]=sub;
            manglers[0x5d]=sub;
            manglers[0x5e]=sub;
            manglers[0x5f]=sub;

            manglers[0x60]=subc;
            manglers[0x61]=subc;
            manglers[0x62]=subc;
            manglers[0x63]=subc;
            manglers[0x64]=subc;
            manglers[0x65]=subc;
            manglers[0x66]=subc;
            manglers[0x67]=subc;
            manglers[0x68]=subc;
            manglers[0x69]=subc;
            manglers[0x6a]=subc;
            manglers[0x6b]=subc;
            manglers[0x6c]=subc;
            manglers[0x6d]=subc;
            manglers[0x6e]=subc;
            manglers[0x6f]=subc;

            manglers[0x70]=nadd;
            manglers[0x71]=nadd;
            manglers[0x72]=nadd;
            manglers[0x73]=nadd;
            manglers[0x74]=nadd;
            manglers[0x75]=nadd;
            manglers[0x76]=nadd;
            manglers[0x77]=nadd;
            manglers[0x78]=nadd;
            manglers[0x79]=nadd;
            manglers[0x7a]=nadd;
            manglers[0x7b]=nadd;
            manglers[0x7c]=nadd;
            manglers[0x7d]=nadd;
            manglers[0x7e]=nadd;
            manglers[0x7f]=nadd;

            manglers[0x80]=cmp;
            manglers[0x81]=cmp;
            manglers[0x82]=cmp;
            manglers[0x83]=cmp;
            manglers[0x84]=cmp;
            manglers[0x85]=cmp;
            manglers[0x86]=cmp;
            manglers[0x87]=cmp;
            manglers[0x88]=cmp;
            manglers[0x89]=cmp;
            manglers[0x8a]=cmp;
            manglers[0x8b]=cmp;
            manglers[0x8c]=cmp;
            manglers[0x8d]=cmp;
            manglers[0x8e]=cmp;
            manglers[0x8f]=cmp;

            manglers[0x90]=xmult;
            manglers[0x91]=xmult;
            manglers[0x92]=xmult;
            manglers[0x93]=xmult;
            manglers[0x94]=xdiv;
            manglers[0x95]=xdiv;
            manglers[0x96]=xdiv;
            manglers[0x97]=xdiv;
            manglers[0x98]=tst;
            manglers[0x99]=tst;
            manglers[0x9a]=tst;
            manglers[0x9b]=tst;
            manglers[0x9c]=bsr;
            manglers[0x9d]=bsr;
            manglers[0x9e]=bsr;
            manglers[0x9f]=bsr;
            //0xax



            manglers[0xa0]=xsl;
            manglers[0xa1]=xsl;
            manglers[0xa2]=xsl;
            manglers[0xa3]=xsl;
            manglers[0xa4]=xsr;
            manglers[0xa5]=xsr;
            manglers[0xa6]=xsr;
            manglers[0xa7]=xsr;
            manglers[0xa8]=invalid;
            manglers[0xa9]=invalid;
            manglers[0xaa]=invalid;
            manglers[0xab]=invalid;
            manglers[0xac]=invalid;
            manglers[0xad]=invalid;
            manglers[0xae]=invalid;
            manglers[0xaf]=invalid;




            manglers[0xb0]=or;
            manglers[0xb1]=or;
            manglers[0xb2]=or;
            manglers[0xb3]=or;
            manglers[0xb4]=or;
            manglers[0xb5]=or;
            manglers[0xb6]=or;
            manglers[0xb7]=or;
            manglers[0xb8]=or;
            manglers[0xb9]=or;
            manglers[0xba]=or;
            manglers[0xbb]=or;
            manglers[0xbc]=or;
            manglers[0xbd]=or;
            manglers[0xbe]=or;
            manglers[0xbf]=or;


            manglers[0xc0]=and;
            manglers[0xc1]=and;
            manglers[0xc2]=and;
            manglers[0xc3]=and;
            manglers[0xc4]=and;
            manglers[0xc5]=and;
            manglers[0xc6]=and;
            manglers[0xc7]=and;
            manglers[0xc8]=and;
            manglers[0xc9]=and;
            manglers[0xca]=and;
            manglers[0xcb]=and;
            manglers[0xcc]=and;
            manglers[0xcd]=and;
            manglers[0xce]=and;
            manglers[0xcf]=and;

            manglers[0xd0]=xor;
            manglers[0xd1]=xor;
            manglers[0xd2]=xor;
            manglers[0xd3]=xor;
            manglers[0xd4]=xor;
            manglers[0xd5]=xor;
            manglers[0xd6]=xor;
            manglers[0xd7]=xor;
            manglers[0xd8]=xor;
            manglers[0xd9]=xor;
            manglers[0xda]=xor;
            manglers[0xdb]=xor;
            manglers[0xdc]=xor;
            manglers[0xdd]=xor;
            manglers[0xde]=xor;
            manglers[0xdf]=xor;

            manglers[0xe0]=bra;
            manglers[0xe1]=bra;
            manglers[0xe2]=bra;
            manglers[0xe3]=bra;
            manglers[0xe4]=blt;
            manglers[0xe5]=blt;
            manglers[0xe6]=blt;
            manglers[0xe7]=blt;
            manglers[0xe8]=bxx; // bpl
            manglers[0xe9]=invalid;
            manglers[0xea]=invalid;
            manglers[0xeb]=invalid;
            manglers[0xec]=bxx; //bmi
            manglers[0xed]=invalid;
            manglers[0xee]=invalid;
            manglers[0xef]=invalid;

            manglers[0xf0]=bne;
            manglers[0xf1]=bne;
            manglers[0xf2]=bne;
            manglers[0xf3]=bne;
            manglers[0xf4]=beq;
            manglers[0xf5]=beq;
            manglers[0xf6]=beq;
            manglers[0xf7]=beq;
            manglers[0xf8]=bcc;
            manglers[0xf9]=bcc;
            manglers[0xfa]=bcc;
            manglers[0xfb]=bcc;
            manglers[0xfc]=bcs;
            manglers[0xfd]=bcs;
            manglers[0xfe]=bcs;
            manglers[0xff]=bcs;
        }

    static OpCodeMangler GetModifiedMangler(String opcodeLabel, String OpcodeLabelModified, short opcodeBase)
    {
        return (modifier,opcode,address)->{
            boolean modified = modifier==0x09;

            byte[] opcodeValues = new byte[opcode.length];
            short opcodeReal = 0x00; // only checking lower byte though
            for(int i=0;i<opcode.length;i++)
            {
                byte[] opcodeWord = Shorts.toByteArray(opcode[i]);
                opcodeValues[i]=opcodeWord[0];
                opcodeReal=(short)((opcode[i]-opcodeBase)&0xFF); //stompy stomp, only keeps the last
            }

            int valueUnsigned = XAPDisAsmGeneratedCode.unsignedCounts.get(Utils.bytesToHex(opcodeValues));
            int valueSigned =XAPDisAsmGeneratedCode.signedCounts.get(Utils.bytesToHex(opcodeValues));
            String register = "";

            String value = "";
            switch(opcodeReal)
            {
                case 0x00:
                    value = ParamManglerImmediate(valueUnsigned);
                    break;
                case 0x01:
                    value = ParamManglerAddress(valueUnsigned);
                    break;
                case 0x02:
                    value = ParamManglerIndirectX(valueSigned);
                    break;
                case 0x03:
                    value = ParamManglerIndirectY(valueSigned);
                    break;
            }

            return OpcodeAddressRangeToString(address,opcode.length+(modified?1:0))+":"+(modified?OpcodeLabelModified:opcodeLabel)+":"+value;
        };
    }





    static OpCodeMangler GetBraMangler(String opcodeLabel, short opcodeBase, boolean bra_specialcase)
    {
        return (modifier,opcode,address)->{
            if(modifier!=0x00)
            {
                //return invalid.mangleOpCode(modifier,opcode,address);
            };

            byte[] opcodeValues = new byte[opcode.length];
            short opcodeReal = 0x00; // only checking lower byte though
            for(int i=0;i<opcode.length;i++)
            {
                byte[] opcodeWord = Shorts.toByteArray(opcode[i]);
                opcodeValues[i]=opcodeWord[0];
                opcodeReal=(short)((opcode[i]-opcodeBase)&0xFF); //stompy stomp, only keeps the last
            }
            int valueUnsigned=0;
            int valueSigned=0;
            if(opcodeReal !=0x0)
            {
                valueUnsigned = XAPDisAsmGeneratedCode.unsignedCounts.get(Utils.bytesToHex(opcodeValues));
                valueSigned =XAPDisAsmGeneratedCode.signedCounts.get(Utils.bytesToHex(opcodeValues));
            }
            String register = "";

            String value = "";
            switch(opcodeReal)
            {
                case 0x00:
                    register="AH";
                    value = ParamManglerConstAddr(untwiddleOpcodeParamBra(opcodeValues,address+opcode.length-1));
                    break;
                case 0x01:
                    register="AH";
                    value = ParamManglerAddress(valueSigned);
                    break;
                case 0x02:
                    register="AH";
                    value = ParamManglerIndirectX(valueSigned);
                    break;
                case 0x03:
                    register="AH";
                    value = ParamManglerIndirectY(valueSigned);
                    break;
            }
            String potentialTargetAddress=addressToString(address+opcode.length-1+valueSigned);

            if(bra_specialcase&&opcodeReal==0x02&&address==0)
            {
                return addressToString(address)+":rts";
            }


            return OpcodeAddressRangeToString(address,opcode.length)+":"+opcodeLabel+":"+value+"; Target="+potentialTargetAddress+" OR "+addressToString(valueUnsigned);
        };
    }



    static OpCodeMangler GetBasicMangler(String opcodeLabel, short opcodeBase)
    {
        return (modifier,opcode,address)->{
            if(modifier!=0x00)
            {
                //return invalid.mangleOpCode(modifier,opcode,address);
            };

            byte[] opcodeValues = new byte[opcode.length];
            short opcodeReal = 0x00; // only checking lower byte though
            for(int i=0;i<opcode.length;i++)
            {
                byte[] opcodeWord = Shorts.toByteArray(opcode[i]);
                opcodeValues[i]=opcodeWord[0];
                opcodeReal=(short)(opcodeWord[1]&(short)0x00FF); //stompy stomp, only keeps the last
            }
            short opcodeDispatch = (short)(opcodeReal-opcodeBase);

            int valueUnsigned = XAPDisAsmGeneratedCode.unsignedCounts.get(Utils.bytesToHex(opcodeValues));
            int valueSigned =XAPDisAsmGeneratedCode.signedCounts.get(Utils.bytesToHex(opcodeValues));
            String register = "";

            String value = "";
            switch(opcodeDispatch)
            {
                case 0x0:
                    register="AH";
                    value = ParamManglerImmediate(valueUnsigned);
                    break;
                case 0x1:
                    register="AH";
                    value = ParamManglerAddress(valueUnsigned);
                    break;
                case 0x2:
                    register="AH";
                    value = ParamManglerIndirectX(valueSigned);
                    break;
                case 0x3:
                    register="AH";
                    value = ParamManglerIndirectY(valueSigned);
                    break;


                case 0x4:
                    register="AL";
                    value = ParamManglerImmediate(valueUnsigned);
                    break;
                case 0x5:
                    register="AL";
                    value = ParamManglerAddress(valueUnsigned);
                    break;
                case 0x6:
                    register="AL";
                    value = ParamManglerIndirectX(valueSigned);
                    break;
                case 0x7:
                    register="AL";
                    value = ParamManglerIndirectY(valueSigned);
                    break;

                case 0x8:
                    register="X";
                    value = ParamManglerImmediate(valueUnsigned);
                    break;
                case 0x9:
                    register="X";
                    value = ParamManglerAddress(valueUnsigned);
                    break;
                case 0xa:
                    register="X";
                    value = ParamManglerIndirectX(valueSigned);
                    break;
                case 0xb:
                    register="X";
                    value = ParamManglerIndirectY(valueSigned);
                    break;

                case 0xc:
                    register="Y";
                    value = ParamManglerImmediate(valueUnsigned);
                    break;
                case 0xd:
                    register="Y";
                    value = ParamManglerAddress(valueUnsigned);
                    break;
                case 0xe:
                    register="Y";
                    value = ParamManglerIndirectX(valueSigned);
                    break;
                case 0xf:
                    register="Y";
                    value = ParamManglerIndirectY(valueSigned);
                    break;
            }
            return OpcodeAddressRangeToString(address,opcode.length)+":"+opcodeLabel+": "+register+" "+value;
        };
    }

    private static int untwiddleOpcodeParamBra(byte[] opcode,int opcodelocation)
    {
        int out = opcode[0];
        l.trace(out);
        l.trace(String.format("0x%08X", out));
        for(int i=1;i<opcode.length;i++)
        {
            out=out<<8;
            out+=opcode[i];
            l.trace(out);
            l.trace(opcode[i]);
            l.trace(String.format("0x%08X", out));
        }
        out=out+opcodelocation;
        out = out &0x00FFFFFF;
        return out;
    }


    private static String addressToString(int address)
    {
        //return "0x"+(Utils.bytesToHex(Ints.toByteArray(address)));
        return String.format("0x%08X", address);
    }

    private static String OpcodeAddressRangeToString(int address,int opcodelen)
    {
        String startaddr = addressToString(address);
        if(labels.containsKey(startaddr))
        {
            startaddr = labels.get(startaddr)+":\n"+startaddr;
        }


        if(opcodelen>1)
        {
        return startaddr+
                "---"+
                addressToString(address+(opcodelen-1));
        }
        else return startaddr;
    }

    private static String ParamManglerImmediate(int number)
    {
        return "#"+number;
    }

    private static String ParamManglerConstAddr(int number)
    {
        String addr = addressToString(number);
        if(labels.containsKey(addr))
        {
            addr = addr+"("+labels.get(addr)+")";
        }
        return addr;
    }

    private static String ParamManglerAddress(int number)
    {
        return "@"+number;
    }

    private static String ParamManglerIndirectX(int number)
    {
        return "@("+number+",X)";
    }

    private static String ParamManglerIndirectY(int number)
    {
        return "@("+number+",Y)";
    }



    public static void Disassemble(String in, String out)
    {
        try {
            RandomAccessFile f = new RandomAccessFile(in,"r");
            PrintStream disassembled = new PrintStream(new FileOutputStream(out));

            String mapfilepath = in+".sbwmap";
            if(new File(mapfilepath).exists())
            {
                RandomAccessFile mapfile = new RandomAccessFile(mapfilepath,"r");
                String line = mapfile.readLine();
                while(line !=null)
                {
                    if(line.equals(""))
                    {
                        line = mapfile.readLine();
                        continue;
                    }
                    String[] entry = line.split(":");
                    labels.put(entry[0].toLowerCase(),entry[1]);
                    line = mapfile.readLine();
                }
            }


            long assemblyLength = f.length()/2;

            int baseAddressLastOpcode=0;
            List<Short> opcodeHoldingBay = new ArrayList<>(5);

            for(int i=0;i<assemblyLength;i++)
            {
                Short word = f.readShort();
                l.trace("READ:"+ (word&0xff));
                l.trace(addressToString(i));
                l.trace(addressToString(baseAddressLastOpcode));
                opcodeHoldingBay.add(word);
                if(word==(short)0xfe09) //specialcasing one specific opcode
                {
                    opcodeHoldingBay.clear();
                    disassembled.println(brxl.mangleOpCode((short)0x00,(short[])null,baseAddressLastOpcode));
                    baseAddressLastOpcode++;
                }
                else if((word&0xFF)!=0)
                {
                    if((word&0xFF)!=0x09)//skip modifier
                    {
                        short[] opcode;
                        short modifier=0x00;
                        if(opcodeHoldingBay.get(0)==0x09) //modifier
                        {
                            modifier = 0x09;
                            opcode = new short[opcodeHoldingBay.size()-1];
                            for(int j=1;j<opcodeHoldingBay.size();j++)
                            {
                                opcode[j-1]=opcodeHoldingBay.get(j);
                            }
                        }
                        else
                        {
                            opcode = new short[opcodeHoldingBay.size()];
                            for(int j=0;j<opcodeHoldingBay.size();j++)
                            {
                                opcode[j]=opcodeHoldingBay.get(j);
                            }
                        }
                        l.trace("Mangling Opcode with length"+opcode.length);
                        try{
                            disassembled.println(manglers[word&0xff].mangleOpCode(modifier,opcode,baseAddressLastOpcode));
                        }
                        catch(Exception e)
                        {
                            l.info(invalid.mangleOpCode(modifier,opcode,baseAddressLastOpcode));
                            disassembled.println(invalid.mangleOpCode(modifier,opcode,baseAddressLastOpcode));
                            //System.exit(-1);
                        }
                        baseAddressLastOpcode+=opcodeHoldingBay.size();
                        opcodeHoldingBay.clear();

                    }
                }
                else
                {
                    if(word==0x00)//nop
                    {
                        disassembled.println(manglers[0].mangleOpCode((short)0x00, new short[]{(short)0x00}, baseAddressLastOpcode));
                        opcodeHoldingBay.clear();
                        baseAddressLastOpcode++;
                    }
                    l.trace("VALUE");
                    //opcodeHoldingBay.add(word);
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
