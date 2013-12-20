package org.rogatio.quarxs;

import java.lang.reflect.Type;

import org.xwiki.component.wiki.WikiComponentScope;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

public class DefaultEdge implements Edge {

	private String roleHint;
	private XWikiDocument xwikiDocument;
	private DocumentReference documentReference;
	private DocumentReference authorReference;
	private Node source;
	private Node target;

	public Node getSource() {
		return source;
	}

	public void setSource(Node source) {
		this.source = source;
	}

	public Node getOpposite(Node node) {
		if (this.getSource().getPrettyId().equals(node.getPrettyId())) {
			return this.getTarget();
		}
		if (this.getTarget().getPrettyId().equals(node.getPrettyId())) {
			return this.getSource();
		}
		return null;
	}

	public boolean contains(Node node) {
		if (node == null) {
			return false;
		}
		if (this.getSource() != null) {
			if (this.getSource().getPrettyId().equals(node.getPrettyId())) {
				return true;
			}
		}
		if (this.getTarget() != null) {
			if (this.getTarget().getPrettyId().equals(node.getPrettyId())) {
				return true;
			}
		}
		return false;
	}

	public Node getTarget() {
		return target;
	}

	public void setTarget(Node target) {
		this.target = target;
	}

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

	private EdgeType type;

	public EdgeType getType() {
		return type;
	}

	public void setType(EdgeType type) {
		this.type = type;
	}

	public String getRoleHint() {
		return roleHint;
	}

	public WikiComponentScope getScope() {
		return WikiComponentScope.WIKI;
	}

	private String label;

	public DefaultEdge() {
	}

	public String getLabel() {

		if (label == null) {
			if (this.getType() != null) {
				return this.getType().getDefaultLabel();
			}
		}

		if (label.equals("")) {
			if (this.getType() != null) {
				return this.getType().getDefaultLabel();
			}
		}

		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Type getRoleType() {
		return Edge.class;
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

}
