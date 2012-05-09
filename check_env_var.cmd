@echo off
if "%WDIR_DRIVE%" == "" GOTO WDIR_DRIVE_EXIT
@echo Working dir drive set to %WDIR_DRIVE%

if '%WDIR_NAME%' == '' GOTO WDIR_NAME_EXIT
@echo Wording dir name set to %WDIR_NAME%

SET WDIR=%WDIR_DRIVE%\%WDIR_NAME%
@echo Workding dir is %WDIR%

SET SVN_FILE_PARENT_PATH=file:///%WDIR_DRIVE%/%WDIR_NAME%
@echo Parent path for svn-repo is %SVN_FILE_PARENT_PATH%

if '%EDITOR%' == '' GOTO EDITOR_EXIT
@echo Editor set to %EDITOR%

GOTO CONTINUE_SETUP

:WDIR_DRIVE_EXIT
@echo Define a drive for the working directory, e.g.:  set WDIR_DRIVE=e:
Exit /b

:WDIR_NAME_EXIT
@echo Define a name for the working directory, e.g. set WDIR_NAME=tmp
Exit /b

:EDITOR_EXIT
@echo Define an editor with the env-variable EDITOR, e.g set EDITOR=notepad++
Exit /b

:CONTINUE_SETUP