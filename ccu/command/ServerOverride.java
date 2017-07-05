package ccu.command;

import ccu.general.ReadConfig;

public class ServerOverride {
	public static String getCommand(String getString, int tabNum) {

		String getWhiteSpace = null;
		String shortcutCalc = null;
		String[] shortcutCalcArray = null;
		String shortcutResultCalc = null;
		boolean changedLine = false;
		boolean shortcutDataTag = false;

		getWhiteSpace = getString.substring(0, tabNum);
		shortcutCalc = getString.substring(tabNum);
		shortcutCalcArray = shortcutCalc.split(" ");

		for (int j = 0; j < shortcutCalcArray.length; j++) {

			// if it's a data tag --> 3 (DATATAG)
			if (shortcutCalcArray[j].startsWith("{")) {
				shortcutDataTag = true;
			}
			if (shortcutDataTag == false) {
				for (String cmdGet : ReadConfig.serverOverrideArray) {
					if (shortcutCalcArray[j].equals(cmdGet)) {
						shortcutCalcArray[j] = "minecraft:" + shortcutCalcArray[j];
						changedLine = true;
						break;
					}
				}
			}
			
		}

		if (changedLine == true) {
			for (int j = 0; j < shortcutCalcArray.length; j++)
				if (j == 0) {
					shortcutResultCalc = getWhiteSpace + shortcutCalcArray[j];
				} else {
					if (shortcutCalcArray[j].equals("") == false) {
						shortcutResultCalc += " " + shortcutCalcArray[j];
					}
				}
			return shortcutResultCalc;
		} else {
			return null;
		}
	}
}
