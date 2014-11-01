package net.openesb.standalone.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class LogFormatter extends Formatter {

    private static final String RFC_3339_DATE_FORMAT
	    = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final SimpleDateFormat dateFormatter
	    = new SimpleDateFormat(RFC_3339_DATE_FORMAT);

    private static final String LINE_SEPARATOR
	    = (String) java.security.AccessController.doPrivileged(
		    new sun.security.action.GetPropertyAction("line.separator"));
    
    private static final String RECORD_BEGIN_MARKER = "[#|";
    private static final String RECORD_END_MARKER = "|#]" + LINE_SEPARATOR;
    
    private static final char FIELD_SEPARATOR = ' ';
    private static final char NVPAIR_SEPARATOR = ';';
    private static final char NV_SEPARATOR = '=';
    
    private static final char SEPARATOR_BEGIN_MARKER = '[';
    private static final char SEPARATOR_END_MARKER = ']';

    private static final char SEPARATOR2_BEGIN_MARKER = '(';
    private static final char SEPARATOR2_END_MARKER = ')';
    
    // A Dummy Container Date Object is used to format the date
    private Date date = new Date();

    @Override
    public String format(LogRecord record) {
	StringBuilder recordBuffer = new StringBuilder(RECORD_BEGIN_MARKER);
	// The following operations are to format the date and time in a
	// human readable  format.
	// _REVISIT_: Use HiResolution timer to analyze the number of
	// Microseconds spent on formatting date object
	date.setTime(record.getMillis());
	recordBuffer.append(dateFormatter.format(date));
	recordBuffer.append(FIELD_SEPARATOR);

	recordBuffer.append(record.getLevel()).append(FIELD_SEPARATOR);
	
	recordBuffer
		.append(SEPARATOR_BEGIN_MARKER)
		.append(record.getLoggerName())
		.append(SEPARATOR_END_MARKER)
		.append(FIELD_SEPARATOR);

	recordBuffer
		.append(SEPARATOR2_BEGIN_MARKER)
		.append(Thread.currentThread().getName())
		.append(SEPARATOR2_END_MARKER);

	Level level = record.getLevel();
	if (level.intValue() <= Level.FINE.intValue()) {
	    recordBuffer.append("ClassName").append(NV_SEPARATOR);
	    recordBuffer.append(record.getSourceClassName());
	    recordBuffer.append(NVPAIR_SEPARATOR);
	    recordBuffer.append("MethodName").append(NV_SEPARATOR);
	    recordBuffer.append(record.getSourceMethodName());
	    recordBuffer.append(NVPAIR_SEPARATOR);
	}

	recordBuffer.append(FIELD_SEPARATOR);

	recordBuffer.append(formatMessage(record));

	if (record.getThrown() != null) {
	    recordBuffer.append(LINE_SEPARATOR);
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    record.getThrown().printStackTrace(pw);
	    pw.close();
	    recordBuffer.append(sw.toString());
	}

	recordBuffer.append(RECORD_END_MARKER);
	
	return recordBuffer.toString();
    }

}
