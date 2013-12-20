package org.rogatio.quarxs;

import org.xwiki.component.wiki.WikiComponent;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 *
= Nodes = 
{{groovy}}
   import org.rogatio.quarxs.Node;
   for (Node node : services.component.getComponentManager().getInstanceList(Node.class)) {
      println("== "+node.getLabel() +" ==")
      println("**Typ:** "+node.getType().getName() )
      if (node.getType().hasIcon()) {
         println("**Icon:** {{html}}<img src='"+node.getType().getIconUrl()+"' width='32px'>{{/html}}" )
      }
   }
{{/groovy}}
 * 
 * @author Matthias Wegner
 * 
 */
public interface Node extends WikiComponent {

	public static final String CLASS = "Node";

	public String getPrettyId();

	public String getGuid();

	public String getLabel();

	public XWikiDocument getDocument();
	
	public NodeType getType();

}
