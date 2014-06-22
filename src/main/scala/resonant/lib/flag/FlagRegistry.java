package resonant.lib.flag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * All the different types of flags that can be registered.
 *
 * @author Calclavia
 */
public class FlagRegistry
{
	public static final String DEFAULT_NAME = "ModFlags";
	public static final List<String> flags = new ArrayList<String>();
	private static final HashMap<String, ModFlag> MOD_FLAGS = new HashMap<String, ModFlag>();
	public static boolean isInitiated = false;

	public static void registerModFlag(String name, ModFlag flagData)
	{
		MOD_FLAGS.put(name, flagData);
	}

	public static ModFlag getModFlag()
	{
		return getModFlag(DEFAULT_NAME);
	}

	public static ModFlag getModFlag(String name)
	{
		return MOD_FLAGS.get(name);
	}

	/**
	 * Registers a flag name, allowing it to be used and called by the player. Call this in your
	 * mod's init function.
	 */
	public static String registerFlag(String name)
	{
		if (!isInitiated)
		{
			isInitiated = true;
		}

		name = name.toLowerCase();

		if (!flags.contains(name))
		{
			flags.add(name);
		}

		return name;
	}
}
