package jaca;

import jason.asSyntax.Literal;
import jason.environment.Environment;

import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jaca.CAgentArch;
import jaca.CartagoEnvironment;
import cartago.CartagoException;
import cartago.IAgentSession;
import cartago.tools.inspector.Inspector;

/**
 * Jason Environment Class enabling access to CArtAgO environments.
 * 
 * @author aricci
 *
 */
public class CartagoEnvironment extends Environment {

	private static CartagoEnvironment instance;
	private String wspName;
	
	private boolean local;		// an existing local node (false -> to be created, because not existing)
    private boolean standalone; // default case, install a node
    private boolean debug;
	
	static Logger logger = Logger.getLogger(CartagoEnvironment.class.getName());

	public void init(String[] args) {
		logger.setLevel(Level.WARNING);
		wspName = cartago.CartagoEnvironmentStandalone.WSP_DEFAULT_NAME;
		local = false;
		standalone = true;
		debug = false;

		/*
		 * Arguments include also "options", whose prefix is "-"
		 * Options can be specified in any position. 
		 */
		if (args.length > 0){
			if (!args[0].startsWith("-")){
				
				/*
				 * standalone: creating a local workspace, running local agents, not accepting agents connecting from remote.
				 * 
				 * infrastructure: creating a worspace that can be joined on the network
				 * 
				 * remote: connecting to an existing (remote) workspace
				 * 
				 */
				if (args[0].equals("standalone")){
					standalone = true;
				} else {
					throw new IllegalArgumentException("Unknown argument: "+args[0]+" (should be standalone)");
				}
			}
		}

		/* current supported options:
		 * -debug
		 * -name
		 */
		if (hasOption(args, "-debug")) {
			debug = true;
		}
		
		
		/*
		 * Initialise the environment by creating a local workspace, 
		 * only in the standalone or infrastructure case.
		 * 
		 * In the remote case, the agent will join a remote workspace.
		 * 
		 */
		
		if (standalone){
			try {
				cartago.CartagoEnvironmentStandalone env = cartago.CartagoEnvironmentStandalone.getInstance();
		
				if (debug){
					 Inspector insp = new Inspector();
					 insp.start();
					 env.initWsp(wspName, Optional.of(insp.getLogger()));
				} else {				 
					 env.initWsp(wspName, Optional.empty());
				}
				logger.info("CArtAgO Environment - standalone workspace " + wspName + "created.");
			} catch (Exception ex){
				logger.severe("CArtAgO Environment - standalone setup failed.");
				ex.printStackTrace();
			}
		}  
				
		instance = this;
		
	}
	
	/**
	 * Get the instance of this environment.
	 * 
	 * @return
	 */
	public static CartagoEnvironment getInstance(){
		return instance;
	}
	
	/**
	 * Join an agent to the default workspace of the environment
	 * 
	 * @param agName agent name
	 * @param arch agent arch. class
	 * @return the interface to act inside the workspace
	 * @throws Exception
	 */
	public IAgentSession startSession(String agName, CAgentArch arch) throws Exception {
		IAgentSession context = cartago.CartagoEnvironmentStandalone.getInstance().startSession(new cartago.AgentIdCredential(agName),arch);
		// logger.info("NEW AGENT JOINED: "+agName);
		return context;
	}


	@Override
	public void stop() {
		super.stop();
			try {
				cartago.CartagoEnvironmentStandalone.getInstance().shutdown();
			} catch (CartagoException e) {
				e.printStackTrace();
			}
	}

	private static boolean hasOption(String[] args, String arg){
		for (int i = 0; i<args.length; i++){
			if (args[i].equals(arg) && i<args.length-1){
				return true;
			} 
		}
		return false;
	}

	private static String getParam(String[] args, String arg){
		for (int i = 0; i<args.length; i++){
			if (args[i].equals(arg) && i<args.length-1){
				return args[i+1];
			} 
		}
		return null;
	}

}

