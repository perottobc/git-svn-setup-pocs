@echo --------------------------------------------------------------------------------------------------
@echo Script creates a file, adds, commits and pushes it to the bare 

set FILE=file_%1%

@echo ------------------------------------------------
@echo 1. Create file add, commit and push it to bare-repo
cd sources/websites
call touch %FILE%
call git add %FILE%
call git commit -m"%FILE% is ready"
call git push
cd..
cd..

