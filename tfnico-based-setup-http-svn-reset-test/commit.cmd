@echo ------------------------------------------------
@echo 2. Let the commit-repo do the svn-stuff
cd git_svn_commit
call git pull --rebase
REM call git svn reset 100000
call git svn rebase
call git svn dcommit 
cd..