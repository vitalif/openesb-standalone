package net.openesb.standalone.rest.security;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class Session {

    protected static final long MILLIS_PER_SECOND = 1000;
    protected static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    protected static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    /**
     * Default main session timeout value, equal to {@code 30} minutes.
     */
    public static final long DEFAULT_GLOBAL_SESSION_TIMEOUT = 30 * MILLIS_PER_MINUTE;
    private Serializable id;
    private Date startTimestamp;
    private Date stopTimestamp;
    private Date lastAccessTime;
    private boolean expired;
    private long timeout;

    public Session() {
        this.timeout = DEFAULT_GLOBAL_SESSION_TIMEOUT;
        this.startTimestamp = new Date();
        this.lastAccessTime = this.startTimestamp;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public Date getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Date getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(Date stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    protected boolean isTimedOut() {

        if (isExpired()) {
            return true;
        }

        long timeout = getTimeout();

        Date lastAccessTime = getLastAccessTime();

        if (lastAccessTime == null) {
            String msg = "session.lastAccessTime for session with id ["
                    + getId() + "] is null.  This value must be set at "
                    + "least once, preferably at least upon instantiation.  Please check the "
                    + getClass().getName() + " implementation and ensure "
                    + "this value will be set (perhaps in the constructor?)";
            throw new IllegalStateException(msg);
        }

        // Calculate at what time a session would have been last accessed
        // for it to be expired at this point.  In other words, subtract
        // from the current time the amount of time that a session can
        // be inactive before expiring.  If the session was last accessed
        // before this time, it is expired.
        long expireTimeMillis = System.currentTimeMillis() - timeout;
        Date expireTime = new Date(expireTimeMillis);
        return lastAccessTime.before(expireTime);
    }
    
    public void validate() throws Exception {
        //check for expiration
        if (isTimedOut()) {
            expire();

            //throw an exception explaining details of why it expired:
            Date lastAccessTime = getLastAccessTime();
            long timeout = getTimeout();

            Serializable sessionId = getId();

            DateFormat df = DateFormat.getInstance();
            String msg = "Session with id [" + sessionId + "] has expired. " +
                    "Last access time: " + df.format(lastAccessTime) +
                    ".  Current time: " + df.format(new Date()) +
                    ".  Session timeout is set to " + timeout / MILLIS_PER_SECOND + " seconds (" +
                    timeout / MILLIS_PER_MINUTE + " minutes)";

            throw new Exception(msg);
        }
    }
    
    public void access() {
        this.lastAccessTime = new Date();
    }
    
    public void expire() {
        this.setExpired(true);
    }
}
