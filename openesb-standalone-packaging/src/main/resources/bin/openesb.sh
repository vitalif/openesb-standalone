#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  OpenESB Standalone runtime Bootstrap Script                             ##
##                                                                          ##
### ====================================================================== ###

DIRNAME=`dirname $0`
PROGNAME=`basename $0`

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
        
    Linux)
        linux=true
        ;;
esac

# Force IPv4 on Linux systems since IPv6 doesn't work correctly with jdk5 and lower
if [ "$linux" = "true" ]; then
   JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"
fi

# Setup OPENESB_HOME
if [ "x$OPENESB_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    OPENESB_HOME=`cd $DIRNAME/..; pwd`
fi

export OPENESB_HOME

export JAVA_OPTS="$JAVA_OPTS"

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
    JAVA="$JAVA_HOME/bin/java"
    else
    JAVA="java"
    fi
fi

# Setup the classpath
runjar="$OPENESB_HOME/lib/openesb-standalone-bootstrap.jar"
if [ ! -f "$runjar" ]; then
    die "Missing required file: $runjar"
fi
OPENESB_BOOT_CLASSPATH="$runjar"


# Display our environment
echo "========================================================================="
echo ""
echo "  OpenESB Standalone Runtime Bootstrap Environment"
echo ""
echo "  OPENESB_HOME: $OPENESB_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $OPENESB_BOOT_CLASSPATH"
echo ""
echo "========================================================================="
echo ""

# Execute the JVM in the foreground
"$JAVA" $JAVA_OPTS \
        -Djava.util.logging.config.file=$OPENESB_HOME/config/logger.properties \
        -jar "$OPENESB_BOOT_CLASSPATH" \
        "$@"
OPENESB_STATUS=$?