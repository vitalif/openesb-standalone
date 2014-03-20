package net.openesb.standalone;

/**
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class LifecycleException extends RuntimeException {

    public LifecycleException() {
    }

    public LifecycleException(String message) {
        super(message);
    }

    public LifecycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
