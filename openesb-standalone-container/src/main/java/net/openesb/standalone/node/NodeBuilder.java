package net.openesb.standalone.node;

import net.openesb.standalone.node.internal.InstanceNode;

/**
 *
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public final class NodeBuilder {
    
    /**
     * A convenient factory method to create a {@link NodeBuilder}.
     */
    public static NodeBuilder nodeBuilder() {
        return new NodeBuilder();
    }
    
    /**
     * Builds the node without starting it.
     */
    public Node build() {
        return new InstanceNode();
    }
}
