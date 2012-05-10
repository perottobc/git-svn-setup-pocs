call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on

mkdir %WDIR%\devs
mkdir %WDIR%\devs\adm
svn co http://localhost/svn-repos/company-repo %WDIR%\devs\adm\svn --username adm --password secure
xcopy company_repo_template %WDIR%\devs\adm\svn  /S /E

cd %WDIR%\devs\adm\svn
svn add websites
svn commit -m"Initial directory structure created for websites"
