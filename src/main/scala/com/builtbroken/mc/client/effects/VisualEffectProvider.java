package com.builtbroken.mc.client.effects;

import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Called to generate a graphic effect into the world from data received
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/15/2017.
 */
public abstract class VisualEffectProvider
{
    public final String name;

    public VisualEffectProvider(String name)
    {
        this.name = name;
    }

    public void displayEffect(World world, double x, double y, double z, double mx, double my, double mz, boolean movementIsEndpoint, NBTTagCompound otherData)
    {
        if (movementIsEndpoint)
        {
            Pos pos = new Pos(mx, my, mz).sub(x, y, z).normalize();
            if (otherData.hasKey("vel"))
            {
                pos = pos.multiply(otherData.getFloat("vel"));
            }
            mx = pos.x();
            my = pos.y();
            mz = pos.z();
        }
        displayEffect(world, x, y, z, mx, my, mz, otherData);
    }

    public void displayEffect(World world, double x, double y, double z, double mx, double my, double mz, NBTTagCompound otherData)
    {

    }
}
