# Sena Firmware Modding Tools
Supported: 
 * Splitting the Sena IMG files into modules (WOrks on any file that uses the long header format)
 * Disassembling Prompts into editable wave files. Requires SOX and the ADK for CSR chips installed and configured
    (Requires metadata for each headset. Currently tested: SRL2 and 10S) Requires ADK
 * Disassembling the VMApp File (Reachable via deep mode on the prompt extraction) WIP
 * Decoding VMApp FIle header (Reachable via deep mode on the prompt extraction)
 * Flash dump of the Headset via SPI. Requires ADK
 * Jailbreaking Sena Headsets to accept selfsigned DFU files. Requries ADK
 * Resignign DFU Files. Requires ADK
