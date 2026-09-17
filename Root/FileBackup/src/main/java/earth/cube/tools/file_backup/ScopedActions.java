package earth.cube.tools.file_backup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ScopedActions {
	
	private ScopedActions _parent;
	
	private Set<String> _actions = new HashSet<>();

	
	public ScopedActions(ScopedActions parent, String... saActions) {
		_parent = parent;
		_actions.addAll(Arrays.asList(saActions));
	}
	
	public boolean isRunningAction(String... saAction) {
		Set<String> matching = new HashSet<>(_actions);
		matching.retainAll(Arrays.asList(saAction));
		return matching.size() > 0 || (_parent != null && _parent.isRunningAction(saAction));
	}

}
