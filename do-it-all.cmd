call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on

rmdir %WDIR% /S/Q
mkdir %WDIR%

call all.1.cmd
call test