package org.rogatio.quarxs.view;

import org.rogatio.quarxs.Edge;
import org.rogatio.quarxs.Graph;
import org.rogatio.quarxs.Node;
import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.objects.BaseObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Component
@Named("graph")
@Singleton
public class GraphScriptService implements ScriptService {
    
	@Inject
	private Graph graph;
	
	public void removeEdge(String prettyId) {
		this.graph.removeEdge(prettyId);
	}
	
	public void removeNode(String prettyId) {
		this.graph.removeNode(prettyId);
	}
	
	public void createEdge(String prettyidConnection) {
		this.graph.createEdge(prettyidConnection);
	}
	
	public void setFilter(String jsonFilter) {
		this.graph.setFilter(jsonFilter);
	}

	public Edge getEdge(String prettyId) {
        return this.graph.getEdge(prettyId);
    }
	
	public Node getNode(String prettyId) {
	    return this.graph.getNode(prettyId);
	}
	
	public BaseObject getData(String prettyId) {
	    BaseObject bo = this.graph.getData(prettyId);
	    return bo;
	}
	
	public void setMaster(String nodeIdentity) {
		this.graph.setMasterNode(nodeIdentity);
	}
	
	public String calculate() {
		return this.graph.calculate();
	}

	public String calculate(int width, int height) {
		return this.graph.calculate(width, height);
	}

	public String calculate(int width, int height, String layout) {
		return this.graph.calculate(width, height, layout);
	}

}
