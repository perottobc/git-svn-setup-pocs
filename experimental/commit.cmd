@echo ------------------------------------------------
@echo 2. Let the commit-repo do the svn-stuff
cd git_svn_commit_repo
call git pull --rebase 
call git svn rebase
call git svn dcommit 

cd..