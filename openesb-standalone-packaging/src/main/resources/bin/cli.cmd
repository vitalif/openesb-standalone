@echo off
COLOR F8
prompt OE-ADMIN$Q$G
mode con:cols=180 lines=2500

cd %OPENESB_HOME%

:: the pattern is used to support OE SE Versionning
set OEADMIN_PATTERN=openesb-oeadmin*.jar
SET FILENAME=""

for /r %%x in (%OEADMIN_PATTERN%) do (SET filename=%%x)

if "%filename%"=="" Goto notfind

doskey oeadmin=java -jar %filename% $*

echo **********************************************************************************************
echo *
echo * 			Welcome to OpenESB Command Line Interface OEADMIN
echo *
echo * OEADMIN is the OpenESB command line interface. OEAdmin utility can be used to perform any 
echo * OpenESB administrative task to install and manage OE components and applications
echo * 
echo * -Type oeadmin + a commande to execute the command 
echo * -Type oeadmin to get the command list
echo * -Type exit to leave this window
echo *
echo * Mode details on www.open-esb.net 
echo **********************************************************************************************
echo.

