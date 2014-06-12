package net.openesb.standalone.logger;

import java.util.logging.LogRecord;
import org.fusesource.jansi.AnsiConsole;

/**
 * Color Console Handler for jdk: using jansi (http://jansi.fusesource.org/)
 * 
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class JAnsiColorConsoleHandler extends BaseColorConsoleHandler {
    
    @Override
    public void publish(LogRecord record) {
        AnsiConsole.err.print(logRecordToString(record));
        AnsiConsole.err.flush();
    }
}
