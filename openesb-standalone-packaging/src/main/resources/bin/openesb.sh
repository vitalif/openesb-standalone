#!/bin/sh

## ***************************************************************
## The contents of this file are subject to the terms
## of the Common Development and Distribution License
## (the "License").  You may not use this file except
## in compliance with the License.
## You can obtain a copy of the license at
## www.opensource.org/licenses/CDDL-1.0.
## See the License for the specific language governing
## permissions and limitations under the License.
##
## When distributing Covered Code, include this CDDL
## HEADER in each file and include the License file at
## https://open-esb.dev.java.net/public/CDDLv1.0.html.
## If applicable add the following below this CDDL HEADER,
## with the fields enclosed by brackets "[]" replaced with
## your own identifying information: Portions Copyright
## [year] [name of copyright owner]
##
##
##  Copyright OpenESB Community 2015.
## *****************************************************************

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
    OPENESB_HOME=`cd $DIRNAME/..; pwd -P`
fi

export OPENESB_HOME

export JAVA_OPTS

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
    JAVA="$JAVA_HOME/bin/java"
    else
    JAVA="java"
    fi
fi

# Setup the classpath
runjar=`find $OPENESB_HOME -name "openesb-standalone-bootstrap*.jar"`
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
        -Dcom.atomikos.icatch.file=$OPENESB_HOME/tm/jta.properties \
        -Djava.util.logging.config.file=$OPENESB_HOME/config/logger.properties \
        -Djava.util.logging.manager=net.openesb.standalone.logging.LogManager \
        -cp "$OPENESB_BOOT_CLASSPATH:$OPENESB_HOME/lib/ext/jansi-1.11.jar" \
        -Djmx.invoke.getters=true \
        -Dopenesb.home=$OPENESB_HOME \
        net.openesb.standalone.startup.Bootstrap \
        "$@"
OPENESB_STATUS=$?
