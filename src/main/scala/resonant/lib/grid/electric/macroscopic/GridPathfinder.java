package resonant.lib.grid.electric.macroscopic;

import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.lib.grid.electric.macroscopic.part.Branch;
import resonant.lib.grid.electric.macroscopic.part.Junction;
import resonant.lib.grid.electric.macroscopic.part.Part;
import resonant.lib.grid.node.NodeConnector;

import java.util.*;

/**
 * Simple connection path finder that generates grid parts while pathing all routes from a single node.
 *
 * @author Darkguardsman, Calclavia
 */
public class GridPathfinder
{
    /**
     * Network that is being pathed
     */
    private BranchedGrid<NodeConnector> grid;

    /**
     * All parts created by the path finder
     */
    private Set<Part> parts = new HashSet<Part>();

    /**
     * Nodes that have already been pathed
     */
    private List<NodeConnector> pathNodes = new LinkedList<NodeConnector>();

    public GridPathfinder(BranchedGrid grid)
    {
        this.grid = grid;
    }

    /**
     * Starts the path finder to generate grid parts from a list of nodes
     *
     * @return list of NetworkParts
     */
    public Set<Part> generateParts()
    {
        NodeConnector firstNode = grid.getFirstNode();
        if (firstNode != null)
        {
            path(null, firstNode, null);
        }
        return parts;
    }

    /**
     * Triggers a pathfinding loop from the node through all its connections and those node's connections.
     * Does not end until all connections are plotted, and creates new NetworkParts when required
     *
     * @param part        - last part created, used to connect new parts to, can be null for first run
     * @param currentNode - current node being pathed
     * @param side        - side we are pathing to from the node, can only be null for first run
     */
    public void path(Part part, NodeConnector currentNode, ForgeDirection side)
    {
        Map<Object, ForgeDirection> connections = currentNode.directionMap();
        Part nextPart = null;
        pathNodes.add(currentNode);

        //More than two connections, wire is a junction connecting to several paths
        if (connections.size() > 2)
        {
            //Connection new junction to last part
            if (part instanceof Branch)
            {
                ((Branch) part).setConnectionB(nextPart);

                //Create new junction
                nextPart = new Junction();
                nextPart.add(currentNode);
                ((Junction) nextPart).addConnection(part);
            }//If we have another junction point merge it into a single junction
            else if (part instanceof Junction)
            {
                ((Junction) part).add(currentNode);
                nextPart = part;
            }
        }//Wire is a path only connecting in two directions
        else
        {
            //If the last part was a wire add this wire to it
            if (part instanceof Branch)
            {
                ((Branch) part).add(currentNode);
                nextPart = part;
            } else
            {
                //Create a new wire and connect it to old part
                nextPart = new Branch();
                ((Branch) nextPart).add(currentNode);
                if (part != null)
                {
                    ((Branch) nextPart).setConnectionA(part);
                }
                if (part instanceof Junction)
                {
                    ((Junction) part).addConnection(nextPart);
                }
            }
        }

        //Loop threw all connection triggering path() on each instance of NetworkNode
        for (Map.Entry<Object, ForgeDirection> entry : connections.entrySet())
        {
            if (entry.getKey() != null)
            {
                if (entry.getKey() instanceof NodeConnector)
                {
                    if (!pathNodes.contains(entry.getKey()))
                    {
                        path(nextPart, (NodeConnector) entry.getKey(), entry.getValue());
                    }
                } else if (entry.getKey() instanceof INodeProvider)
                {
                    INode providerNode = ((INodeProvider) entry.getKey()).getNode(NodeConnector.class, entry.getValue().getOpposite());

                    if (providerNode instanceof NodeConnector)
                    {
                        if (!pathNodes.contains(entry.getKey()))
                        {
                            path(nextPart, (NodeConnector) entry.getKey(), entry.getValue());
                        }
                    }
                } else
                {
                    //TODO handle everything else as machines using an input & output logic
                }
            }
        }
    }

    /**
     * Clears out the path finder's results taking it back to a clean state
     */
    public void reset()
    {
        this.parts.clear();
        this.pathNodes.clear();
    }
}