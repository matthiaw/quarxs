package org.rogatio.quarxs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.rogatio.quarxs.util.Constants;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentBuilder;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;

@Component
@Singleton
@Named(NodeType.CLASS)
public class NodeTypeClassBuilder implements WikiComponentBuilder {

	@Inject
	private Execution execution;

	@Inject
	private QueryManager queryManager;

	@Inject
	private EntityReferenceSerializer<String> serializer;

	@Override
	public List<DocumentReference> getDocumentReferences() {
		List<DocumentReference> references = new ArrayList<DocumentReference>();

		try {
			Query query = queryManager.createQuery("SELECT doc.space, doc.name FROM Document doc, doc.object(" + Constants.SPACE + "." + NodeType.CLASS + "Class) AS obj", Query.XWQL);
			List<Object[]> results = query.execute();
			for (Object[] result : results) {
				references.add(new DocumentReference(getXWikiContext().getDatabase(), (String) result[0], (String) result[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return references;
	}

	@Override
	public List<WikiComponent> buildComponents(DocumentReference reference) throws WikiComponentException {
		List<WikiComponent> components = new ArrayList<WikiComponent>();
		XWikiContext context = getXWikiContext();
		DocumentReference classReference = new DocumentReference(getXWikiContext().getDatabase(), Constants.SPACE, NodeType.CLASS + "Class");

		try {
			XWikiDocument doc = getXWikiContext().getWiki().getDocument(reference, getXWikiContext());

			XWikiRightService rightService = getXWikiContext().getWiki().getRightService();

			if (!rightService.hasAccessLevel("admin", doc.getAuthor(), "XWiki.XWikiPreferences", getXWikiContext())) {
				throw new WikiComponentException(String.format("Failed to building " + NodeType.CLASS + "Class components from document " + " [%s], author [%s] doesn't have admin rights in the wiki",
						reference.toString(), doc.getAuthor()));
			}

			for (final BaseObject obj : doc.getXObjects(classReference)) {
				String roleHint = serializer.serialize(obj.getReference());

				DefaultNodeType n = new DefaultNodeType();

				n.setGuid(obj.getGuid());
				n.setName(obj.getStringValue("name"));
				n.setIconUrl(obj.getStringValue("iconurl"));
				n.setEntity(obj.getStringValue("entity"));
				n.setPrettyId(doc.getSpace()+"."+doc.getName()+"."+obj.getStringValue("name") + " (" + obj.getGuid() + ")");
				
				n.setAuthorReference(doc.getAuthorReference());
				n.setRoleHint(roleHint);
				n.setDocumentReference(reference);
				n.setXwikiDocument(doc);
				
				obj.set("prettyid", n.getPrettyId(), getXWikiContext());

				components.add(n);
			}
			
		} catch (Exception e) {
			throw new WikiComponentException(String.format("Failed to build " + NodeType.CLASS + "Class components from document [%s]", reference.toString()), e);
		}
		
		

		return components;
	}

	private XWikiContext getXWikiContext() {
		return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
	}

}
