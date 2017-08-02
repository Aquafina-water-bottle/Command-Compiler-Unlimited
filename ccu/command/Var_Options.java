package ccu.command;

import java.io.File;
import java.util.ArrayList;

import ccu.block.Coordinates;
import ccu.general.ArgUtils;

public class Var_Options {
	private ArrayList<String> arrayGet = new ArrayList<String>();
	private String fullLineGet;
	private int tabNum;

	// All options will be static as there is no need
	// to have different options
	public static String blockOption = null;
	public static Coordinates coordsOption = new Coordinates(0, 0, 0);
	public static Boolean coordsOptionTest = null;
	public static String styleOption = null;
	public static Boolean parseOption = null;
	public static Boolean commandOption = null;
	public static Boolean combinerOption = null;
	public static File filePathFuncOption = null;
	public static Boolean parseChanges = null;

	// @formatter:off
	private final String[] optionArray = {
			"blockOption",
			"coordsOption",
			"styleOption",
			"parseOption",
			"commandOption",
			"combinerOption",
			"filePathFuncOption",
			"parseChangesOption",
			"A"
			};
	// @formatter:on

	// encapsulateArray, tabNum, ccuFileArray.get(i)
	public Var_Options(ArrayList<String> arrayGet, int tabNumGet, String fullLineGet) {
		this.arrayGet = arrayGet;
		this.fullLineGet = fullLineGet;
		this.tabNum = tabNumGet;
	}

	public ArrayList<String> getArray() {
		
		if (fullLineGet.endsWith(":") == false) {
			System.out.println("Incorrect syntax at line '" + fullLineGet + "' (doesn't end with ':')");
			System.exit(0);
		}

		boolean lineUsed = false;

		// does the checkCommands part first to make sure all statements with
		// this is parsed first
		ReadCCUFile ccuSubsetFile = new ReadCCUFile(this.arrayGet, tabNum);
		ArrayList<String> checkCommandsArray = ccuSubsetFile.checkCommands();
		if (checkCommandsArray.isEmpty() == false) {
			this.arrayGet = checkCommandsArray;
		}

		// checks tab spaces
		ArgUtils.checkWhiteSpace(this.arrayGet, this.tabNum, false);

		for (String line : this.arrayGet) {
			// now actually starts detecting the options lol
			lineUsed = false;
			for (String optionName : optionArray) {

				if (line.contains(optionName) && line.trim().substring(0, optionName.length()).equals(optionName)) {
					String tempInput = line.substring(line.indexOf(optionName) + optionName.length() + 1).trim();

					if (optionName.equals("blockOption")) {
						Var_Options.blockOption = tempInput;
						lineUsed = true;
					}

					if (optionName.equals("coordsOption")) {
						Var_Options.coordsOption.setCoordinates(tempInput);
						coordsOptionTest = true;
						lineUsed = true;
					}

					if (optionName.equals("styleOption")) {
						Var_Options.styleOption = tempInput;
						lineUsed = true;
					}

					if (optionName.equals("parseOption")) {
						if (tempInput.equalsIgnoreCase("true")) {
							Var_Options.parseOption = true;
						} else {
							if (tempInput.equalsIgnoreCase("false")) {
								Var_Options.parseOption = false;
							}
						}
						lineUsed = true;
					}

					if (optionName.equals("commandOption")) {
						if (tempInput.equalsIgnoreCase("true")) {
							Var_Options.commandOption = true;
						} else {
							if (tempInput.equalsIgnoreCase("false")) {
								Var_Options.commandOption = false;
							}
						}
						lineUsed = true;
					}

					if (optionName.equals("combinerOption")) {
						if (tempInput.equalsIgnoreCase("true")) {
							Var_Options.combinerOption = true;
						} else {
							if (tempInput.equalsIgnoreCase("false")) {
								Var_Options.combinerOption = false;
							}
						}
						lineUsed = true;
					}

					if (optionName.equals("filePathFuncOption")) {
						Var_Options.filePathFuncOption = new File(tempInput);
						lineUsed = true;
					}

					if (optionName.equals("parseChangesOption")) {
						if (tempInput.equalsIgnoreCase("true")) {
							Var_Options.parseChanges = true;
						} else {
							if (tempInput.equalsIgnoreCase("false")) {
								Var_Options.parseChanges = false;
							}
						}
						lineUsed = true;
					}

					if (optionName.equals("A")) {
						if (tempInput.equals("well kept secret")) {
							RickRoll.lyrics();
						}
						lineUsed = true;
					}
				}
			}

			if (lineUsed == false) {
				System.out.println("ERROR: Invalid option at line '" + line.replaceAll("^\\s+", "") + "' under '"
						+ fullLineGet.replaceAll("^\\s+", "") + "'");
				System.exit(0);
			}
		}

		// should always return null 
		return null;
	}

	public static void checkOptions() {
		/**
		 * Method ran at the end of reading the document to
		 */

		if (blockOption == null) {
			System.out.println("WARNING: 'blockOption' field is empty - defaults to 'air 0'");
			blockOption = "air 0";
		}
		if (coordsOptionTest == null) {
			System.out.println("WARNING: 'coordsOption' field is empty - defaults to '~5, ~5, ~5'");
			coordsOption = new Coordinates(5, 5, 5, "~", "~", "~");
		}
		if (styleOption == null) {
			System.out.println("WARNING: 'styleOption' field is empty - defaults to '+X 16'");
			Var_Options.styleOption = "+X 16";
		}
		if (parseOption == null) {
			System.out.println("WARNING: 'parseOption' field is empty - defaults to 'true'");
			Var_Options.parseOption = true;
		}
		if (commandOption == null) {
			System.out.println("WARNING: 'commandOption' field is empty - defaults to 'true'");
			Var_Options.commandOption = true;
		}
		if (combinerOption == null) {
			System.out.println("WARNING: 'combinerOption' field is empty - defaults to 'true'");
			Var_Options.combinerOption = true;
		}
		if (filePathFuncOption == null || filePathFuncOption.toString().length() == 0) {
			System.out.println("WARNING: 'filePathFuncOption' field is empty - will be an issue if mcfunctions are used");
			Var_Options.filePathFuncOption = null;
		}
		if (parseChanges == null) {
			System.out.println("WARNING: 'parseChanges' field is empty - defaults to 'false'");
			Var_Options.parseChanges = false;
		}
	}

}
