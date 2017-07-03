package ccu.command;

import java.util.ArrayList;

public class Var_Define {
	public static ArrayList<String[]> arrayDefineSave = new ArrayList<String[]>();

	private int tabNum;
	private String fullLineGet;

	public Var_Define(String fullLineGet, int tabNumGet) {
		this.tabNum = tabNumGet;
		this.fullLineGet = fullLineGet;
	}

	// list of definitions that CANNOT be definitions because they are used for parameters for other CCU statements

	// @formatter:off
	public static String[] exceptionArray = {
			"DEF", "ARRAY", "GLOBAL", "COORDS", "TELE",
			"GROUP", "PULSE", "CLOCK", "BLOCK",
			"USE", "BEG", "END", "NOSPACE",
			"FUNC", "ACTIVATE",
			"MFUNC", "BRANCH",
			
			"COND", "OPTIONS", "UNASSIGN"
			};
	// @formatter:on

	public ArrayList<String> getArray() {
		/** Defines are generally a find --> replace thing
		 * Normal:
		 * DEF $Test$ asdf
		 * GLOBAL can be added to affect all and not just within the encapsulation
		 * COORDS --> set of 3 or 6 numbers
		 * TELE --> set of 5 numbers
		 * 
		 * TODO
		 * If COORDS --> name[x] gets x, name[y] gets y, name[z] gets z
		 * If TELE --> All above and name[yr] and name[xr]
		 */

		/* arrayDefineCalc will include:
		 * 	-Definition type - defineType
		 *  -tabNum - tabNum
		 *  -name - defineName
		 * 	-string - defintionGet
		 */
		String[] arrayDefineCalc = new String[5];
		Integer defineType = null;
		String defineName = null;
		String defintionGet = null;
		Boolean isGlobal = null;
		int paramMaxNum = 1;

		String[] coordsArrayDisp = null;
		String[] coordsArrayCalc = null;
		boolean testInt = false;
		boolean testFloat = false;
		boolean foundRepeat = false;

		/* defineType
		 * null = unspecified, will be specified after
		 * 0 = NULL
		 * 1 = string
		 * 2 = int
		 * 3 = float
		 * 4 = coords
		 * 5 = teleport
		 */

		// Removes "DEF" and for all arguments
		String statementEncase = this.fullLineGet.replaceFirst("DEF", "").replaceAll("^\\s+", "");
		switch (statementEncase.substring(0, statementEncase.indexOf(" "))) {
		case "GLOBAL":
			isGlobal = true;
			// removes GLOBAL
			statementEncase = statementEncase.substring(statementEncase.indexOf(" ") + 1);
			break;

		case "COORDS":
			defineType = 4;
			// removes COORDS
			statementEncase = statementEncase.substring(statementEncase.indexOf(" ") + 1);
			break;

		case "TELE":
			defineType = 5;
			// removes TELE
			statementEncase = statementEncase.substring(statementEncase.indexOf(" ") + 1);
			break;
		}

		// Gets second parameters
		if (statementEncase.contains(" ")) {
			switch (statementEncase.substring(0, statementEncase.indexOf(" "))) {
			case "GLOBAL":
				if (isGlobal == null) {
					isGlobal = true;
				} else {
					System.out.println(
							"ERROR: There are at least two parameters that conflict with each other in line '" + this.fullLineGet + "'");
					System.exit(0);
				}
				// removes GLOBAL
				statementEncase = statementEncase.substring(statementEncase.indexOf(" ") + 1, statementEncase.length());
				break;

			case "COORDS":
				if (defineType == null) {
					defineType = 4;
				} else {
					System.out.println(
							"ERROR: There are at least two parameters that conflict with each other in line '" + this.fullLineGet + "'");
					System.exit(0);
				}
				// removes COORDS
				statementEncase = statementEncase.substring(statementEncase.indexOf(" ") + 1, statementEncase.length());
				break;

			case "TELE":
				if (defineType == null) {
					defineType = 5;
				} else {
					System.out.println(
							"ERROR: There are at least two parameters that conflict with each other in line '" + this.fullLineGet + "'");
					System.exit(0);
				}
				// removes TELE
				statementEncase = statementEncase.substring(statementEncase.indexOf(" ") + 1, statementEncase.length());
				break;
			}
			// the end should make 'statementEncase' as the actual use thing (Name Definition)

			// Sets name
			if (statementEncase.contains(" ")) {
				defineName = statementEncase.substring(0, statementEncase.indexOf(" "));
				defintionGet = statementEncase.substring(statementEncase.indexOf(" ") + 1, statementEncase.length());

				// Checks of defineName is literally nothing
				if (defineName.trim().length() == 0) {
					System.out.println("ERROR: Definition '" + this.fullLineGet + "' is blank");
					System.exit(0);
				}

				// Checks if the name matches any unacceptable define names
				for (String checkException : exceptionArray) {
					if (defineName.equals(checkException)) {
						System.out.println("ERROR: A definition cannot be '" + defineName + "' in line '" + this.fullLineGet + "'");
						System.exit(0);
					}
				}

				// Checks how many parameters exist (up to 1000 because why the hell would you need more than 1000 parameters)
				for (int i = 0; i < 1000; i++) {
					if (defintionGet.contains("|" + i + "|")) {
						paramMaxNum = i + 1;
					}
				}

			} else {
				System.out.println("ERROR: '" + this.fullLineGet + "' does not define anything without spaces");
				System.exit(0);
			}

			// sets options if they are unspecified
			if (isGlobal == null) {
				isGlobal = false;
			}
			// detects definition type if not specified

			if (defineType == null) {
				try {
					Integer.parseInt(defintionGet);
					testInt = true;
				} catch (NumberFormatException e) {
				}
				if (testInt == true) {
					// is int
					defineType = 2;
				}

				try {
					Float.parseFloat(defintionGet);
					testFloat = true;
				} catch (NumberFormatException e) {
				}
				if (testFloat == true && defineType == null) {
					// is float
					defineType = 3;
					/* This part is to round
					if (defintionGet.substring(defintionGet.indexOf(".")).length() >= 11) {
						defintionGet = defintionGet.substring(0, defintionGet.indexOf(".") + 10);
					}*/
				}
			}

			// Sets to string
			if (defineType == null) {
				defineType = 1;
			}

			// tests whether coords works
			if (defineType == 4 || defineType == 5) {
				coordsArrayDisp = defintionGet.split(" ");
				coordsArrayCalc = defintionGet.replace("~", "0").split(" ");
				if (((coordsArrayCalc.length == 3 || coordsArrayCalc.length == 6) && defineType == 4)
						|| (coordsArrayCalc.length == 5 && defineType == 5)) {
					for (int i = 0; i < coordsArrayCalc.length; i++) {
						try {
							Float.parseFloat(coordsArrayCalc[i]);
						} catch (NumberFormatException e) {
							System.out.println("ERROR: '" + coordsArrayDisp[i] + "' in line '" + this.fullLineGet + "' must be a number");
							System.exit(0);
						}
					}
				} else {
					if (defineType == 4) {
						System.out.println("ERROR: Coordinates must be a set of 3 or 6 numbers in line '" + this.fullLineGet + "'");
					} else {
						System.out.println("ERROR: Teleport coordinates must be a set of 5 numbers in line '" + this.fullLineGet + "'");
					}
					System.exit(0);

				}
			}

			// If global, tabnum = 0
			if (isGlobal == true) {
				this.tabNum = 0;
			}

			arrayDefineCalc[0] = defineType.toString();
			arrayDefineCalc[1] = tabNum + "";
			arrayDefineCalc[2] = defineName;
			arrayDefineCalc[3] = defintionGet;
			arrayDefineCalc[4] = paramMaxNum + "";

			// Checks whether the defineName and tabnum is the same anywhere --> will remove
			for (int i = 0; i < arrayDefineSave.size(); i++) {
				if (arrayDefineSave.get(i)[2].equals(defineName) && arrayDefineSave.get(i)[1].equals(tabNum + "")) {
					arrayDefineSave.set(i, arrayDefineCalc);
					foundRepeat = true;
				}
			}

			// System.out.println(arrayDefineCalc[2] + " | " + arrayDefineCalc[1]);

			if (foundRepeat == false) {
				arrayDefineSave.add(arrayDefineCalc);
			}
		}

		return null;
	}

}