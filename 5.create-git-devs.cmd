call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on

mkdir %WDIR%\devs\per
mkdir %WDIR%\devs\siv
mkdir %WDIR%\devs\ola

call git clone -o mirror %WDIR%\devs\adm\websites.git %WDIR%\devs\per\git_websites
cd %WDIR%\devs\per\git_websites
call git config user.name "per"
call git config user.email "per@doit.com"
call git checkout -t mirror/trunk
call git pull --rebase


call git clone -o mirror %WDIR%\devs\adm\websites.git %WDIR%\devs\ola\git_websites
cd %WDIR%\devs\ola\git_websites
call git config user.name "ola"
call git config user.email "ola@doit.com"
call git checkout -t mirror/kaksi
call git pull --rebase


call git clone -o mirror %WDIR%\devs\adm\websites.git %WDIR%\devs\siv\git_websites
cd %WDIR%\devs\siv\git_websites
call git config user.name "siv"
call git config user.email "siv@doit.com"
call git checkout -t mirror/yksi
call git pull --rebase





