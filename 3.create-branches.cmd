call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on

cd %WDIR%\devs\adm\svn\websites\trunk
call mvn --batch-mode release:branch -DbranchName=yksi
call mvn --batch-mode release:branch -DbranchName=kaksi
 


