@echo ------------------------------------------------
@echo 2. Let the commit-repo do the svn-stuff
cd repos/websites.commit-repo
call git pull --rebase
call git svn rebase
call git svn dcommit --username svnuser
cd..
cd..