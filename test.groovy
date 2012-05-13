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
	List gitChanges	
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
		gitChanges = new ArrayList(); 
		return this;		
	}
		
	def checkout(String branch) {
		this.branch = branch;
		git("checkout",branch);
		git("pull", "--rebase");
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
			gitChanges.add( "added: " + file );
		}
		
		return this;
	}
	
	def commit() {
		if( SCM.git == currentSCM ) 
			git("commit","-m'"+username+" changes: "+ gitChanges +" on "+branch+"'");
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
		gitChanges.add( "added: " + file );
		return this;
	}
		
	def remove(String file) {		
		def fullFilePath = gitRepoDir + "/" + projectDir + "/" + file;
	    println( "remove: " +  fullFilePath )
	    git("pull", "--rebase").git("rm", projectDir + "/" + file ).git( "commit", "-m'"+username+" removed ["+file+"]'").git("push");	    
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
		
		if( SCM.git == currentSCM ) {
			fileToAppend = gitRepoDir + "/" + projectDir + "/" + currentFile;
			git( "pull", "--rebase");
		}
				
		println( "Adding <" + content + "> to ["+fileToAppend+"]");  				
		ant.echo(file:fileToAppend,append:true,message:System.getProperty("line.separator")+content);
		
		if( SCM.git == currentSCM ) {
			git( "add", fileToAppend );
			gitChanges.add( content + " appended to " + fileToAppend  ) 
		}
				
		return this;
	}
	
	def gatekeeper_update_svn_from_bare(branch) {
		
		git( "checkout", branch )
		git( "fetch", "bare_repo" )
		git( "rebase", "remotes/bare_repo/svn/" + branch)
		git( "svn", "reset", "2147483647" )
		git( "svn", "rebase" )
		git( "svn", "dcommit" )
	
		// Alternative, doesn't work, gets merge conflict on the second passgit 
		// git( "fetch", "bare_repo" ).git( "checkout", "svn/" + branch ).git( "merge", "--no-ff","remotes/bare_repo/svn/trunk" ).git( "svn","dcommit");
	}
	
	def assert_svn_file_exists( String file ) {
		def fileWithFullPath = svnRepoDir + file;
		println( "Assert that ["+fileWithFullPath+"] exists" );
		def input = new File(fileWithFullPath )
		assert input.exists() 
		assert input.canRead()
		println( "File exists => assertion OK" );
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
		
		println( "Text found => assertion OK" );
	}
	
	def assert_svn_file_not_contains( String file, String text ) {	
		def fileWithFullPath = svnRepoDir + file;
		println( "Assert that ["+fileWithFullPath+"] does not contain ["+text+"]" );
		def input = new File(fileWithFullPath )
		
		assert input.exists() 
		assert input.canRead()
		
		Boolean matchFound = false;
		input.eachLine { line ->
			println( "    " +  line )
			if( line.contains( text ) ) matchFound = true;  
		}
		
		if( matchFound ) throw new RuntimeException("AssertError: ["+text+"] found in ["+fileWithFullPath+"]" );
		
		println( "Text not found => assertion OK" );
	}
	
	
	def assert_svn_file_not_exists( String file ) {
		def fileWithFullPath = svnRepoDir + file;
		println( "Assert that ["+fileWithFullPath+"] not exists" );
		def input = new File(fileWithFullPath )		
		assert !input.exists()
		println( "File not found => assertion OK" ); 	
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

println( "--------------- SCENARIO 7 ---------------------------------------------")
println( "Given that siv adds a file on the trunk and pushes it to the bare, which is then delete by per")
println( "When gatekeeper does the merge back to subversion")  
println( "Then the admin can't see the file")
siv.git().checkout( "svn/trunk" ).chdir( "web" ).touch_and_add( "siv-next-solution.txt" ).commit().push();
per.git().checkout( "svn/trunk" ).chdir( "web" ).remove( "siv-next-solution.txt" ).commit().push();
adm.gatekeeper_update_svn_from_bare("trunk").svn( "up" )
adm.assert_svn_file_not_exists( "/trunk/web/siv-next-solution.txt" );

println( "--------------- SCENARIO 8 ---------------------------------------------")
println( "Given that siv adds a file on the trunk and pushes it to the bare, which is then edited by per")
println( "When gatekeeper does the merge back to subversion")  
println( "Then the admin can see the file as it is editet by per")
siv.git().checkout( "svn/trunk" ).chdir( "web" ).touch_and_add( "siv-x2-solution.txt" ).commit().push();
siv.git().on_file( "siv-x2-solution.txt" ).append( "SivSuggestion" ).commit().push();
per.git().checkout( "svn/trunk" ).chdir( "web" ).on_file("siv-x2-solution.txt").append( "PerSuggestion" ).commit().push();
adm.gatekeeper_update_svn_from_bare("trunk").svn( "up" )
adm.assert_svn_file_contains( "/trunk/web/siv-x2-solution.txt", "SivSuggestion" );
adm.assert_svn_file_contains( "/trunk/web/siv-x2-solution.txt", "PerSuggestion" );

println( "--------------- SCENARIO 9 ---------------------------------------------")
println( "Given that siv extends the readme.txt on yksi ");
println( "    and ola cherry-picks siv's commit on kaksi + makes hos his own extension");
println( "    and per cherry-picks siv and ola's commit on trunk + makes hos his own extension");
println( "When gatekeeper does the merge back to subversion")  
println( "Then the admin can see the changes by siv on yksi, by ola on kaksi and on the trunk by per")

siv.git().checkout( "svn/trunk" ).git( "pull", "--rebase" )
siv.checkout( "svn/yksi" ).chdir( "web" ).on_file( "readme.txt" ).append( "Siv->Web2.0Rocks-YouKnow" ).commit().push();

ola.git().checkout( "svn/kaksi" ).git( "cherry-pick", "remotes/origin/svn/yksi~0");
ola.chdir( "web" ).on_file( "readme.txt" ).append( "Ola->JavaScriptRocksYouKnow" ).commit().push();

per.git().checkout( "svn/trunk" ).git( "cherry-pick", "remotes/origin/svn/yksi~0").git( "cherry-pick", "remotes/origin/svn/kaksi~0");
per.chdir( "web" ).on_file( "readme.txt" ).append( "Per->GitRocksYouKnow" ).commit().push();

adm.gatekeeper_update_svn_from_bare("yksi").svn( "up" )
adm.gatekeeper_update_svn_from_bare("kaksi").svn( "up" )
adm.gatekeeper_update_svn_from_bare("trunk").svn( "up" )

adm.assert_svn_file_contains( "/branches/yksi/web/readme.txt", "Siv->Web2.0Rocks-YouKnow" );
adm.assert_svn_file_not_contains( "/branches/yksi/web/readme.txt", "Ola->JavaScriptRocksYouKnow" );
adm.assert_svn_file_not_contains( "/branches/yksi/web/readme.txt", "Per->GitRocksYouKnow" );

adm.assert_svn_file_contains( "/branches/kaksi/web/readme.txt", "Siv->Web2.0Rocks-YouKnow" );
adm.assert_svn_file_contains( "/branches/kaksi/web/readme.txt", "Ola->JavaScriptRocksYouKnow" );
adm.assert_svn_file_not_contains( "/branches/kaksi/web/readme.txt", "Per->GitRocksYouKnow" );

adm.assert_svn_file_contains( "/trunk/web/readme.txt", "Siv->Web2.0Rocks-YouKnow" );
adm.assert_svn_file_contains( "/trunk/web/readme.txt", "Ola->JavaScriptRocksYouKnow" );
adm.assert_svn_file_contains( "/trunk/web/readme.txt", "Per->GitRocksYouKnow" );

