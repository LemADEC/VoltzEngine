package universalelectricity.api.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.world.World;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;

/**
 * Extend from this class if your item requires electricity or to be charged. Optionally, you can
 * implement IItemElectric instead.
 * 
 * @author Calclavia
 */
public abstract class ItemElectric extends Item implements IEnergyItem, IVoltageItem
{
	public ItemElectric(int id)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
		this.setNoRepair();
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
	{
		String color = "";
		long joules = this.getEnergy(itemStack);

		if (joules <= this.getEnergyCapacity(itemStack) / 3)
		{
			color = "\u00a74";
		}
		else if (joules > this.getEnergyCapacity(itemStack) * 2 / 3)
		{
			color = "\u00a72";
		}
		else
		{
			color = "\u00a76";
		}

		list.add(color + UnitDisplay.getDisplayShort(joules, Unit.JOULES) + "/" + UnitDisplay.getDisplayShort(this.getEnergyCapacity(itemStack), Unit.JOULES));
	}

	/**
	 * Makes sure the item is uncharged when it is crafted and not charged. Change this if you do
	 * not want this to happen!
	 */
	@Override
	public void onCreated(ItemStack itemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		this.setEnergy(itemStack, 0);
	}

	@Override
	public long recharge(ItemStack itemStack, long energy, boolean doReceive)
	{
		long rejectedElectricity = Math.max((this.getEnergy(itemStack) + energy) - this.getEnergyCapacity(itemStack), 0);
		long energyToReceive = energy - rejectedElectricity;

		if (doReceive)
		{
			this.setEnergy(itemStack, this.getEnergy(itemStack) + energyToReceive);
		}

		return energyToReceive;
	}

	@Override
	public long discharge(ItemStack itemStack, long energy, boolean doTransfer)
	{
		long energyToTransfer = Math.min(this.getEnergy(itemStack), energy);

		if (doTransfer)
		{
			this.setEnergy(itemStack, this.getEnergy(itemStack) - energyToTransfer);
		}

		return energyToTransfer;
	}

	@Override
	public long getVoltage(ItemStack itemStack)
	{
		return UniversalElectricity.DEFAULT_VOLTAGE;
	}

	@Override
	public void setEnergy(ItemStack itemStack, long joules)
	{
		// Saves the frequency in the ItemStack
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		long electricityStored = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0);
		itemStack.getTagCompound().setLong("electricity", electricityStored);

		/** Sets the damage as a percentage to render the bar properly. */
		itemStack.setItemDamage((int) (100 - (electricityStored / getEnergyCapacity(itemStack)) * 100));
	}

	public long getTransfer(ItemStack itemStack)
	{
		return this.getEnergyCapacity(itemStack) - this.getEnergy(itemStack);
	}

	/** Gets the energy stored in the item. Energy is stored using item NBT */
	@Override
	public long getEnergy(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		long energyStored = 0;

		if (itemStack.getTagCompound().hasKey("electricity"))
		{
			NBTBase obj = itemStack.getTagCompound().getTag("electricity");

			if (obj instanceof NBTTagLong)
			{
				energyStored = (long) ((NBTTagLong) obj).data;
			}
			else if (obj instanceof NBTTagDouble)
			{
				energyStored = (long) ((NBTTagDouble) obj).data;
			}
			else if (obj instanceof NBTTagFloat)
			{
				energyStored = (long) ((NBTTagFloat) obj).data;
			}
			else
			{
				energyStored = itemStack.getTagCompound().getLong("electricity");
			}
		}

		/** Sets the damage as a percentage to render the bar properly. */
		itemStack.setItemDamage((int) (100 - (energyStored / getEnergyCapacity(itemStack)) * 100));
		return energyStored;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(CompatibilityModule.getItemWithCharge(new ItemStack(this), 0));
		par3List.add(CompatibilityModule.getItemWithCharge(new ItemStack(this), this.getEnergyCapacity(new ItemStack(this))));
	}
}
