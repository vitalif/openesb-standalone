#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  OpenESB Administration Command Line                                     ##
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
runjar=`find $OPENESB_HOME -name "openesb-oeadmin*.jar"`
if [ ! -f "$runjar" ]; then
    die "Missing required file: $runjar"
fi
OPENESB_OEADMIN_CLASSPATH="$runjar"

"$JAVA" $JAVA_OPTS \
        -jar "$OPENESB_OEADMIN_CLASSPATH" \
        "$@"

OPENESB_STATUS=$?
