call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on

cd %WDIR%\devs\ola
call svn co http://localhost/svn-repos/company-repo/websites svn_websites --username ola --password secure

cd %WDIR%\devs\per
call svn co http://localhost/svn-repos/company-repo/websites svn_websites --username per --password secure

cd %WDIR%\devs\siv
call svn co http://localhost/svn-repos/company-repo/websites svn_websites --username siv --password secure
