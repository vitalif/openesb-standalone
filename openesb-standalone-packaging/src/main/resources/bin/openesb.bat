:: ***************************************************************
:: The contents of this file are subject to the terms
:: of the Common Development and Distribution License
:: (the "License").  You may not use this file except
:: in compliance with the License.
:: You can obtain a copy of the license at
:: www.opensource.org/licenses/CDDL-1.0.
:: See the License for the specific language governing
:: permissions and limitations under the License.
::
:: When distributing Covered Code, include this CDDL
:: HEADER in each file and include the License file at
:: https://open-esb.dev.java.net/public/CDDLv1.0.html.
:: If applicable add the following below this CDDL HEADER,
:: with the fields enclosed by brackets "[]" replaced with
:: your own identifying information: Portions Copyright
:: [year] [name of copyright owner]

::  Copyright OpenESB Community 2014.
:: ******************************************************************************

:: ******************** Read me first *******************************************
:: This batch file starts OpenESB for standalone JVM. 
:: Two variables JAVA_HOME and OPENESB_HOME must be set before starting. 
:: JAVA_HOME must be defined before starting OpenESB 
:: OPENESB_HOME is the directory where openESB is installed. 
::
:: Note: we suppose that openesb.bat is in the directory %OPENESB_HOME%\bin  
:: if it is not the case, please  modify the .bat file
:: ******************** END  Read me first *******************************************

::@echo off 

echo **************************************************
echo *
echo * Welcome to OpenESB SE for Windows 
echo * OpenESB starting in process ...
echo *
echo * OpenESB Community: www.open-esb.net 
echo **************************************************
echo.

::****************** set configuration variables ***********************
:: Call setenv.bat that MUST be in the same directory than openesb.bat
:: Change to be in OEHOME directory
::**********************************************************************

:: Change to the current disk to %OPENESB_HOME% disk 
%~d0
:: Change to the %OPENESB_HOME% directory 
CD %~p0
SET OESETENV_DIR=%~dp0
CALL %OESETENV_DIR%\setenv.bat 

::****************** set configuration variables ***********************
:: Check if Java home is set
::****************** set configuration variables ***********************
IF "%JAVA_HOME%"=="" GOTO nojavahome

:: change to OpenESB home directory
cd %OPENESB_HOME% 

:: Start OpenESB in a new Dos window
START "OpenESB SE"  %JAVA_HOME%\bin\java -Djava.util.logging.config.file=%OPENESB_HOME%/config/logger.properties -Djava.util.logging.manager=net.openesb.standalone.logger.OpenESBLogManager -Djavax.net.ssl.keyStore=%OPENESB_HOME%/keystore.jks -Djavax.net.ssl.trustStore=%OPENESB_HOME%/cacerts.jks -Djavax.net.ssl.keyStorePassword=changeit -Djmx.invoke.getters=true -Dinstall.root=%OPENESB_HOME% -jar %OPENESB_HOME%\lib\openesb-standalone-bootstrap.jar

echo.
echo.
echo **************************************************
echo * 
echo * OpenESB SE Windows is opening in a new console 
echo * 
echo * Please check the logs to get OpenESB JMX Port 
echo *
echo * Documentation and forum on www.open-esb.net
echo * or contact@open-esb.net
echo * 
echo **************************************************
GOTO endbatch


:nojavahome
echo.
echo **************************************************
echo *
echo * WARNING ...
echo * JAVA_HOME must be set before starting OpenESB 
echo * Please check Java documentation to do it 
echo *
echo **************************************************
GOTO endbatch

:endbatch 