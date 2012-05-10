class GitExecutable {
    String gitRepoDir;
	def ant = new AntBuilder();
	
	def git(String command ) {
		git(command, "");
	}
	
	def git(String command,String argument) {		
		println(command + " " + argument );		
		ant.exec(executable:"git",dir:gitRepoDir,resultproperty:"cmdExit") {
			arg(value:command)
			if( "" != argument ) { 
				arg(value:argument)
			}
		}
		if( "0" != "${ant.project.properties.cmdExit}" ) throw new RuntimeException("Error executing ant command: " + command );						
	}
}

class Dev extends GitExecutable {
	String username
	String branch	
	String wdir = System.getenv()['WDIR'];	
	String projectDir;
	List addedFiles;
	
	public Dev(String _username) {		
		if( null == wdir ) throw new RuntimeException("Env variable WDIR must be set");		
		username=_username;				
		gitRepoDir=wdir+"/devs/"+username+"/git_websites";		
	}

	def git() {	    
		println( "--- " + username + " Working on git repo ---" );
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
		git("commit","-m'"+username+" added "+ addedFiles +" on "+branch+"'");
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
	
}

def per = new Dev("per");
def siv = new Dev("siv");
def ola = new Dev("ola");

/*
ola.git().checkout( "kaksi").chdir("web/src/main/webapp").add( "ola-hello.html", "ola-world.html" ).commit().push();
siv.git().checkout( "yksi").chdir("web/src/main/webapp").add( "siv-hello.html", "siv-world.html" ).commit().push();
per.git().checkout( "trunk").chdir("web/src/main/webapp").add( "per-hello.html", "per-world.html" ).commit().push();
*/

ola.git().checkout( "kaksi" ).chdir( "web" ).touch_and_add( "readme.txt" ).commit().push();






