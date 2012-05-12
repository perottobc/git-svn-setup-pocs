enum SCM {
    svn, git
}

class ScmExecutable {
	String username
	
    String gitRepoDir;
    String svnRepoDir;
	def ant = new AntBuilder();
	
	def git(String command,String ... arguments) {		
		println(username +"/git: " + command + " " + arguments );		
		ant.exec(executable:"git",dir:gitRepoDir,resultproperty:"cmdExit") {
			arg(value:command)
		
			for( argument in arguments ) { 
				arg(value:argument)
			}
		}
		if( "0" != "${ant.project.properties.cmdExit}" ) throw new RuntimeException("Error executing ant command: " + command );
		return this;						
	}
	
	def svn(String command,String argument) {		
		println(username + "/svn: " + command + " " + argument + " on " + svnRepoDir );		
		ant.exec(executable:"svn",dir:svnRepoDir,resultproperty:"cmdExit") {
			arg(value:command)
			if( "" != argument ) { 
				arg(value:argument)
			}
			arg(value:"--username")
			arg(value:username)
			arg(value:"--password")
			arg(value:"secure")
		}		
		if( "0" != "${ant.project.properties.cmdExit}" ) throw new RuntimeException("Error executing ant command: " + command );						
	}	
}

class Dev extends ScmExecutable {
	
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
		git("pull");
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
		    svn("commit", "-m'Commit from svn-user ["+username+"]'" );
		    
		return this;
	}

	def push() {
		git("pull", "--rebase");
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
		git("pull",option);
		return this;
	}
	
	def svn_reset( Integer revision ) {
		git("svn","reset",revision.toString());
		return this;
	}

	def svn_rebase() {
		git("svn","rebase");
		return this;
	}
	
	def svn_dcommit() {
		git("svn","dcommit");
		return this;
	}
	
	def dcommit_to_svn_2() {
		git( "fetch", "bare_repo" ).git( "checkout", "svn/trunk" ).git( "merge", "--no-ff","remotes/bare_repo/svn/trunk" ).git( "svn","dcommit");
		git( "fetch", "bare_repo" ).git( "checkout", "svn/kaksi" ).git( "merge", "--no-ff","remotes/bare_repo/svn/kaksi" ).git( "svn","dcommit");
		git( "fetch", "bare_repo" ).git( "checkout", "svn/yksi" ).git( "merge", "--no-ff","remotes/bare_repo/svn/yksi" ).git( "svn","dcommit");	
	} 
	
}

def jenkins = new Dev( "adm", "websites" );
def per = new Dev("per");
def siv = new Dev("siv");
def ola = new Dev("ola");

ola.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme1.txt" ).commit().push();
siv.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme2.txt" ).commit().push();
per.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme3.txt" ).commit().push();

jenkins.dcommit_to_svn_2();

ola.svn().goto_branch( "kaksi").chdir("model/src/main/mod").on_file( "domain.mod" ).append( "OlaFoo" );
ola.git().checkout( "svn/kaksi").chdir("web/src/main/webapp").add( "ola-foo.html", "ola-foo-view.html" ).commit().push();
ola.svn().commit();

ola.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme4.txt" ).commit().push();
siv.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme5.txt" ).commit().push();
per.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme6.txt" ).commit().push();

jenkins.dcommit_to_svn_2();

per.svn().goto_trunk().chdir("model/src/main/mod").on_file( "domain.mod" ).append( "PerBaz" );
per.git().checkout( "svn/trunk").chdir("web/src/main/webapp").add( "per-baz.html", "per-baz-view.html" ).commit().push();
per.svn().commit();

siv.svn().goto_branch( "yksi").chdir("model/src/main/mod").on_file( "domain.mod" ).append( "SivBar" );
siv.git().checkout( "svn/yksi").chdir("web/src/main/webapp").add( "siv-bar.html", "siv-bar-view.html" ).commit().push();
siv.svn().commit();

ola.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme7.txt" ).commit().push();
siv.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme8.txt" ).commit().push();
per.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme9.txt" ).commit().push();

jenkins.dcommit_to_svn_2();

ola.svn().goto_branch( "kaksi").chdir("model/src/main/mod").on_file( "domain.mod" ).append( "OlaFoo" );
ola.git().checkout( "svn/kaksi").chdir("web/src/main/webapp").add( "ola-foo2.html", "ola-foo-view2.html" ).commit().push();
ola.svn().commit();

per.svn().goto_trunk().chdir("model/src/main/mod").on_file( "domain.mod" ).append( "PerBaz" );
per.git().checkout( "svn/trunk").chdir("web/src/main/webapp").add( "per-baz2.html", "per-baz-view2.html" ).commit().push();
per.svn().commit();

jenkins.dcommit_to_svn_2();

siv.svn().goto_branch( "yksi").chdir("model/src/main/mod").on_file( "domain.mod" ).append( "SivBar" );
siv.git().checkout( "svn/yksi").chdir("web/src/main/webapp").add( "siv-bar2.html", "siv-bar-view2.html" ).commit().push();
siv.svn().commit();

ola.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme10.txt" ).commit().push();
siv.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme11.txt" ).commit().push();
per.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme12.txt" ).commit().push();

jenkins.dcommit_to_svn_2();

println("Please verify!")