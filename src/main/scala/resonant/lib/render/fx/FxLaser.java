package resonant.lib.render.fx;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import resonant.engine.References;
import universalelectricity.core.transform.vector.IVector3;

@SideOnly(Side.CLIENT)
public class FxLaser extends FxBeam
{
	public FxLaser(World world, IVector3 position, IVector3 target, float red, float green, float blue, int age)
	{
		super(new ResourceLocation(References.DOMAIN, References.TEXTURE_DIRECTORY + "laser.png"), world, position, target, red, green, blue, age);
	}
}