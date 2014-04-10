package calclavia.lib.config;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import universalelectricity.core.asm.ASMHelper;

import java.util.Set;

/**
 * @since 09/03/14
 * @author tgame14
 */
public class ConfigTransformer implements IClassTransformer
{
	public static final Set<String> classes = new ConfigSet();

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		// no need to iterate over Forge, FML, and MC source
		if (transformedName.startsWith("net.minecraft") || transformedName.startsWith("cpw.mods.fml") || transformedName.startsWith("universalelectricity."))
			return bytes;

		ClassNode cnode = ASMHelper.createClassNode(bytes);

		for (FieldNode fnode : cnode.fields)
		{
			if (fnode != null && fnode.visibleAnnotations != null)
			{
				for (AnnotationNode anode : fnode.visibleAnnotations)
				{
					if (anode.desc.equals("Lcalclavia/lib/config/Config;"))
					{
						classes.add(transformedName);
						return bytes;
					}
				}
			}
		}

		return bytes;

	}

}
