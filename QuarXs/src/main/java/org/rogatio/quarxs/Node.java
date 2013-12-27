package org.rogatio.quarxs;

import org.xwiki.component.wiki.WikiComponent;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Node Interface Class
 * 
 * @version $Id$
 */
public interface Node extends WikiComponent
{

    /**
     * Descriptor for NodeClass
     */
    public static final String CLASS = "Node";

    /**
     * Unified Id which hold the Space, DocumentName, NodeLabel and GUID of XObject
     * @return
     */
    public String getPrettyId();

    /**
     * GUID of the XObject
     * @return
     */
    public String getGuid();

    /**
     * Label of the Node. If on creation no Label is given the Page-Name is set as Label
     * @return
     */
    public String getLabel();

    /**
     * Document which holds the node
     * @return
     */
    public XWikiDocument getDocument();

    /**
     * NodeType which defines the Node-Style and Node-Data-Model
     * @return
     */
    public NodeType getType();

}
