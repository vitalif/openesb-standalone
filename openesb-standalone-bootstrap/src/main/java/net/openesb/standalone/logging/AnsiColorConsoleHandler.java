package net.openesb.standalone.logging;

import java.util.logging.LogRecord;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class AnsiColorConsoleHandler extends BaseColorConsoleHandler {
    
    @Override
    public void publish(LogRecord record) {
        System.err.print(logRecordToString(record));
    }
}