package org.rogatio.quarxs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.rogatio.quarxs.util.Constants;
import org.rogatio.quarxs.util.ObjectQuery;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentBuilder;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;

@Component
@Singleton
@Named(Node.CLASS)
public class NodeClassBuilder implements WikiComponentBuilder {

	@Inject
	@Named("context")
	private Provider<ComponentManager> componentManagerProvider;
	
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
			Query query = queryManager.createQuery("SELECT doc.space, doc.name FROM Document doc, doc.object(" + Constants.SPACE + "." + Node.CLASS + "Class) AS obj", Query.XWQL);
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
		DocumentReference documentReference = new DocumentReference(getXWikiContext().getDatabase(), Constants.SPACE, Node.CLASS + "Class");

		try {
			XWikiDocument doc = getXWikiContext().getWiki().getDocument(reference, getXWikiContext());

			XWikiRightService rightService = getXWikiContext().getWiki().getRightService();

//			if (!rightService.hasAccessLevel("admin", doc.getAuthor(), "XWiki.XWikiPreferences", getXWikiContext())) {
//				throw new WikiComponentException(String.format("Failed to building " + Node.CLASS + "Class components from document " + " [%s], author [%s] doesn't have admin rights in the wiki",
//						reference.toString(), doc.getAuthor()));
//			}

			List<BaseObject> objects = doc.getXObjects(documentReference);

			for (final BaseObject obj : objects) {
				String roleHint = serializer.serialize(obj.getReference());

				DefaultNode n = new DefaultNode();

				n.setGuid(obj.getGuid());
				n.setLabel(obj.getStringValue("label"));
				n.setPrettyId(doc.getDocumentReference().getLastSpaceReference().getName() + "." + doc.getDocumentReference().getName() + "." + obj.getStringValue("label") + " (" + obj.getGuid()
						+ ")");

				n.setAuthorReference(doc.getAuthorReference());
				n.setRoleHint(roleHint);
				n.setDocumentReference(reference);
				n.setDocument(doc);

				String idNodeType = obj.getStringValue("nodetype").trim();
//				if (idNodeType.equals("")) {
//					n.setType(ObjectQuery.getNodeTypeDefault(queryManager));
//				} else {
					n.setType(ObjectQuery.getNodeType(idNodeType, queryManager));
//				}
				
				obj.set("prettyid", n.getPrettyId(), getXWikiContext());

				components.add(n);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WikiComponentException(String.format("Failed to build " + Node.CLASS + "Class components from document [%s]", reference.toString()), e);
		}

		return components;
	}

	private XWikiContext getXWikiContext() {
		return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
	}

}
