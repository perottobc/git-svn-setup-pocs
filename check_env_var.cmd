@echo off

if "%WDIR%" == "" GOTO WDIR_EXIT
@echo Working directory set to %WDIR%

if '%EDITOR%' == '' GOTO EDITOR_EXIT
@echo Editor set to %EDITOR%

if '%HTPASSWD_EXE%' == '' GOTO HTPASSWD_EXE_EXIT
@echo htpasswd set to %HTPASSWD_EXE%

GOTO CONTINUE_SETUP

:WDIR_EXIT
@echo Define working directory where testing will be done, e.g.:  set WDIR=e:\
Exit /b

:EDITOR_EXIT
@echo Define an editor with the env-variable EDITOR, e.g set EDITOR=notepad++
Exit /b

:HTPASSWD_EXE_EXIT
@echo Define apache2 home, e.g. "C:\Programfiler\Apache Software Foundation\Apache2.2\bin\htpasswd"
Exit /b

:CONTINUE_SETUP
SET ENV_CHECKED=OK
@echo doit