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
	
	def svn(String command,String ... arguments) {		
		println(username + "/svn: " + command + " " + arguments + " on " + svnRepoDir );		
		ant.exec(executable:"svn",dir:svnRepoDir,resultproperty:"cmdExit") {
			arg(value:command)

			for( argument in arguments ) { 
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
	
	def gatekeeper_update_svn_from_bare(branch) {	
		git( "checkout", branch )
		git( "fetch", "bare_repo" )
		git( "rebase", "remotes/bare_repo/svn/" + branch)
		git( "svn", "reset", "2147483647" )
		git( "svn", "rebase" )
		git( "svn", "dcommit" )
	}
	

	def dcommit_to_svn_2() {
		git( "fetch", "bare_repo" ).git( "checkout", "svn/trunk" ).git( "merge", "--no-ff","remotes/bare_repo/svn/trunk" ).git( "svn","dcommit");
		git( "fetch", "bare_repo" ).git( "checkout", "svn/kaksi" ).git( "merge", "--no-ff","remotes/bare_repo/svn/kaksi" ).git( "svn","dcommit");
		git( "fetch", "bare_repo" ).git( "checkout", "svn/yksi" ).git( "merge", "--no-ff","remotes/bare_repo/svn/yksi" ).git( "svn","dcommit");	
	}
	
	def assert_svn_file_exists( String file ) {
		def fileWithFullPath = svnRepoDir + file;
		println( "Assert that ["+fileWithFullPath+"] exists" );
		def input = new File(fileWithFullPath )
		assert input.exists() 
		assert input.canRead()
		println( "File exists" );
	}
	
	def assert_svn_file_contains( String file, String text ) {
	
		def fileWithFullPath = svnRepoDir + file;
		println( "Assert that ["+fileWithFullPath+"] contains ["+text+"]" );
		def input = new File(fileWithFullPath )
		
		assert input.exists() 
		assert input.canRead()
		
		Boolean matchFound = false;
		input.eachLine { line ->
			println( "    " +  line )
			if( line.contains( text ) ) matchFound = true;  
		}
		
		if( !matchFound ) throw new RuntimeException("AssertError: ["+text+"] not found in ["+fileWithFullPath+"]" );
		
		println( "Text found" );
	}
		
}

def adm = new Dev( "adm", "websites" );
def per = new Dev("per");
def siv = new Dev("siv");
def ola = new Dev("ola");

println( "--------------- SCENARIO 1 ---------------------------------------------")
println( "Given that per checkout trunk and push a new file to the bare-repo")
println( "When the gatekeeper runs and updates the svn repo")
println( "Then the admin can see the new file on the trunk of the subversion repo")
println( "--")
per.git().checkout( "svn/trunk" ).chdir( "web" ).touch_and_add( "readme-from-per.txt" ).commit().push();
adm.gatekeeper_update_svn_from_bare("trunk").svn( "up" )
adm.assert_svn_file_exists( "/trunk/web/readme-from-per.txt" );
println( "------------------------------------------------------------------------")
println()
 
println( "--------------- SCENARIO 2 ---------------------------------------------")
println( "Given that ola checkout kaksi and push a new file to the bare-repo")
println( "When the gatekeeper runs and updates the svn repo")
println( "Then the admin can see the new file on the kaksi-branch of the subversion repo")
println( "--")
ola.git().checkout( "svn/kaksi" ).chdir( "web" ).touch_and_add( "readme-from-ola.txt" ).commit().push();
adm.gatekeeper_update_svn_from_bare("kaksi").svn( "up" )
adm.assert_svn_file_exists( "/branches/kaksi/web/readme-from-ola.txt" );
println( "------------------------------------------------------------------------")
println()

println( "--------------- SCENARIO 3 ---------------------------------------------")
println( "Given that siv checkout yksi and push a new file to the bare-repo")
println( "When the gatekeeper runs and updates the svn repo")
println( "Then the admin can see the new file on the yksi-branch of the subversion repo")
println( "--")
siv.git().checkout( "svn/yksi" ).chdir( "web" ).touch_and_add( "readme-from-siv.txt" ).commit().push();
adm.gatekeeper_update_svn_from_bare("yksi").svn( "up" )
adm.assert_svn_file_exists( "/branches/yksi/web/readme-from-siv.txt" );
println( "------------------------------------------------------------------------")
println()

println( "--------------- SCENARIO 4 ---------------------------------------------")
println( "Given that per modifies a file on the trunk in his svn repo and commits it")
println( "When he pushes two new files on the trunk via the git svn gatekeeper")  
println( "Then the admin can see the updated svn file and the two new files on the trunk")
println( "--")
per.svn().goto_trunk().chdir("model/src/main/mod").on_file( "domain.mod" ).append( "PerBaz" ).commit();
per.git().checkout( "svn/trunk").chdir("web/src/main/webapp").add( "per-baz.html", "per-baz-view.html" ).commit().push();
adm.gatekeeper_update_svn_from_bare("trunk").svn( "up" )
adm.assert_svn_file_exists( "/trunk/web/src/main/webapp/per-baz.html" );
adm.assert_svn_file_exists( "/trunk/web/src/main/webapp/per-baz-view.html" );
adm.assert_svn_file_contains( "/trunk/model/src/main/mod/domain.mod", "PerBaz" );
println( "------------------------------------------------------------------------")
println()

println( "--------------- SCENARIO 5 ---------------------------------------------")
println( "Given that ola modifies a file on the branch/kaksi in his svn repo and commits it")
println( "When he pushes two new files on the branch/kaksi via the git svn gatekeeper")  
println( "Then the admin can see the updated svn file and the two new files on the branch/kaksi")
println( "--")
ola.svn().goto_branch( "kaksi").chdir("model/src/main/mod").on_file( "domain.mod" ).append( "OlaFoo" ).commit();
ola.git().checkout( "svn/kaksi").chdir("web/src/main/webapp").add( "ola-foo.html", "ola-foo-view.html" ).commit().push();
adm.gatekeeper_update_svn_from_bare("kaksi").svn( "up" )
adm.assert_svn_file_exists( "/branches/kaksi/web/src/main/webapp/ola-foo.html" );
adm.assert_svn_file_exists( "/branches/kaksi/web/src/main/webapp/ola-foo-view.html" );
adm.assert_svn_file_contains( "/branches/kaksi/model/src/main/mod/domain.mod", "OlaFoo" );
println( "------------------------------------------------------------------------")
println()

println( "--------------- SCENARIO 6 ---------------------------------------------")
println( "Given that siv modifies a file on the branch/yksi in here svn repo and commits it")
println( "When she pushes two new files on the branch/yksi via the git svn gatekeeper")  
println( "Then the admin can see the updated svn file and the two new files on the branch/kaksi")
println( "--")
siv.svn().goto_branch( "yksi").chdir("model/src/main/mod").on_file( "domain.mod" ).append( "SivBar" ).commit();
siv.git().checkout( "svn/yksi").chdir("web/src/main/webapp").add( "siv-bar.html", "siv-bar-view.html" ).commit().push();
adm.gatekeeper_update_svn_from_bare("yksi").svn( "up" )
adm.assert_svn_file_exists( "/branches/yksi/web/src/main/webapp/siv-bar.html" );
adm.assert_svn_file_exists( "/branches/yksi/web/src/main/webapp/siv-bar-view.html" );
adm.assert_svn_file_contains( "/branches/yksi/model/src/main/mod/domain.mod", "SivBar" );
println( "------------------------------------------------------------------------")
println()