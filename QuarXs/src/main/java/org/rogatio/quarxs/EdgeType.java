package org.rogatio.quarxs;

import org.xwiki.component.wiki.WikiComponent;

import com.xpn.xwiki.doc.XWikiDocument;

public interface EdgeType extends WikiComponent{

	public static final String CLASS = "EdgeType";
	
	public String getPrettyId();
	
	public String getGuid();
	
	public String getName();
	
	public String getConnection();
	
	public String getDefaultLabel();
	
	public XWikiDocument getXwikiDocument();
	
}
