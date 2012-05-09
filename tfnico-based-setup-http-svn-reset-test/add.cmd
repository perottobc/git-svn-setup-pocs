@echo --------------------------------------------------------------------------------------------------
@echo Script creates a file and pushes it to the bare repo

set FILE=file_%1%

@echo ------------------------------------------------
@echo 1. Create file add, commit and push it to bare-repo
cd git_dev
call touch %FILE%
call git add %FILE%
call git commit -m"%FILE% is commited from add.cmd-script.. ."
call git push
cd..
