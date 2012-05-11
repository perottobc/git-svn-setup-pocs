SET SCRIPT_DIR=e:\git-svn
call %SCRIPT_DIR%\1.init-svn-repo-on-apache.cmd
pause
call %SCRIPT_DIR%\2.init-svn-repo-with.compay-repo-template.cmd
pause
call %SCRIPT_DIR%\3.create-branches.cmd
pause
call %SCRIPT_DIR%\4.create-bare-and-commit-repo.cmd
pause
call %SCRIPT_DIR%\5.create-git-devs.cmd
pause
call %SCRIPT_DIR%\6.create-svn-for-devs.cmd



