package org.rogatio.quarxs;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.xwiki.component.wiki.WikiComponentScope;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

public class DefaultNodeType implements NodeType {

	private String name;


	private String iconUrl;

	private String entity;

	private String roleHint;

	private XWikiDocument xwikiDocument;
	private DocumentReference documentReference;
	private DocumentReference authorReference;

	public XWikiDocument getXwikiDocument() {
		return xwikiDocument;
	}

	public void setXwikiDocument(XWikiDocument xwikiDocument) {
		this.xwikiDocument = xwikiDocument;
	}

	public boolean hasIcon() {
		if (this.iconUrl != null) {
			if (!this.iconUrl.trim().equals("")) {

				try {
					final URL url = new URL(iconUrl);
					HttpURLConnection huc = (HttpURLConnection) url.openConnection();
					int responseCode = huc.getResponseCode();
					if (responseCode == 200) {
						return true;
					}
				} catch (UnknownHostException uhe) {
					return false;
				} catch (FileNotFoundException fnfe) {
					return false;
				} catch (Exception e) {
					return false;
				}

				return true;
			}
		}
		return false;
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

	public String getRoleHint() {
		return roleHint;
	}

	public WikiComponentScope getScope() {
		return WikiComponentScope.WIKI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Type getRoleType() {
		return NodeType.class;
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

	public String getIconUrl() {
	    
//	    if (iconUrl!=null) {
//	        if (!iconUrl.startsWith("http")) {
//	            try {
//	                String f = new File("../../../resources/icons/"+iconUrl).toURL().toString();
//	                System.out.println(f);
//                    return f;
//                } catch (MalformedURLException e) {
//                    return iconUrl; 
//                }
//	        }
//	    }
	    
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	@Override
	public String getEntity() {
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
        
        if (color==null) {
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
	
}
