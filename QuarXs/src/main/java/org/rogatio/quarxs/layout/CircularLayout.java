package org.rogatio.quarxs.layout;

import y.module.CircularLayoutModule;
import y.view.Graph2D;

public class CircularLayout implements GraphLayout {

	public String getDescription() {
		return "Circular";
	}

	public void calculate(Graph2D graph) {
		CircularLayoutModule layout = new CircularLayoutModule();
		layout.getOptionHandler().addBool("HANDLE_NODE_LABELS", true);
		layout.setMorphingEnabled(false);
		layout.start(graph);
	}

}
