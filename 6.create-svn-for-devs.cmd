call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on

mkdir %WDIR%\devs\adm
cd %WDIR%\devs\adm
call svn co http://localhost/svn-repos/company-repo/websites svn_websites --username adm --password secure

mkdir %WDIR%\devs\per
cd %WDIR%\devs\per
call svn co http://localhost/svn-repos/company-repo/websites svn_websites --username per --password secure

mkdir %WDIR%\devs\siv
cd %WDIR%\devs\siv
call svn co http://localhost/svn-repos/company-repo/websites svn_websites --username siv --password secure

mkdir %WDIR%\devs\ola
cd %WDIR%\devs\ola
call svn co http://localhost/svn-repos/company-repo/websites svn_websites --username ola --password secure


