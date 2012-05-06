:: This starts by createing av dedicated svn-commit-repo, that is cloned to a bare repo, for developers to work with.
:: The idea is that the svn-commit-repo does regular pulls from the bare repo, and dcommits to svn

call ../check_env_var.cmd
echo on
copy commit.cmd %WDIR% 
copy dev_1_add.cmd %WDIR% 
copy dev_2_add.cmd %WDIR% 

set SVN_DEPOT_URL=http://www.objectdesign.no/svn_repos/company-repo/websites

:: 3. Clone the svn depot into a git_svn_commit_repo
cd %WDIR%
call git svn clone -s %SVN_DEPOT_URL% git_svn_commit_repo 

:: 4. Clone commit repo into a bare depo for devs to clone from
cd %WDIR%
call git clone --bare git_svn_commit_repo git_svn_bare

:: 5. Set the bare-repo as origion in the commit-repo
cd %WDIR%/git_svn_commit_repo
call git remote add origin ../git_svn_bare/

:: 6. Clone bare repo to dev_repo
cd %WDIR%
call git clone git_svn_bare git_dev1
call git clone git_svn_bare git_dev2

@echo Remove fetch from git_svn_commit_repo
%EDITOR% %WDIR%\git_svn_commit_repo\.git\config