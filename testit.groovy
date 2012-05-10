// http://groovy.codehaus.org/Executing+External+Processes+From+Groovy
class GitExecutable {
    String gitRepoDir;
	def ant = new AntBuilder();
	
	def git(String command,String argument) {		
		println(command + " " + argument );		
		ant.exec(executable:"git",dir:gitRepoDir,failonerror: "true") {
			arg(value:command)
			if( null != argument ) { 
				arg(value:argument)
			}
		}						
	}
}

class GitDev extends GitExecutable {
	String username
	String branch
	String wdir = System.getenv()['WDIR'];	
	
	public GitDev(String _username, String _branch) {		
		if( null == wdir ) throw new RuntimeException("Env variable WDIR must be set");
		
		username=_username;
		branch=_branch;		
		gitRepoDir=wdir+"/devs/"+username+"/git_websites";		
	}

	def test(Integer testRunIndex) {	
		println("start")
		
		String file="hello-from-"+branch+"-"+testRunIndex+".html";
		String testfile = "web/src/main/webapp/"+ username + "-" + file;
		
		println( "Test username ["+username+"], branch ["+branch+"], testfile["+testfile+"]");

		git("checkout",branch);
		git("pull","--rebase");
		ant.copy(file:"./dev_src_templates/hello.html",tofile:gitRepoDir + "/" + testfile); 
		git("add",testfile);	
		git("commit","-m'"+username+" added "+testfile+" on "+branch+"'");
		git("push",null );
			
		println("done")
	}
}

def siv = new GitDev("siv","yksi");
def ola = new GitDev("ola","kaksi");
def per = new GitDev("per","trunk");

siv.test(409);



