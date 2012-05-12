@echo off
SET HTPASSWD_EXE="C:\Program Files (x86)\Apache Software Foundation\Apache2.2\bin\htpasswd"

if "%WDIR%" == "" GOTO WDIR_EXIT
@echo Working directory set to %WDIR%

if '%EDITOR%' == '' GOTO EDITOR_EXIT
@echo Editor set to %EDITOR%

@echo htpasswd set to %HTPASSWD_EXE%

GOTO CONTINUE_SETUP

:WDIR_EXIT
@echo Define working directory where testing will be done, e.g.:  set WDIR=e:\
Exit /b

:EDITOR_EXIT
@echo Define an editor with the env-variable EDITOR, e.g set EDITOR=notepad++
Exit /b

:CONTINUE_SETUP
SET ENV_CHECKED=OK
@echo htpasswd set to default: %HTPASSWD_EXE%