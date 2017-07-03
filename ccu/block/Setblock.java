package ccu.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import ccu.command.Cmd_Group;
import ccu.command.Var_Options;
import ccu.general.GeneralFile;
import ccu.general.ReadConfig;

public class Setblock {

	// initial commands are setblocks and fill commands before the setting of command blocks
	public static ArrayList<String> initialCommands = new ArrayList<String>();

	// the command blocks themselves
	public static ArrayList<String> setblockCommands = new ArrayList<String>();

	// the command blocks except with added backslashes to work with the combiner
	public static ArrayList<String> combinerSetblockCommands = new ArrayList<String>();

	// the legit combiner commands
	public static ArrayList<String> fullCombinerCommands = new ArrayList<String>();

	// the length of each command in the combiner setblock array
	public static ArrayList<Integer> combinerCommandLengths = new ArrayList<Integer>();

	public static void getCommands() {
		int[] directionPosX = {5, 4, 3, 1, 0};
		int[] directionPosZ = {3, 2, 5, 1, 0};
		int[] directionNegX = {4, 5, 2, 1, 0};
		int[] directionNegZ = {2, 3, 4, 1, 0};
		int[] directionFinal = null;
		String groupType = null;

		// getting the setblock commands
		for (int i = 0; i < Cmd_Group.arrayGroupSave.size(); i++) {
			initialCommands.add("setblock " + Box.groupNameCoordArray[i].getString() + " " + Cmd_Group.arraySetblockSave.get(i));
		}

		// initialization
		int dataValue = 0;
		String cmd = null;

		// checking direction
		if (GroupStructure.styleOptionXZ.equals("+X")) {
			directionFinal = directionPosX;
		}
		if (GroupStructure.styleOptionXZ.equals("+Z")) {
			directionFinal = directionPosZ;
		}
		if (GroupStructure.styleOptionXZ.equals("-X")) {
			directionFinal = directionNegX;
		}
		if (GroupStructure.styleOptionXZ.equals("-Z")) {
			directionFinal = directionNegZ;
		}

		// Getting the setblock commands
		// Each group
		for (int i = 0; i < GroupStructure.groupCommandsArray.size(); i++) {

			// For each command in each group
			for (int j = 0; j < GroupStructure.groupCommandsArray.get(i).length; j++) {
				dataValue = 0;
				cmd = null;

				cmd = "setblock " + GroupStructure.groupCoordsArray.get(i)[j].getString() + " ";
				if (j == 0) {
					cmd = cmd + Cmd_Group.arrayBlockTypeSave.get(i) + " ";
				} else {
					cmd = cmd + "chain_command_block" + " ";
				}

				// gets direction value
				if (GroupStructure.groupDirectionArray.get(i)[j].equals("NORTH")) {
					dataValue = directionFinal[0];
				}

				if (GroupStructure.groupDirectionArray.get(i)[j].equals("SOUTH")) {
					dataValue = directionFinal[1];
				}

				if (GroupStructure.groupDirectionArray.get(i)[j].equals("SIDEWAYS")) {
					dataValue = directionFinal[2];
				}

				if (GroupStructure.groupDirectionArray.get(i)[j].equals("UP")) {
					dataValue = directionFinal[3];
				}

				if (GroupStructure.groupDirectionArray.get(i)[j].equals("DOWN")) {
					dataValue = directionFinal[4];
				}

				if (GroupStructure.groupConditionalArray.get(i)[j] == true) {
					dataValue += 8;
				}

				// adds data value and command
				// replaces '\' with '\\' and '"' with '\"'
				cmd = cmd + dataValue + " replace {Command:\""
						+ GroupStructure.groupCommandsArray.get(i)[j].replace("\\", "\\\\").replace("\"", "\\\"");

				// if it's the first command, it's not always active
				if (j == 0) {
					cmd = cmd + "\",TrackOutput:0b,auto:0b}";
				} else {
					cmd = cmd + "\",TrackOutput:0b,auto:1b}";
				}

				// finally adds it to the list
				setblockCommands.add(cmd);
			}
		}

		// writes the name_cmd.txt file
		if (Var_Options.commandOption == true) {
			String cmdFileCalc = ReadConfig.regFilePath.getName().toString().substring(0,
					ReadConfig.regFilePath.getName().toString().indexOf(".ccu")) + "_cmd.txt";
			File writeCmdFile = new File(ReadConfig.regFilePath.getParentFile().toString() + "//" + cmdFileCalc);

			PrintWriter writer = null;
			try {
				writer = new PrintWriter(writeCmdFile, "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				GeneralFile.dispError(e);
				System.exit(0);
			}

			// goes through initial commands (fill and setblock) and the actual setblock commands
			for (String writeCmd : initialCommands) {
				writer.println(writeCmd);
			}
			for (String writeCmd : setblockCommands) {
				writer.println(writeCmd);
			}

			writer.close();
		}

		if (Var_Options.parseOption == true) {
			// writes the name_parsed.txt file
			String parseFileCalc = ReadConfig.regFilePath.getName().toString().substring(0,
					ReadConfig.regFilePath.getName().toString().indexOf(".ccu")) + "_parsed.txt";
			File writeParseFile = new File(ReadConfig.regFilePath.getParentFile().toString() + "//" + parseFileCalc);
			PrintWriter writer = null;

			try {
				writer = new PrintWriter(writeParseFile, "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				GeneralFile.dispError(e);
				System.exit(0);
			}

			for (int i = 0; i < GroupStructure.groupCommandsArray.size(); i++) {
				for (int j = 0; j < GroupStructure.groupCommandsArray.get(i).length; j++) {

					// line breaks between each group
					if (i != 0 && j == 0) {
						writer.println("");
					}

					// sets group name
					if (j == 0) {
						if (Cmd_Group.arrayBlockTypeSave.get(i).equals("repeating_command_block")) {
							groupType = "CLOCK";
						}
						if (Cmd_Group.arrayBlockTypeSave.get(i).equals("command_block")) {
							groupType = "PULSE";
						}

						writer.println(Cmd_Group.arrayGroupSave.get(i)[0] + " | " + groupType + "\t\t["
								+ Box.groupNameCoordArray[i].getString() + " | " + Cmd_Group.arraySetblockSave.get(i) + "]");
					}

					// the actual commands
					if (GroupStructure.groupConditionalArray.get(i)[j] == true) {
						if (j < GroupStructure.groupCommandsArray.get(i).length - 1
								&& GroupStructure.groupConditionalArray.get(i)[j + 1] == false) {
							writer.println("\t L " + GroupStructure.groupCommandsArray.get(i)[j]);
						} else {
							if (j == GroupStructure.groupCommandsArray.get(i).length - 1) {
								writer.println("\t L " + GroupStructure.groupCommandsArray.get(i)[j]);
							} else {
								writer.println("\t | " + GroupStructure.groupCommandsArray.get(i)[j]);
							}
						}
					} else {
						writer.println("\t" + GroupStructure.groupCommandsArray.get(i)[j]);
					}
				}
			}
			writer.close();

		}

		String combinerBeg = null;
		String combinerMid = null;
		String combinerEnd = null;
		int combinerBegLength = 0;
		int combinerMidLength = 0;
		int combinerEndLength = 0;
		ArrayList<String> combinerArrayCalc = new ArrayList<String>();
		int combinerLengthCalc = 0;
		String combinerCmdCalc = null;
		boolean finalCommand = false;

		if (ReadConfig.mcVersion == 1) {
			combinerBeg = "summon FallingSand ~ ~1 ~ {Block:stone,Time:1,Passengers:["
					+ "{id:FallingSand,Block:redstone_block,Time:1,Passengers:["
					+ "{id:FallingSand,Block:activator_rail,Time:1,Passengers:[" + "{id:MinecartCommandBlock,Command:\"";

			combinerMid = "\"},{id:MinecartCommandBlock,Command:\"";

			combinerEnd = "\"},{id:MinecartCommandBlock,Command:\"setblock ~ ~1 ~ chain_command_block 0 replace "
					+ "{Command:\\\"fill ~ ~-4 ~ ~ ~2 ~ air\\\",auto:1b}\"},"
					+ "{id:MinecartCommandBlock,Command:\"setblock ~ ~2 ~ command_block 0 replace "
					+ "{Command:\\\"kill @e[type=MinecartCommandBlock,r=10]\\\"}\"},"
					+ "{id:MinecartCommandBlock,Command:\"setblock ~ ~3 ~ redstone_block\"},"
					+ "{id:MinecartCommandBlock,Command:\"summon FallingSand ~ ~-2 ~ "
					+ "{Block:command_block,Time:1s,TileEntityData:{TrackOutput:0b}}\"}," + "{id:MinecartCommandBlock,Command:\""
					+ "kill @e[type=MinecartCommandBlock,r=3]\"}]}]}]}";

		} else {
			combinerBeg = "summon falling_block ~ ~1 ~ {Block:stone,Time:1,Passengers:["
					+ "{id:falling_block,Block:redstone_block,Time:1,Passengers:["
					+ "{id:falling_block,Block:activator_rail,Time:1,Passengers:[" + "{id:commandblock_minecart,Command:\"";

			combinerMid = "\"},{id:commandblock_minecart,Command:\"";

			combinerEnd = "\"},{id:commandblock_minecart,Command:\"setblock ~ ~1 ~ chain_command_block 0 replace "
					+ "{Command:\\\"fill ~ ~-4 ~ ~ ~2 ~ air\\\",auto:1b}\"},"
					+ "{id:commandblock_minecart,Command:\"setblock ~ ~2 ~ command_block 0 replace "
					+ "{Command:\\\"kill @e[type=commandblock_minecart,r=10]\\\"}\"},"
					+ "{id:commandblock_minecart,Command:\"setblock ~ ~3 ~ redstone_block\"},"
					+ "{id:commandblock_minecart,Command:\"summon falling_block ~ ~-2 ~ "
					+ "{Block:command_block,Time:1s,TileEntityData:{TrackOutput:0b}}\"}," + "{id:commandblock_minecart,Command:\""
					+ "kill @e[type=commandblock_minecart,r=3]\"}]}]}]}";
		}

		// if serverPlugins is true - adds minecraft:
		if (ReadConfig.serverPlugins == true) {
			for (String serverCmd : ReadConfig.serverOverrideArray) {
				if (serverCmd.equals("fill") || serverCmd.equals("summon") || serverCmd.equals("kill") || serverCmd.equals("setblock")) {
					combinerBeg = combinerBeg.replace(serverCmd, "minecraft:" + serverCmd);
					combinerMid = combinerMid.replace(serverCmd, "minecraft:" + serverCmd);
					combinerEnd = combinerEnd.replace(serverCmd, "minecraft:" + serverCmd);
				}
			}
		}

		// using preventServerKick lengths
		if (ReadConfig.preventServerKick == true) {
			combinerBegLength = combinerBeg.replace("\"", "AA").replace("\\", "AA").replace("=", "AAAAAA").length();
			combinerMidLength = combinerMid.replace("\"", "AA").replace("\\", "AA").replace("=", "AAAAAA").length();
			combinerEndLength = combinerEnd.replace("\"", "AA").replace("\\", "AA").replace("=", "AAAAAA").length();
		} else {
			// just sets length by getting the length of the original strings
			combinerBegLength = combinerBeg.length();
			combinerMidLength = combinerMid.length();
			combinerEndLength = combinerEnd.length();
		}

		// creates the array for the combiner commands and the server combiner commands (accounts for kicking)
		// includes the initial commands array
		for (int i = 0; i < initialCommands.size(); i++) {
			combinerSetblockCommands.add(initialCommands.get(i).replace("\\", "\\\\").replace("\"", "\\\""));
			if (ReadConfig.preventServerKick == true) {
				combinerCommandLengths
						.add(combinerSetblockCommands.get(i).replace("\\", "AA").replace("\"", "AA").replace("=", "AAAAAA").length());
			} else {
				combinerCommandLengths.add(combinerSetblockCommands.get(i).length());
			}

		}

		for (int i = 0; i < setblockCommands.size(); i++) {
			combinerSetblockCommands.add(setblockCommands.get(i).replace("\\", "\\\\").replace("\"", "\\\""));
			if (ReadConfig.preventServerKick == true) {
				combinerCommandLengths
						.add(combinerSetblockCommands.get(i).replace("\\", "AA").replace("\"", "AA").replace("=", "AAAAAA").length());
			} else {
				combinerCommandLengths.add(combinerSetblockCommands.get(i).length());
			}
		}

		/* The general idea of this is to detect the previous and future commands
		 * Adds previous command with current and if the future causes it to be too long, then shit happens
		 * The difference between non-server and server is that server uses the length replacements
		 * The non-server sticks at 32500
		 */

		// server
		// initialization
		combinerLengthCalc += combinerBegLength + combinerEndLength;

		for (int i = 0; i < combinerSetblockCommands.size(); i++) {

			// adds commands to list
			combinerArrayCalc.add(combinerSetblockCommands.get(i));

			combinerLengthCalc += combinerCommandLengths.get(i);

			// if it is the end
			if (i == combinerSetblockCommands.size() - 1) {
				finalCommand = true;
			}

			// regular detection
			if (finalCommand == true || combinerLengthCalc + combinerMidLength * (combinerArrayCalc.size() + 1)
					+ combinerCommandLengths.get(i + 1) >= 32500) {

				// singular command
				if (combinerLengthCalc >= 32500) {
					fullCombinerCommands.add(combinerSetblockCommands.get(i));
				} else {

					// singular command within a combiner
					if (combinerArrayCalc.size() == 1) {
						fullCombinerCommands.add(combinerBeg + combinerSetblockCommands.get(i) + combinerEnd);
					} else {

						// normal combiner
						combinerCmdCalc = combinerBeg;
						for (int j = 0; j < combinerArrayCalc.size(); j++) {
							combinerCmdCalc += combinerArrayCalc.get(j);
							if (j < combinerArrayCalc.size() - 1) {
								combinerCmdCalc += combinerMid;
							}
						}
						combinerCmdCalc += combinerEnd;
						fullCombinerCommands.add(combinerCmdCalc);
					}
				}

				combinerLengthCalc = 0;
				combinerArrayCalc.clear();
				combinerCmdCalc = null;
			}
		}

		// writes the name_cmd.txt file
		if (Var_Options.combinerOption == true) {
			String combinerFileCalc = ReadConfig.regFilePath.getName().toString().substring(0,
					ReadConfig.regFilePath.getName().toString().indexOf(".ccu")) + "_combiner.txt";
			File writeCombinerFile = new File(ReadConfig.regFilePath.getParentFile().toString() + "//" + combinerFileCalc);

			PrintWriter writer = null;
			try {
				writer = new PrintWriter(writeCombinerFile, "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				GeneralFile.dispError(e);
				System.exit(0);
			}

			// goes through initial commands (fill and setblock) and the actual setblock commands
			for (String writeCmd : fullCombinerCommands) {
				writer.println(writeCmd + "\n");
			}

			writer.close();
		}
	}
}