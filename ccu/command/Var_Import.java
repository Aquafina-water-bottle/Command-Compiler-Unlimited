package ccu.command;

import java.io.File;
import java.util.ArrayList;

import ccu.general.GeneralFile;
import ccu.general.ReadConfig;

public class Var_Import {
	/** Avaliable parameters:
	 * * - to specify that it's a directory rather than a ccu file
	 * LIBRARY- to import from a specific library folder (specified in the ini file) - a special 'DIRECTORY'
	 * WITHIN - an import file is either within the folder or a folder above
	 * GROUPCOORDS - to import only group coordinates from the name_dat.ccu files
	 * Defaults to LIBRARY
	 * 
	 * LIBRARY should not be used with GROUPCOORDS - all ccu files that aren't specifically meant for importing should be elsewhere
	 * LIBRARY cannot work with WITHIN
	 * * cannot work with WITHIN
	 * 
	 * whether the functions / definitions / arrays in the imported files are global depends on the actual file itself
	 * aka the funcs / defs / arrays should be specified with "global" themselves
	 * 
	 * IMPORT {LIBRARY General/utils.ccu}
	 * 
	 */

	private ArrayList<String> arrayReturn = new ArrayList<String>();

	private int tabNum;
	private String fullLineGet;

	public Var_Import(String fullLineGet, int tabNumGet) {
		this.tabNum = tabNumGet;
		this.fullLineGet = fullLineGet;
	}

	public ArrayList<String> getArray() {

		// import lookup type (LIBRARY = 1, WITHIN = 2)
		// If it's not stated as either or, it defaults to direct file
		Integer importLookupType = null;

		// Get the entire directory (*)
		Boolean importDirectory = null;

		// Import type (normal = 1, group coords = 2)
		Integer importType = null;

		// File array
		ArrayList<File> getFileArray = new ArrayList<File>();

		String whitespaceCalc = this.fullLineGet.substring(0,
				(this.fullLineGet.length() - this.fullLineGet.replaceAll("^\\s+", "").length()));
		if (whitespaceCalc.contains(" ")) {
			System.out.println("ERROR: Line '" + this.fullLineGet + "' contains spaces instead of tab spaces");
			System.exit(0);
		}

		if (whitespaceCalc.length() - whitespaceCalc.replace("\t", "").length() != this.tabNum) {
			System.out.println("ERROR: Line '" + this.fullLineGet + "' contains an incorrect number of tab spaces");
			System.exit(0);
		}

		String statementEncase = this.fullLineGet.replaceFirst("IMPORT", "").replaceAll("^\\s+", "");
		if (statementEncase.startsWith("{") && statementEncase.endsWith("}")) {
			String statementArgs = statementEncase.substring(1, statementEncase.length() - 1);

			if (statementArgs.contains("\t")) {
				System.out.println("ERROR: Arguments in line '" + this.fullLineGet + "' contains unnecessary tab spaces");
				System.exit(0);
			}

			if (statementArgs.contains(" ")) {

				switch (statementArgs.substring(0, statementArgs.indexOf(" "))) {
				case "LIBRARY":
					importLookupType = 1;
					// removes LIBRARY
					statementArgs = statementArgs.substring(statementArgs.indexOf(" ") + 1);
					break;

				case "WITHIN":
					importLookupType = 2;
					// removes WITHIN
					statementArgs = statementArgs.substring(statementArgs.indexOf(" ") + 1);
					break;

				case "*":
					importDirectory = true;
					// removes *
					statementArgs = statementArgs.substring(statementArgs.indexOf(" ") + 1);
					break;

				case "GROUPCOORDS":
					importType = 2;
					// removes GROUPCOORDS
					statementArgs = statementArgs.substring(statementArgs.indexOf(" ") + 1);
					break;
				}
			}
			// Gets second parameters
			if (statementArgs.contains(" ")) {
				switch (statementArgs.substring(0, statementArgs.indexOf(" "))) {
				case "LIBRARY":
					if (importLookupType == null) {
						importLookupType = 1;
					}
					// removes LIBRARY
					statementArgs = statementArgs.substring(statementArgs.indexOf(" ") + 1);
					break;

				case "WITHIN":
					if (importLookupType == null) {
						importLookupType = 2;
					}
					// removes WITHIN
					statementArgs = statementArgs.substring(statementArgs.indexOf(" ") + 1);
					break;

				case "*":
					if (importDirectory == null) {
						importDirectory = true;
					}
					// removes *
					statementArgs = statementArgs.substring(statementArgs.indexOf(" ") + 1);
					break;

				case "GROUPCOORDS":
					if (importType == null) {
						importType = 2;
					}
					// removes GROUPCOORDS
					statementArgs = statementArgs.substring(statementArgs.indexOf(" ") + 1);
					break;
				}
			} else {
				switch (statementArgs) {
				case "LIBRARY":
					if (importLookupType == null) {
						importLookupType = 1;
					}
					statementArgs = null;
					break;

				case "WITHIN":
					if (importLookupType == null) {
						importLookupType = 2;
					}
					statementArgs = null;
					break;

				case "*":
					if (importDirectory == null) {
						importDirectory = true;
					}
					statementArgs = null;
					break;

				case "GROUPCOORDS":
					if (importType == null) {
						importType = 2;
					}
					statementArgs = null;
					break;
				}
			}

			// Default for importLookupType
			if (importLookupType == null) {
				importLookupType = 0;
			}

			// default for importDirectory
			if (importDirectory == null) {
				importDirectory = false;
			}

			// default for importType
			if (importType == null) {
				importType = 1;
			}

			if (statementArgs != null
					|| ((importLookupType == 1 || importLookupType == 2) && importDirectory == true && importType == 1)) {
				if (importLookupType == 0) { // direct
					if (importDirectory == true) { // full directory
						if (importType == 1) { // normal ccu files

							// direct, full directory, normal files
							File importFile = new File(statementArgs);
							if (importFile.isDirectory()) {

								// Get all files within the directory and directories inside
								getFileArray = GeneralFile.getAllFiles(importFile, ".ccu");
								for (File filesInArray : getFileArray) {
									if (filesInArray.equals(ReadConfig.regFilePath) == false) {
										GeneralFile importFileCalc = new GeneralFile(filesInArray);
										arrayReturn.addAll(GeneralFile.parseCCU(importFileCalc.getFileArray()));
									}
								}
							} else {
								System.out.println(
										"ERROR: Directory '" + statementArgs + "' in line '" + this.fullLineGet + "' does not exist");
								System.exit(0);
							}

						} else { // dat files
							// direct, full directory, dat files

						}
					} else { // not directory
						if (importType == 1) { // normal ccu files

							// direct, not directory, normal files
							String importFile = GeneralFile.checkFileExtension(statementArgs, ".ccu");
							GeneralFile importFileCalc = new GeneralFile(importFile);
							arrayReturn.addAll(GeneralFile.parseCCU(importFileCalc.getFileArray()));

						} else { // dat files

							// direct, not directory, dat files
						}
					}
				} else {
					if (importLookupType == 1) { // library
						if (importDirectory == true) { // if it's a full directory
							if (importType == 1) { // normal ccu files

								// library, full directory, normal
								File importFile = ReadConfig.importLibraryPath;
								if (importFile.isDirectory()) {
									getFileArray = GeneralFile.getAllFiles(importFile, ".ccu");
									for (File filesInArray : getFileArray) {
										if (filesInArray.equals(ReadConfig.regFilePath) == false) {
											GeneralFile importFileCalc = new GeneralFile(filesInArray);
											arrayReturn.addAll(GeneralFile.parseCCU(importFileCalc.getFileArray()));
										}
									}

								} else {
									System.out.println("ERROR: directory '" + ReadConfig.importLibraryPath + "' in line '"
											+ this.fullLineGet + "' does not exist");
									System.exit(0);
								}

							} else { // dat files
								// library, full directory, dat files

							}

						} else { // not directory
							if (importType == 1) { // normal ccu files
								// library, not directory, ccu files
								String importFile = GeneralFile
										.checkFileExtension(ReadConfig.importLibraryPath.toString() + "/" + statementArgs, ".ccu");
								GeneralFile importFileCalc = new GeneralFile(importFile);
								arrayReturn.addAll(GeneralFile.parseCCU(importFileCalc.getFileArray()));

							} else { // dat files
								// library, not directory, dat files

							}
						}
					} else { // within
						if (importDirectory == true) { // full directory
							if (importType == 1) { // normal ccu files

								// within, full directory, normal ccu file
								// gets all files all within the same folder EXCEPT the normal one
								File importFile = ReadConfig.regFilePath.getParentFile();
								if (importFile.isDirectory()) {
									getFileArray = GeneralFile.getFilesInFolder(importFile, ".ccu");
									for (File filesInArray : getFileArray) {
										if (filesInArray.equals(ReadConfig.regFilePath) == false) {
											GeneralFile importFileCalc = new GeneralFile(filesInArray);
											arrayReturn.addAll(GeneralFile.parseCCU(importFileCalc.getFileArray()));
										}
									}

								} else {
									System.out.println("ERROR: directory '" + ReadConfig.importLibraryPath + "' in line '"
											+ this.fullLineGet + "' does not exist");
									System.exit(0);
								}
							} else { // imports dat files
								// within, full direcctory, dat files

							}
						} else { // not a full directory
							if (importType == 1) { // normal ccu files

								// within, not directory, normal ccu files
								File importFile = new File(GeneralFile.checkFileExtension(
										ReadConfig.regFilePath.getParentFile().toString() + "/" + statementArgs, ".ccu"));
								if (importFile.isFile()) {
									GeneralFile importFileCalc = new GeneralFile(importFile);
									arrayReturn.addAll(GeneralFile.parseCCU(importFileCalc.getFileArray()));
								} else {
									importFile = new File(GeneralFile.checkFileExtension(
											ReadConfig.regFilePath.getParentFile().getParentFile().toString() + "/" + statementArgs,
											".ccu"));
									if (importFile.isFile()) {
										GeneralFile importFileCalc = new GeneralFile(importFile);
										arrayReturn.addAll(GeneralFile.parseCCU(importFileCalc.getFileArray()));
									} else {
										System.out.println("ERROR: File '" + statementArgs + "' under line '" + this.fullLineGet
												+ "' cannot be found");
										System.exit(0);
									}
								}

							} else { // imports dat files
								// within, not directory, dat files

							}
						}
					}
				}
			} else {
				if (importDirectory == true) {
					System.out.println("ERROR: No directory is provided in line '" + this.fullLineGet + "'");
				} else {
					System.out.println("ERROR: No file is provided in line '" + this.fullLineGet + "'");
				}

				System.exit(0);
			}

		} else {
			System.out.println("ERROR: Incorrect syntax at '" + this.fullLineGet + "'");
			System.exit(0);
		}
		
		// Adds the proper whitespace
		for (int i = 0; i < arrayReturn.size(); i++) {
			arrayReturn.set(i, whitespaceCalc + arrayReturn.get(i));
		}
		
		return arrayReturn;
	}
}
