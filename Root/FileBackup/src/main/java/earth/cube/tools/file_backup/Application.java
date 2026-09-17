package earth.cube.tools.file_backup;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.actions.ActionParameters;
import earth.cube.tools.file_backup.actions.IAction;
import earth.cube.tools.file_backup.commons.TimeSpan;
import earth.cube.tools.file_backup.commons.TimeSpanDueException;

public class Application {

	protected Logger _log = LogManager.getLogger(getClass());
	
	protected ActionParameters _params = new ActionParameters();
	protected ActionType _actionType;


	public Options getOptions() {
		final Options options = new Options();
		options.addOption(new Option("h", "help", false, "Display help page."));
		options.addOption(new Option("v", "verbose", false, "Increase verbosity."));
		options.addOption(new Option("s", "src", true, "Source directory."));
		options.addOption(new Option("d", "dst", true, "Destination directory."));
		options.addOption(new Option("a", "action", true, "Action to perform."));
		options.addOption(new Option("t", "timespan", true, "Approximate time span to perform action. This will limit the number of files being processed."));
		options.addOption(new Option("r", "remark", true, "Remark."));
		options.addOption(new Option(null, "probe", true, "Enable probe mode (not working for all actions)."));
		return options;
	}
	
	
	public boolean parseOptions(String[] saArgs) throws ParseException, IOException, SQLException {
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, saArgs);
		
		if(cmd.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(getClass().getCanonicalName(), options);
			return false;
		}
		
		if(cmd.hasOption('s')) {
			_params.setSourceDirectory(new File(cmd.getOptionValue('s')));
		}

		if(cmd.hasOption('d')) {
			_params.setDestinationDirectory(new File(cmd.getOptionValue('d')));
		}
		
		if(cmd.hasOption('r')) {
			_params.setRemark(cmd.getOptionValue('r'));
		}
		
		_params.setProbing(cmd.hasOption("probe"));
		
		_actionType = ActionType.from(cmd.getOptionValue('a'));
		
		TimeSpan span;
		if(cmd.hasOption('t')) {
			span = new TimeSpan(cmd.getOptionValue('t'));
		}
		else {
			span = new TimeSpan();
		}
		_params.setTimeSpan(span);
		
		_params.setOut(System.out);
		
		return true;
	}
	
	protected IAction createAction() throws IOException {
		try {
			if(_actionType.isNoDestinationDirecory()) {
				_params.checkNoDestinationDirectory();
			}
			IAction action = _actionType.getClazz().newInstance();
			action.setParameters(_params);
			return action;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IOException(e);
		}
	}
	
	
	public void execute() throws IOException, SQLException, TimeSpanDueException {
		IAction action = createAction();
		if(action == null) {
			_log.debug("action: nothing to do!");
			return;
		}

		Thread thread = new Thread(() -> {
			_log.debug("execute: saving statistics in shutdown hook");
			_params.close();
		});

		try {
			Runtime.getRuntime().addShutdownHook(thread);
			action.execute();
		}
		catch(Throwable t) {
			_log.error("execute: Exception occurred -->", t);
			throw t;	
		}
		finally {
			_log.debug("execute: saving statistics in finally block");
			Runtime.getRuntime().removeShutdownHook(thread);

			_params.close();
		}
	}

	
	public static void main(String[] saArgs) throws Exception {
		Application app = new Application();
		if(!app.parseOptions(saArgs))
			System.exit(1);
		
		app.execute();
	}
	
}
