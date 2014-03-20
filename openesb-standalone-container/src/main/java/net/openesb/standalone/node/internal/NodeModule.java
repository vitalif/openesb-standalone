package net.openesb.standalone.node.internal;

import com.google.inject.AbstractModule;
import net.openesb.standalone.node.Node;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class NodeModule extends AbstractModule {

    private final Node node;

    public NodeModule(Node node) {
        this.node = node;
    }

    @Override
    protected void configure() {
        bind(Node.class).toInstance(node);
    }    
}
