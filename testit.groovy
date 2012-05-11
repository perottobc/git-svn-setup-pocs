class ScmExecutable {
    String gitRepoDir;
    String svnRepoDir;
	def ant = new AntBuilder();
	
	def git(String command ) {
		git(command, "");
	}
	
	def git(String command,String argument) {		
		println("GIT: " + command + " " + argument );		
		ant.exec(executable:"git",dir:gitRepoDir,resultproperty:"cmdExit") {
			arg(value:command)
			if( "" != argument ) { 
				arg(value:argument)
			}
		}
		if( "0" != "${ant.project.properties.cmdExit}" ) throw new RuntimeException("Error executing ant command: " + command );						
	}
	
	def svn(String command,String argument) {		
		println("SVN: " + command + " " + argument + " on " + svnRepoDir );		
		ant.exec(executable:"svn",dir:svnRepoDir,resultproperty:"cmdExit") {
			arg(value:command)
			if( "" != argument ) { 
				arg(value:argument)
			}
		}		
		if( "0" != "${ant.project.properties.cmdExit}" ) throw new RuntimeException("Error executing ant command: " + command );						
	}	
}

enum SCM {
    svn, git
}


class Dev extends ScmExecutable {
	String username
	String branch	
	String wdir = System.getenv()['WDIR'];	
	String projectDir
	String currentFile
	List addedFiles	
	SCM currentSCM
	String svn_branch;
	
	public Dev(String _username) {		
		this( _username, "git_websites" ) 		
	}

	public Dev(String _username, String git_project_name ) {		
		if( null == wdir ) throw new RuntimeException("Env variable WDIR must be set");		
		username=_username;				
		gitRepoDir=wdir+"/devs/"+username+"/" + git_project_name;
		svnRepoDir=wdir+"/devs/"+username+"/svn_websites";		
	}

	def git() {	    
		currentSCM = SCM.git
		println( "--- " + username + " Working on git repo --- " );
		addedFiles = new ArrayList(); 
		return this;		
	}
		
	def checkout(String branch) {
		this.branch = branch;
		git("checkout",branch);
		return this;
	}
	
	def chdir(String dir) {
		projectDir = dir;
		return this;
	}
	
	def add(String ... files) {
		def targetDir = gitRepoDir + "/" + projectDir;
		println( "copy files: " + files + " to [" + targetDir + "]");
		for( file in files ) {
			ant.copy(file:"./dev_src_templates/" + file,todir:targetDir );
			git("add",targetDir + "/" + file );
			addedFiles.add( file );
		}
		
		return this;
	}
	
	def commit() {
		if( SCM.git == currentSCM ) 
			git("commit","-m'"+username+" added "+ addedFiles +" on "+branch+"'");
		else
		    svn("commit", "-m'Commit from svn-user'" );
		    
		return this;
	}

	def push() {
		git("push" );
		return this;
	}
	
	def touch_and_add(String file) {
		def fullFilePath = gitRepoDir + "/" + projectDir + "/" + file;
	    println( "touch_and_add: " +  fullFilePath )
	    
		ant.exec(executable:"touch",dir:gitRepoDir + "/" + projectDir,failonerror:true) {
			arg(value:file)
		}
		git("add", fullFilePath );
		addedFiles.add( file );
		return this;
	}
		
	// -- SVN STUFF
	def svn() {	 
		currentSCM = SCM.svn   
		println( "--- " + username + " Working on svn repo ---" );		
		return this;		
	}
	
	def goto_branch( String branch ) {
		svn_branch = "branches/" + branch;
		return this;
	}
	
	def goto_trunk() {
		svn_branch = "trunk";
		return this;
	}
	
	def on_file( String _currentFile ) {
		currentFile = _currentFile;
		return this;
	}
	
	def append( String content ) {
		 
		def fileToAppend = svnRepoDir + "/" + svn_branch + "/" + projectDir + "/" + currentFile
		
		println( "Adding <" + content + "> to ["+fileToAppend+"]");  
				
		ant.echo(file:fileToAppend,append:true,message:System.getProperty("line.separator")+content);
		
		return this;
	}
	
	// jenkins
	def pull( String option ) {
		return this;
	}
	
	def svn_reset( Integer revision ) {
		return this;
	}

	def svn_rebase() {
		return this;
	}
	
	def svn_dcommit() {
		return this;
	}
	
	
}

def jenkins = new Dev( "adm", "websites.commit-repo" );
def per = new Dev("per");
def siv = new Dev("siv");
def ola = new Dev("ola");

ola.svn().goto_branch( "kaksi").chdir("model/src/main/mod").on_file( "domain.mod" ).append( "OlaFoo" );
ola.git().checkout( "kaksi").chdir("web/src/main/webapp").add( "ola-foo.html", "ola-foo-view.html" ).commit().push();
ola.svn().commit();

siv.svn().goto_branch( "yksi").chdir("model/src/main/mod").on_file( "domain.mod" ).append( "SivBar" );
siv.git().checkout( "yksi").chdir("web/src/main/webapp").add( "siv-bar.html", "siv-bar-view.html" ).commit().push();
siv.svn().commit();

per.svn().goto_trunk().chdir("model/src/main/mod").on_file( "domain.mod" ).append( "PerBaz" );
per.git().checkout( "trunk").chdir("web/src/main/webapp").add( "per-baz.html", "per-baz-view.html" ).commit().push();
per.svn().commit();


/*
jenkins.git().checkout( "trunk").pull("--rebase").svn_reset(2147483647).svn_rebase().svn_dcommit();
jenkins.git().checkout( "kaksi").pull("--rebase").svn_reset(2147483647).svn_rebase().svn_dcommit();
jenkins.git().checkout( "yksi").pull("--rebase").svn_reset(2147483647).svn_rebase().svn_dcommit();

println("VERIFY")
*/

/*
ola.git().checkout( "kaksi" ).chdir( "web" ).touch_and_add( "readme.txt" ).commit().push();
*/


/*
per.svn().goto_trunk().chdir("model/src/main/mod").on_file( "domain.mod" ).append( "PerBaz" );
per.svn().commit();
*/








