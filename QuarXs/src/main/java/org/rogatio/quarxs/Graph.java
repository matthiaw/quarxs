package org.rogatio.quarxs;

import org.xwiki.component.annotation.Role;

@Role /* annotation used for declaring the service our component provides */
public interface Graph
{
	public String calculate();
    
	public void removeNode(String prettyId);
	
	public void removeEdge(String prettyId);
	
	public void remove(Edge edge);
	
    public void remove(Node node);
    
    public void createEdge(String prettyidConnection);
    
    public void setMasterNode(String prettyidMaster);
    
    public String calculate(int width, int height);
    
    public void setFilter(String filter);
    
    public String calculate(int width, int height, String layout);
}

