call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on

set APACHE_SVN_REPOS=%WDIR%\apache-httpd-svn-repos
mkdir %APACHE_SVN_REPOS%

%HTPASSWD_EXE% -c -b %APACHE_SVN_REPOS%\svn-auth-file adm secure
%HTPASSWD_EXE% -b %APACHE_SVN_REPOS%\svn-auth-file ola secure
%HTPASSWD_EXE% -b %APACHE_SVN_REPOS%\svn-auth-file per secure
%HTPASSWD_EXE% -b %APACHE_SVN_REPOS%\svn-auth-file siv secure

svnadmin create %APACHE_SVN_REPOS%\company-repo
