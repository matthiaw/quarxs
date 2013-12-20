package org.rogatio.quarxs;

import org.xwiki.component.wiki.WikiComponent;

import com.xpn.xwiki.doc.XWikiDocument;

public interface NodeType extends WikiComponent{

	public static final String CLASS = "NodeType";
	
	public String getPrettyId();
	
	public String getGuid();
	
	public String getName();
	
	public String getEntity();
	
	public String getIconUrl();
	
	public XWikiDocument getXwikiDocument();
	
	public boolean hasIcon();
	
}
