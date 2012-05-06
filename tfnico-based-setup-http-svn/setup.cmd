:: This setup uses pre-created svn repo at objectdesign and applies the steps from http://blog.tfnico.com/2010/11/git-svn-mirror-for-multiple-branches.html.
:: The change is this setup is that the devs should push back to the bare-repo and let a dedicated commit-repo do the job with dcomming to svn.

call ../check_env_var.cmd
echo on

mkdir %WDIR%\repos

@echo ----------------------------------
@echo 1. Clone Subversion repo
cd %WDIR%/repos
call git svn clone -s http://www.objectdesign.no/svn_repos/company-repo/websites

cd %WDIR%/repos/websites
call git svn fetch

@echo ----------------------------------
@echo  2. Set up a bare repo
pause
cd %WDIR%/repos
call git init --bare websites.git

@echo ----------------------------------
@echo 3. Configure fetching repo to push changes
pause
cd %WDIR%/repos/websites
call git remote add origin ../websites.git/


@echo Change block to the following
@echo [remote "origin"]
@echo    url = ../websites.git/
@echo    fetch = +refs/remotes/*:refs/remotes/origin/*
@echo    push = refs/remotes/*:refs/heads/*

%EDITOR% %WDIR%/repos/websites/.git/config
@echo waiting with git command until file is saved 
pause

call git push origin

@echo ----------------------------------
@echo 4. Clone the bare repo for dev, without svn-remote
pause
mkdir %WDIR%\sources
cd %WDIR%/sources
call git clone -o mirror ../repos/websites.git/

cd websites 
call git checkout -t mirror/trunk
call git pull --rebase


@echo ----------------------------------
@echo 5* Set up an SVN remote in a dedicated commit-repo
pause
:: This solution should let the developers push to the bare repo only, where the commit-repo pulls from the bare and commits to subversion
cd %WDIR%/repos
call git clone -o mirror ./websites.git/ websites.commit-repo

cd websites.commit-repo 
call git checkout -t mirror/trunk
call git pull --rebase

call git svn init --prefix=mirror/ -s http://www.objectdesign.no/svn_repos/company-repo/websites

cd %WDIR%














