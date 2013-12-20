package org.rogatio.quarxs.layout;

import y.module.OrganicEdgeRouterModule;
import y.module.OrganicLayoutModule;
import y.view.Graph2D;

public class OrganicLayout implements GraphLayout {

	public String getDescription() {
		return "Organic";
	}

	public void calculate(Graph2D graph) {
		OrganicLayoutModule layout = new OrganicLayoutModule();
		layout.getOptionHandler().set("ACTIVATE_DETERMINISTIC_MODE", Boolean.TRUE);
		layout.setMorphingEnabled(false);
		layout.start(graph);

		OrganicEdgeRouterModule layout1 = new OrganicEdgeRouterModule();
		layout1.setMorphingEnabled(false);
		layout1.start(graph);
	}

}