call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on

mkdir %WDIR%\devs
mkdir %WDIR%\devs\per
svn co http://localhost/svn-repos/company-repo %WDIR%\devs\per\svn --username per --password secure
xcopy company_repo_template %WDIR%\devs\per\svn  /S /E

cd %WDIR%\devs\per\svn
svn add websites
svn commit -m"Initial directory structure created for websites"
