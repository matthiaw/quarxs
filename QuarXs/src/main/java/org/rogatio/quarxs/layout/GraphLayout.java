package org.rogatio.quarxs.layout;

import y.view.Graph2D;

public interface GraphLayout {

	/**
	 * @return Returns the Description of the Layout
	 */
	public String getDescription();
	
	/**
	 * Calculates the Layout
	 */
	public void calculate(Graph2D graph);
	
}
