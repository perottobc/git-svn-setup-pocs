@echo --------------------------------------------------------------------------------------------------
@echo Script creates a file and goes through the loop of getting it into subversion thourgh git, arg: %1

set FILE=file_%1%

@echo ------------------------------------------------
@echo 1. Create file add, commit and push it to bare-repo
cd git_dev
call touch %FILE%
call git add %FILE%
call git commit -m"%FILE% is commited from add.cmd-script.. ."
call git push
cd..
