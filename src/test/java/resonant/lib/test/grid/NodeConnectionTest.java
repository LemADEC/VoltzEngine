package resonant.lib.test.grid;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.grid.INodeProvider;
import resonant.content.wrapper.BlockDummy;
import resonant.lib.grid.branch.NodeBranchPart;
import resonant.lib.prefab.TileConductor;
import resonant.lib.test.world.FakeWorld;
import resonant.lib.transform.vector.VectorWorld;

import java.util.Map;

/**
 * Created by robert on 11/20/2014.
 */
public class NodeConnectionTest extends TestCase
{
    private static Block wire;

    private FakeWorld world;
    VectorWorld center;

    public void testWireExists()
    {
        assertNotNull("Test can't continue without wire being created", wire);
        assertNotNull("Wire is not in the block registry", Block.blockRegistry.getObject("wire"));
    }

    public void testForNodes()
    {
        for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
        {
            //Build
            buildWireInDir(ForgeDirection.UNKNOWN);
            buildWireInDir(side);

            //Test
            VectorWorld vec = center.add(side);
            TileEntity tile = vec.getTileEntity();
            if(tile instanceof INodeProvider)
            {
                NodeBranchPart part = ((INodeProvider) tile).getNode(NodeBranchPart.class, side.getOpposite());
                if(part == null)
                    fail("Failed to get NodeBranchPart from tile at " + vec + " from side " + side.getOpposite());
            }
            else
            {
                fail("Something failed good as the wire is not an instance of INodeProvider");
            }

            //Cleanup
            center.setBlockToAir();
            center.add(side).setBlockToAir();
        }
    }

    public void testSingleConnections()
    {
        for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
        {
            //Build
            buildWireInDir(ForgeDirection.UNKNOWN);
            buildWireInDir(side);
            centerNode().buildConnections();

            //Test
            checkForOnlyConnection(side);

            //Cleanup
            center.setBlockToAir();
            center.add(side).setBlockToAir();
        }
    }

    //Full all side connection test
    public void testAllSides()
    {
        buildFullWireSet();

        //Trigger connection building in the wire
        TileEntity tile = center.getTileEntity();
        if(tile instanceof TileConductor)
        {
            ((TileConductor) tile).getNode().buildConnections();
        }

        //Test connections
        assertNotNull("There should be a tile at Vec(8,8,8)", tile);
        if(tile instanceof TileConductor)
        {
            NodeBranchPart node = ((TileConductor) tile).getNode();
            assertNotNull("There should be a node at Vec(8,8,8)", node);
            if (node.getConnections().size() == 0)
                fail("Should be at least one connection");
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                boolean found = false;
                for(Map.Entry<NodeBranchPart, ForgeDirection> entry : node.getConnections().entrySet())
                {
                    if(entry.getValue() == dir.getOpposite())
                    {
                        found = true;
                    }
                }
                if(!found)
                    fail("No " + dir + " connection found");
            }
        }

        //Clean up
        world.clear();
    }

    private void checkForOnlyConnection(ForgeDirection dir)
    {
        assertEquals("Should only have one connection", centerNode().getConnections().size(), 1, 0);
        for(ForgeDirection side : ForgeDirection.values())
        {
            if(side != dir)
                fail("Should only contain connection on the " + dir + " side");
        }
    }

    private void buildFullWireSet()
    {
        //Surround wire with wires
        for(ForgeDirection dir : ForgeDirection.values())
        {
           buildWireInDir(dir);
        }
    }

    private void buildWireInDir(ForgeDirection dir)
    {
        VectorWorld vec = center.add(dir);
        vec.setBlock(wire);

        assertNotNull("Failed to place wire at " + vec, vec.getBlock());
        assertNotNull("Failed to place tile at " + vec, vec.getTileEntity());
    }

    private NodeBranchPart centerNode()
    {
        TileEntity tile = center.getTileEntity();
        if(tile instanceof TileConductor)
        {
            return ((TileConductor) tile).getNode();
        }
        return null;
    }

    @Override
    protected void setUp() throws Exception
    {
        world = new FakeWorld();
        center = new VectorWorld(world, 8, 8, 8);
        //Create wire to test with
        if(Block.blockRegistry.getObject("wire") == null)
        {
            wire = new BlockDummy("JUnit", null, new TileConductor());
            Block.blockRegistry.addObject(175, "wire", wire);
        }
        else
        {
            wire = (Block) Block.blockRegistry.getObject("wire");
        }

    }

    @Override
    protected void tearDown() throws Exception
    {
        world.clear();
        center = null;
        world = null;
    }
}
