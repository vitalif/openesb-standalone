@echo off
COLOR F8
prompt OE-ADMIN$Q$G
mode con:cols=180 lines=2500

doskey oeadmin=java -jar %OPENESB_HOME%\lib\openesb-oeadmin.jar $*

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
echo * Mode detail on www.open-esb.net 
echo **********************************************************************************************
echo.
