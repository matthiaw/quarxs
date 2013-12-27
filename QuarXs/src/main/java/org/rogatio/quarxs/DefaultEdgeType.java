package org.rogatio.quarxs;

import java.lang.reflect.Type;

import org.xwiki.component.wiki.WikiComponentScope;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

public class DefaultEdgeType implements EdgeType
{

    private String name;

    private String roleHint;

    private XWikiDocument xwikiDocument;

    private DocumentReference documentReference;

    private DocumentReference authorReference;

    private String defaultLabel;

    private String entity;

    public String getDefaultLabel()
    {
        return defaultLabel;
    }

    public void setDefaultLabel(String defaultLabel)
    {
        this.defaultLabel = defaultLabel;
    }

    public XWikiDocument getXwikiDocument()
    {
        return xwikiDocument;
    }

    public void setXwikiDocument(XWikiDocument xwikiDocument)
    {
        this.xwikiDocument = xwikiDocument;
    }

    public DocumentReference getDocumentReference()
    {
        return documentReference;
    }

    public DocumentReference getAuthorReference()
    {
        return authorReference;
    }

    public void setRoleHint(String roleHint)
    {
        this.roleHint = roleHint;
    }

    public void setDocumentReference(DocumentReference documentReference)
    {
        this.documentReference = documentReference;
    }

    public void setAuthorReference(DocumentReference authorReference)
    {
        this.authorReference = authorReference;
    }

    public String getRoleHint()
    {
        return roleHint;
    }

    public WikiComponentScope getScope()
    {
        return WikiComponentScope.WIKI;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public Type getRoleType()
    {
        return EdgeType.class;
    }

    private String guid;

    public void setGuid(String id)
    {
        this.guid = id;
    }

    private String prettyId;

    @Override
    public String getPrettyId()
    {
        return prettyId;
    }

    public void setPrettyId(String prettyId)
    {
        this.prettyId = prettyId;
    }

    @Override
    public String getGuid()
    {
        return guid;
    }

    public void setEntity(String entity)
    {
        this.entity = entity;
    }

    @Override
    public String getEntity()
    {
        return entity;
    }

    private int width;

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        if (width < 1) {
            width = 1;
        }
        this.width = width;
    }

    public String getColor()
    {

        if (color == null) {
            return "black";
        }

        if (color.equals("")) {
            return "black";
        }

        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    private String color;

    private String connection;

    @Override
    public String getConnection()
    {
        return connection;
    }

    public void setConnection(String connection)
    {
        this.connection = connection;
    }

}
