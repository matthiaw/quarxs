package org.rogatio.quarxs.layout;

import y.module.OrthogonalLayoutModule;
import y.view.Graph2D;

public class OrthogonalLayout implements GraphLayout {

	public String getDescription() {
		return "Orthogonal";
	}

	public void calculate(Graph2D graph) {
		OrthogonalLayoutModule layout = new OrthogonalLayoutModule();
		layout.setMorphingEnabled(false);
		layout.start(graph);
	}

}