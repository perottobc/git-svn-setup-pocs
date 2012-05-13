SET SCRIPT_DIR=C:\git-svn-setup-pocs
call %SCRIPT_DIR%\1.init-svn-repo-on-apache.cmd
call %SCRIPT_DIR%\2.init-svn-repo-with.compay-repo-template.cmd
call %SCRIPT_DIR%\3.create-branches.cmd
call %SCRIPT_DIR%\4.1.create-gatekeeper-and-bare.cmd
call %SCRIPT_DIR%\5.1.create-git-devs.cmd
call %SCRIPT_DIR%\6.create-svn-for-devs.cmd



