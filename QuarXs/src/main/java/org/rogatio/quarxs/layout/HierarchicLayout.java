package org.rogatio.quarxs.layout;

import y.module.HierarchicLayoutModule;
import y.view.Graph2D;

public class HierarchicLayout implements GraphLayout {

	public String getDescription() {
		return "Hierarchic";
	}

	public void calculate(Graph2D graph) {
		int DISTANCE = 50;
		HierarchicLayoutModule layout = new HierarchicLayoutModule();		
//		layout.getOptionHandler().set("ORIENTATION", "LEFT_TO_RIGHT");
		layout.getOptionHandler().set("ORIENTATION", "TOP_TO_BOTTOM");
		layout.getOptionHandler().set("MINIMAL_EDGE_DISTANCE", DISTANCE);
		layout.getOptionHandler().set("MINIMAL_NODE_DISTANCE", DISTANCE);
		layout.setMorphingEnabled(false);
		layout.start(graph);
//		System.out.println("Layout Hierarchic");
	}

}
