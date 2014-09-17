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

start "OpenESB OE-ADMIN" cli.cmd 
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