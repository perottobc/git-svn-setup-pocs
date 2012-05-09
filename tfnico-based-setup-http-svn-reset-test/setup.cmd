:: This setup creates a svn file-repo and applies the steps from http://blog.tfnico.com/2010/11/git-svn-mirror-for-multiple-branches.html.
:: The change is this setup is that the devs should push back to the bare-repo and let a dedicated commit-repo do the job with dcomming to svn.

call ../check_env_var.cmd
echo on

copy add.cmd %WDIR% 
copy commit.cmd %WDIR%

@echo 1. Clone the svn-serve into a fetch repo
cd %WDIR%
call git svn clone -s http://localhost/svn/svn_repo2/site git_svn_fetch

@echo 4. Create the bare repo
cd %WDIR%
call git init --bare git_svn_bare

@echo 5. Configure the fetch repo to push to the bare repo
cd %WDIR%
cd git_svn_fetch
cd
call git remote add origin ../git_svn_bare/

@echo Change block to the following, last two lines
@echo [remote "origin"]
@echo    url = ../git_svn_bare/
@echo    fetch = +refs/remotes/*:refs/remotes/origin/*
@echo    push = refs/remotes/*:refs/heads/*

%EDITOR% .git/config
@echo Waiting for edit of config to finish
pause

call git push origin

@echo 6. Create git dev repo without svn integration
cd %WDIR%
call git clone -o mirror git_svn_bare git_dev
cd git_dev
call git checkout -t mirror/trunk
call git pull --rebase


@echo 7. Set up a dedicated commit-repo
cd %WDIR%
call git clone -o mirror ./git_svn_bare/ git_svn_commit

cd git_svn_commit
call git checkout -t mirror/trunk
call git pull --rebase

call git svn init --prefix=mirror/ -s http://localhost/svn/svn_repo2/site/

cd %WDIR%





