package net.openesb.standalone.http.handlers;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public interface Handler<T> {

    T getHandler();
    
    String path();
}
