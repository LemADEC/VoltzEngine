package com.builtbroken.mc.lib.world.radar;

import com.builtbroken.jlib.lang.DebugPrinter;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.lib.world.radar.data.RadarEntity;
import com.builtbroken.mc.lib.world.radar.data.RadarObject;
import com.builtbroken.mc.lib.world.radar.data.RadarTile;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

/**
 * System designed to track moving or stationary targets on a 2D map. Can be used to detect objects or visualize objects in an area. Mainly
 * used to track flying objects that are outside of the map bounds(Missile in ICBM).
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/5/2016.
 */
public class RadarMap
{
    public static final int UPDATE_DELAY = 20;

    /** DIM ID, never change */
    public final int dimID;

    /** Map of chunk coords( converted to long) to radar contacts in that chunk */
    public final HashMap<ChunkCoordIntPair, List<RadarObject>> chunk_to_entities = new HashMap();
    public final List<RadarObject> allEntities = new ArrayList();

    public int ticks = 0;

    /** Debug printer */
    public DebugPrinter debug;

    /** Enables debug display, game still needs to be in dev mode to work */
    public boolean debugRadarMap = false;

    /**
     * Dimension ID
     *
     * @param dimID - unique dimension that is not already tracked
     */
    public RadarMap(int dimID)
    {
        this.dimID = dimID;
        debug = new DebugPrinter(Engine.logger());
        if (!Engine.runningAsDev || !debugRadarMap)
        {
            debug.disable();
        }
    }

    public void setDebugEnabled(boolean b)
    {
        debugRadarMap = b;
        if (debugRadarMap)
        {
            debug.enable();
        }
        else
        {
            debug.disable();
        }
    }

    /**
     * Called at the end of every world tick to do checks on
     * data stored.
     */
    public void update()
    {
        debug.start("Update", "Objects: " + allEntities.size() + "  Chunks: " + chunk_to_entities.size());
        if (ticks++ >= UPDATE_DELAY && chunk_to_entities.size() > 0)
        {
            ticks = 0;
            //TODO consider multi-threading if number of entries is too high (need to ensure runs in less than 10ms~)


            debug.start("Looking for invalid radar objects and updating position data");
            HashMap<RadarObject, ChunkCoordIntPair> removeList = new HashMap();
            List<RadarObject> addList = new ArrayList();
            for (Map.Entry<ChunkCoordIntPair, List<RadarObject>> entry : chunk_to_entities.entrySet())
            {
                if (entry.getValue() != null)
                {
                    for (RadarObject object : entry.getValue())
                    {
                        if (entry.getKey() != object.getChunkCoordIntPair())
                        {
                            debug.log("Removed from map: " + object);
                            removeList.put(object, entry.getKey());
                            if (object.isValid())
                            {
                                addList.add(object);
                                debug.log("Queued for re-add");
                            }
                        }
                    }
                }
            }
            debug.end();


            debug.start("Removing objects from map");
            for (Map.Entry<RadarObject, ChunkCoordIntPair> entry : removeList.entrySet())
            {
                allEntities.remove(entry.getKey());
                List<RadarObject> list = chunk_to_entities.get(entry.getValue());
                if (list != null)
                {
                    list.remove(entry.getKey());
                    if (list.size() > 0)
                    {
                        chunk_to_entities.put(entry.getValue(), list);
                    }
                    else
                    {
                        chunk_to_entities.remove(entry.getValue());
                    }
                }
                else
                {
                    chunk_to_entities.remove(entry.getValue());
                }
            }
            debug.end();


            debug.start("Adding entries: " + addList.size());
            addList.forEach(this::add);
            debug.end();

            debug.start("Removing invalid objects");
            Iterator<RadarObject> it = allEntities.iterator();
            while (it.hasNext())
            {
                RadarObject object = it.next();
                if (!object.isValid())
                {
                    debug.log("Removed: " + object);
                    it.remove();
                }
            }
            debug.end();
        }
        debug.end();
    }

    public boolean add(Entity entity)
    {
        return add(new RadarEntity(entity));
    }

    public boolean add(TileEntity tile)
    {
        return add(new RadarTile(tile));
    }

    public boolean add(RadarObject object)
    {
        if (!allEntities.contains(object) && object.isValid())
        {
            allEntities.add(object);
            ChunkCoordIntPair pair = getChunkValue((int) object.x(), (int) object.z());
            List<RadarObject> list;

            //Get list or make new
            if (chunk_to_entities.containsKey(pair))
            {
                list = chunk_to_entities.get(pair);
            }
            else
            {
                list = new ArrayList();
            }

            //Check if object is not already added
            if (!list.contains(object))
            {
                list.add(object);
                //TODO fire map update event
                //TODO fire map add event
                //Update map
                chunk_to_entities.put(pair, list);
                return true;
            }
        }
        return false;
    }

    public boolean remove(Entity entity)
    {
        return remove(new RadarEntity(entity));
    }

    public boolean remove(TileEntity tile)
    {
        return remove(new RadarTile(tile));
    }

    public boolean remove(RadarObject object)
    {
        ChunkCoordIntPair pair = getChunkValue((int) object.x(), (int) object.z());
        allEntities.remove(object);
        if (chunk_to_entities.containsKey(pair))
        {
            List<RadarObject> list = chunk_to_entities.get(pair);
            boolean b = list.remove(object);
            //TODO fire radar remove event
            //TODO fire map update event
            if (list.isEmpty())
            {
                chunk_to_entities.remove(pair);
            }
            return b;
        }
        return false;
    }

    /**
     * Removes all entries connected with the provided chunk location data
     *
     * @param chunk - should never be null
     */
    public void remove(Chunk chunk)
    {
        ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
        if (chunk_to_entities.containsKey(pair))
        {
            for (RadarObject object : chunk_to_entities.get(pair))
            {
                //TODO fire remove event
                allEntities.remove(object);
            }
            chunk_to_entities.remove(pair);
        }
    }

    protected final ChunkCoordIntPair getChunkValue(int x, int z)
    {
        return new ChunkCoordIntPair(x >> 4, z >> 4);
    }

    public void unloadAll()
    {
        chunk_to_entities.clear();
    }

    /**
     * Finds all contacts within chunk distances
     *
     * @param x        - world location x
     * @param z        - world location x
     * @param distance - distance m
     * @return list of entries
     */
    public List<RadarObject> getRadarObjects(double x, double z, double distance)
    {
        return getRadarObjects(new Cube(x - distance, 0, z - distance, x + distance, 255, z + distance).cropToWorld(), true);
    }

    /**
     * Finds all contacts within chunk distances
     *
     * @param cube  - area to search inside, approximated to chunk bounds
     * @param exact - match exact cube size, overrides approximation
     * @return list of entries
     */
    public List<RadarObject> getRadarObjects(Cube cube, boolean exact)
    {
        List<RadarObject> list = new ArrayList();
        for (int chunkX = (cube.min().xi() >> 4) - 1; chunkX <= (cube.max().xi() >> 4) + 1; chunkX++)
        {
            for (int chunkZ = (cube.min().zi() >> 4) - 1; chunkZ <= (cube.max().zi() >> 4) + 1; chunkZ++)
            {
                ChunkCoordIntPair p = new ChunkCoordIntPair(chunkX, chunkZ);
                if (chunk_to_entities.containsKey(p))
                {
                    List<RadarObject> objects = chunk_to_entities.get(p);
                    if (objects != null)
                    {
                        if (exact)
                        {
                            for (RadarObject object : objects)
                            {
                                if (object.isValid())
                                {
                                    if (cube.isWithin(object.x(), object.y(), object.z()))
                                    {
                                        list.add(object);
                                    }
                                }
                            }
                        }
                        else
                        {
                            list.addAll(objects);
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * Dimension ID this map tracks
     *
     * @return valid dim ID.
     */
    public int dimID()
    {
        return dimID;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this)
        {
            return true;
        }
        else if (object instanceof RadarMap)
        {
            return ((RadarMap) object).dimID == dimID;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "RadarMap[" + dimID + "]";
    }


}
