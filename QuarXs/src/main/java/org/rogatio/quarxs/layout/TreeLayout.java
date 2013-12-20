package org.rogatio.quarxs.layout;

import y.module.TreeLayoutModule;
import y.view.Graph2D;

public class TreeLayout implements GraphLayout {

	public String getDescription() {
		return "Tree";
	}

	public void calculate(Graph2D graph) {
		TreeLayoutModule layout = new TreeLayoutModule();
		layout.getOptionHandler().set("ALLOW_NON_TREES", Boolean.TRUE);
		layout.setMorphingEnabled(false);
		layout.start(graph);
	}

}