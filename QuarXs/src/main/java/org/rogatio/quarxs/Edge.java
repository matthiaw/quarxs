package org.rogatio.quarxs;

import org.xwiki.component.wiki.WikiComponent;

import com.xpn.xwiki.doc.XWikiDocument;

public interface Edge extends WikiComponent {

	public static final String CLASS = "Edge";

	public String getPrettyId();

	public String getGuid();

	public String getLabel();

	public XWikiDocument getDocument();
	
	public EdgeType getType();

	public Node getTarget();
	
	public Node getSource();
	
	public Node getOpposite(Node node);
	
	public boolean contains(Node node);
	
}
