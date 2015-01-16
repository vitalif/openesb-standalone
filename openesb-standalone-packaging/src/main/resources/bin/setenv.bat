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

::  Copyright OpenESB Community 2015.
:: ******************************************************************************

:: ******************** Read me first *******************************************
:: This batch file starts OpenESB for standalone JVM. 
:: Two variables JAVA_HOME and OPENESB_HOME must be set before starting. 
:: JAVA_HOME must be defined before starting OpenESB 
:: OPENESB_HOME is the directory where openESB is installed. 
::
:: Note: we suppose that openesb.bat is in the directory %OPENESB_HOME%\bin  
:: if it is not the case, please modify setenv.bat file
:: ******************** END  Read me first *******************************************

@echo off

::****************** set Java Home ***********************
:: Set your java home.To set JAVA_HOME: To set this property on Windows
:: Right click My Computer and select Properties.
:: On the Advanced tab, select Environment Variables, 
:: and then edit JAVA_HOME to point to where the JDK software is located, 
:: for example, C:\Program Files\Java\jdk1.7.0_19 
::********************************************************

:: If you need a local java home uncomment the following line 
:: and set your local JAVA_HOME. Don't do it if you want to use 
:: your JAVA_HOME setting defined in your machine
:: SET JAVA_HOME=C:\Program Files\Java\jdk1.7.0_19


:: Check if Java home is empty
IF NOT "%JAVA_HOME%"=="" GOTO javaHome

echo.
echo **************************************************
echo *
echo * WARNING ...
echo * JAVA_HOME must be set before starting OpenESB 
echo * Please check Java documentation to do it 
echo *
echo **************************************************
GOTO set OEHome

:javaHome

echo.
echo JAVA_HOME: %JAVA_HOME%
echo.
echo JAVA_OPTS: %JAVA_OPTS%



:OEHome
::****************** set OpenESB Home ***********************
:: Set your OpenESB home 
:: Seeting OpenESB_Home is optionnal. If this property is empty
:: OpenESB on Windows is supposed to start where you installed OpenESB 
:: For a safer starting, we advice the users to setup OPENESB_HOME.
:: For the windows edition, OpenESB java home is the directory where 
:: you installed OpenESB and not the bin directory
::********************************************************

:: To set a value for %OPENESB_HOME% here
:: SET OPENESB_HOME=f:\mySoftware\openesb-se

::****************** set default OpenESB Home ***********************
:: if OPENESB_HOME is empty we set it with the value of the parent directory
:: of this setenv.bat file.  
::We suppose setanv.bat is in the directory %OPENESB_HOME\BIN
::****************** set default OpenESB Home *********************** 

IF NOT "%OPENESB_HOME%"=="" GOTO displayOEHome
SET OESETENV_DIR=%~dp0
for %%? in ("%~dp0..") do set OPENESB_HOME=%%~f?


:displayOEHome

echo.
echo OPENESB_HOME: %OPENESB_HOME% 
echo. 
echo =========================================================================
echo. 
