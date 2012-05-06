set FILE=file_%1%.html

cd git_dev1
call touch %FILE%
call git add %FILE%
call git commit -m"%FILE% commited from myscript"
call git pull 
call git push
cd..