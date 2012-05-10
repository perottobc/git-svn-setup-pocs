:: This setup uses a setup described by http://blog.tfnico.com/2010/11/git-svn-mirror-for-multiple-branches.html.
:: The change is this setup is that the devs should push back to the bare-repo and let a dedicated commit-repo do the job with dcomming to svn.

call check_env_var.cmd
if "%ENV_CHECKED%" == "OK" GOTO DOIT
@echo Check environment variables that must be set
Exit /b

:DOIT
@echo on
mkdir %WDIR%\devs
mkdir %WDIR%\devs\adm

@echo ----------------------------------
@echo 1. Clone Subversion repo
cd %WDIR%\devs\adm
call git svn clone -s http://localhost/svn-repos/company-repo/websites --username adm

cd %WDIR%\devs\adm/websites
call git svn fetch

@echo ----------------------------------
@echo  2. Set up a bare repo

cd %WDIR%\devs\adm
call git init --bare websites.git

@echo ----------------------------------
@echo 3. Configure fetching repo to push changes

cd %WDIR%\devs\adm/websites
call git remote add origin ../websites.git/


@echo Change block to the following
@echo [remote "origin"]
@echo    url = ../websites.git/
@echo    fetch = +refs/remotes/*:refs/remotes/origin/*
@echo    push = refs/remotes/*:refs/heads/*

%EDITOR% %WDIR%\devs\adm/websites/.git/config
@echo waiting with git command until file is saved 
pause

call git push origin

@echo ----------------------------------
@echo 4. Set up an SVN remote in a dedicated commit-repo

:: This solution should let the developers push to the bare repo only, where the commit-repo pulls from the bare and commits to subversion
cd %WDIR%\devs\adm
call git clone -o mirror ./websites.git/ websites.commit-repo

cd websites.commit-repo 
call git checkout -t mirror/trunk
call git pull --rebase

call git svn init --prefix=mirror/ -s http://localhost/svn-repos/company-repo/websites














