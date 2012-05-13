@echo off

if "%WDIR%" == "" GOTO WDIR_EXIT
@echo Info: Working directory set to %WDIR%

if '%EDITOR%' == '' GOTO EDITOR_EXIT
@echo Info: Editor set to %EDITOR%

if '%HTPASSWD_EXE%' == '' GOTO HTPASSWD_EXE_EXIT
@echo Info: htpasswd set to %HTPASSWD_EXE%

GOTO CONTINUE_SETUP

:WDIR_EXIT
@echo Error: Define working directory where testing will be done, e.g.:  set WDIR=e:\
Exit /b

:EDITOR_EXIT
@echo Error: Set env-var EDITOR, e.g set EDITOR="C:\Program Files (x86)\Notepad++\notepad++.exe"
Exit /b

:HTPASSWD_EXE_EXIT
@echo Error: Set env-var HTPASSWD_EXE, e.g. set HTPASSWD_EXE="C:\Program Files (x86)\Apache Software Foundation\Apache2.2\bin\htpasswd"
Exit /b

:CONTINUE_SETUP
SET ENV_CHECKED=OK
@echo doit