package calclavia.lib.schematic;

import java.util.HashMap;

import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.Pair;

/**
 * Stores building structure data.
 * 
 * TODO: MFFS integration for saving and loading custom modes.
 * 
 * @author Calclavia
 * 
 */
public abstract class Schematic
{
	/**
	 * The name of the schematic that is unlocalized.
	 * 
	 * @return "schematic.NAME-OF-SCHEMATIC.name"
	 */
	public abstract String getName();

	/**
	 * Gets the structure of the schematic.
	 * @param size - The size multiplier.
	 * @return A Hashmap of positions and block IDs with metadata.
	 */
	public abstract HashMap<Vector3, Pair<Integer, Integer>> getStructure(int size);
}
