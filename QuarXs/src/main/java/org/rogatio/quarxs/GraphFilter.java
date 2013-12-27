package org.rogatio.quarxs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Data-Handling for Filtering a graph with JSON
 * 
 * @version $Id$
 */
public class GraphFilter {

	private String space;
	private String doc;
	private String nodelabel;
	private String edgelabel;
	private String nodetype;
	private String edgetype;
	private String mode;
	
	public static void main(String... args) {
		GraphFilter filter = new GraphFilter();
		
		filter.space = "Space";
		filter.doc = "Document";
		filter.nodelabel = "Node";
	
		/**
		{
		  "space": "Space",
		  "doc": "Document",
		  "nodelabel": "Node"
		}
		**/
		System.out.println(filter.toJson());
		
		Gson gson = new GsonBuilder().create();
		filter = gson.fromJson(filter.toJson(), GraphFilter.class);
		
		System.out.println(filter.toJson());
		
	}
	
	public String getSpace() {
		return space;
	}

	public String getDoc() {
		return doc;
	}

	public String getNodelabel() {
		return nodelabel;
	}

	public String getEdgelabel() {
		return edgelabel;
	}

	public String getNodetype() {
		return nodetype;
	}

	public String getEdgetype() {
		return edgetype;
	}
	
	public String getMode() {
		return mode;
	}

	public String toJson() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonOutput = gson.toJson(this);
		return jsonOutput;
	}
	
}
