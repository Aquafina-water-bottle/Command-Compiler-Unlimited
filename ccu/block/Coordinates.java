package ccu.block;

import ccu.command.Var_Options;
import ccu.general.GeneralFile;

public class Coordinates {
	// Okay technically I copied/pasted this class from CBP
	// however, I edited it a lot to the point where I'm about 99% sure it's not even close to
	// what it previously was so I guess this is fine idk
	private int x;
	private int y;
	private int z;
	private String relativeX = "";
	private String relativeY = "";
	private String relativeZ = "";

	// Constructor if coordinates aren't specified

	public Coordinates() {
		this(0, 0, 0);
	}

	// Constructor if coordinates are specified
	public Coordinates(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// Seeing if coordinates should be relative
	/*
	public void RelativeCoords(boolean x, boolean y, boolean z) {
		this.relativeX = x;
		this.relativeY = y;
		this.relativeZ = z;
	}*/

	/*
	// Constructor for array
	public Coordinates(int[] coor) {
		if ((coor == null) || (coor.length != 3)) {
			coor = new int[3];
		} else {
			System.out.println("wtf is this");
			System.out.println(coor);
		}
		this.x = coor[0];
		this.y = coor[1];
		this.z = coor[2];
		// System.out.println(this.x + " " + this.y + " " + this.z);
	}*/

	// Set coordinates
	/*
	public void setCoordinates(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}*/

	// Set coordinates using a string
	public void setCoordinates(String coords) {
		final String[] tempCoordsArray = coords.split(" ");
		if (tempCoordsArray.length == 3) {
			if (tempCoordsArray[0].substring(0, 1).equals("~")) {
				relativeX = "~";
				tempCoordsArray[0] = tempCoordsArray[0].substring(1);
			}
			if (tempCoordsArray[1].substring(0, 1).equals("~")) {
				relativeY = "~";
				tempCoordsArray[1] = tempCoordsArray[1].substring(1);
			}
			if (tempCoordsArray[2].substring(0, 1).equals("~")) {
				relativeZ = "~";
				tempCoordsArray[2] = tempCoordsArray[2].substring(1);
			}
			try {
				this.x = Integer.parseInt(tempCoordsArray[0]);
				this.y = Integer.parseInt(tempCoordsArray[1]);
				this.z = Integer.parseInt(tempCoordsArray[2]);
			} catch (NumberFormatException e) {
				System.out.println("ERROR: coordsOption must be in integers");
				System.exit(0);
			} catch (Exception e) {
				GeneralFile.dispError(e);
				System.exit(0);
			}
		} else {
			System.out.println("ERROR: coordsOption contains an incorrect number of coordinate values (must be 3)");
			System.exit(0);
		}
	}

	// Add coordinates
	public Coordinates addCoordinates(Coordinates coor) {
		this.x += coor.x;
		this.y += coor.y;
		this.z += coor.z;
		
		if (coor.relativeX.equals("~")) {
			this.relativeX = "~";
		}
		if (coor.relativeY.equals("~")) {
			this.relativeY = "~";
		}
		if (coor.relativeZ.equals("~")) {
			this.relativeZ = "~";
		}
		
		return this;
	}

	public Coordinates addCoordinates(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	// Add offset
	public Coordinates addOffset(int[] offset) {
		if ((offset == null) || (offset.length != 3)) {
			offset = new int[3];
		}
		this.x += offset[0];
		this.y += offset[1];
		this.z += offset[2];
		return this;
	}
	
	public Coordinates switchDirection() {
		int tempZ = this.z;
		int tempX = this.x;
		if (GroupStructure.styleOptionXZ.equals("+Z")) {
			this.z = tempX + 0;
			this.x = tempZ + 0;
			return this;
		} else {
			if (GroupStructure.styleOptionXZ.equals("-Z")) {
				this.z = 16 - tempX;
				this.x = 16 - tempZ;
				return this;
			} else {
				if (GroupStructure.styleOptionXZ.equals("-X")) {
					tempX = 16 - this.z;
					tempZ = 16 - this.x;
					this.z = tempX + 0;
					this.x = tempZ + 0;
					return this;
				} else {
					if (GroupStructure.styleOptionXZ.equals("+X") == false) {
						System.out.println("ERROR: Option styleOption '" + Var_Options.styleOption + "' is invalid");
						System.exit(0);
					}
				}
			}
		}
		return this;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getZ() {
		return this.z;
	}

	public int[] getArray() {
		return new int[] {this.x, this.y, this.z};
	}

	// get string
	public String getString() {
		return this.relativeX + this.x + " " + this.relativeY + this.y + " " + this.relativeZ + this.z;
	}
}