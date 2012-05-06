:: This setup creates a svn file-repo and applies the steps from http://blog.tfnico.com/2010/11/git-svn-mirror-for-multiple-branches.html.
:: The change is this setup is that the devs should push back to the bare-repo and let a dedicated commit-repo do the job with dcomming to svn.

call ../check_env_var.cmd
echo on

copy add.cmd %WDIR% 
copy commit.cmd %WDIR%

@echo 1. Create svn depo on server
cd %WDIR%
svnadmin create --pre-1.5-compatible %WDIR%/svn_depot

@echo 2. Check out the svn server-depo and add standard structure
cd %WDIR%
svn co %SVN_FILE_PARENT_PATH%/svn_depot svn_local_depot
cd svn_local_depot
mkdir trunk
mkdir tags
mkdir branches
svn add *
svn commit -m"Init standard structure"

@echo 3. Clone the svn-serve into a fetch repo
cd %WDIR%
call git svn clone -s %SVN_FILE_PARENT_PATH%/svn_depot git_svn_fetch

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

call git svn init --prefix=mirror/ -s %SVN_FILE_PARENT_PATH%/svn_depot

cd %WDIR%





