package org.rogatio.quarxs;

import java.lang.reflect.Type;

import org.xwiki.component.wiki.WikiComponentScope;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

public class DefaultNode implements Node {

	private String roleHint;
	private XWikiDocument xwikiDocument;
	private DocumentReference documentReference;
	private DocumentReference authorReference;

	public XWikiDocument getDocument() {
		return xwikiDocument;
	}

	public void setDocument(XWikiDocument xwikiDocument) {
		this.xwikiDocument = xwikiDocument;
	}

	public DocumentReference getDocumentReference() {
		return documentReference;
	}

	public DocumentReference getAuthorReference() {
		return authorReference;
	}

	public void setRoleHint(String roleHint) {
		this.roleHint = roleHint;
	}

	public void setDocumentReference(DocumentReference documentReference) {
		this.documentReference = documentReference;
	}

	public void setAuthorReference(DocumentReference authorReference) {
		this.authorReference = authorReference;
	}

	private NodeType type;

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
//	    System.out.println(label+": "+type);
		this.type = type;
	}

	public String getRoleHint() {
		return roleHint;
	}

	public WikiComponentScope getScope() {
		return WikiComponentScope.WIKI;
	}

	private String label;

	public DefaultNode() {
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Type getRoleType() {
		return Node.class;
	}

	private String guid;

	public void setGuid(String id) {
		this.guid = id;
	}

	private String prettyId;

	@Override
	public String getPrettyId() {
		return prettyId;
	}

	public void setPrettyId(String prettyId) {
		this.prettyId = prettyId;
	}

	@Override
	public String getGuid() {
		return guid;
	}

	// @Override
	// public DocumentReference getDocumentReference() {
	// return documentReference;
	// }
	//
	// @Override
	// public DocumentReference getAuthorReference() {
	// return authorReference;
	// }
	//
	// public void setRoleHint(String roleHint) {
	// this.roleHint = roleHint;
	// }
	//
	// public void setDocumentReference(DocumentReference documentReference) {
	// this.documentReference = documentReference;
	// }
	//
	// public void setAuthorReference(DocumentReference authorReference) {
	// this.authorReference = authorReference;
	// }

	// @Override
	// public String getRoleHint() {
	// return roleHint;
	// }

	// @Override
	// public WikiComponentScope getScope() {
	// return WikiComponentScope.WIKI;
	// }

}
