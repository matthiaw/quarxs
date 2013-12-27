package org.rogatio.quarxs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.rogatio.quarxs.util.Constants;
import org.rogatio.quarxs.util.ObjectQuery;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentBuilder;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;

@Component
@Singleton
@Named(Edge.CLASS)
public class EdgeClassBuilder implements WikiComponentBuilder {

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
			Query query = queryManager.createQuery("SELECT doc.space, doc.name FROM Document doc, doc.object(" + Constants.SPACE + "." + Edge.CLASS + "Class) AS obj", Query.XWQL);
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
		DocumentReference documentReference = new DocumentReference(getXWikiContext().getDatabase(), Constants.SPACE, Edge.CLASS + "Class");

		try {
			XWikiDocument doc = getXWikiContext().getWiki().getDocument(reference, getXWikiContext());

			XWikiRightService rightService = getXWikiContext().getWiki().getRightService();

			if (!rightService.hasAccessLevel("admin", doc.getAuthor(), "XWiki.XWikiPreferences", getXWikiContext())) {
				throw new WikiComponentException(String.format("Failed to building " + Edge.CLASS + "Class components from document " + " [%s], author [%s] doesn't have admin rights in the wiki",
						reference.toString(), doc.getAuthor()));
			}

			if (doc.getXObjects(documentReference) != null) {
				for (final BaseObject obj : doc.getXObjects(documentReference)) {
					if (obj != null) {
						try {
							String roleHint = serializer.serialize(obj.getReference());
							DefaultEdge edge = new DefaultEdge();

							edge.setGuid(obj.getGuid());
							edge.setLabel(obj.getStringValue("label"));
							edge.setPrettyId(doc.getSpace() + "." + doc.getName() + "." + obj.getStringValue("label") + " (" + obj.getGuid() + ")");
							logger.info("Build Edge with prettyId: " + edge.getPrettyId());

							edge.setAuthorReference(doc.getAuthorReference());
							edge.setRoleHint(roleHint);
							edge.setDocumentReference(reference);
							edge.setDocument(doc);

							EdgeType et = ObjectQuery.getEdgeType(obj.getStringValue("edgetype"), queryManager);
//							if (et==null) {
//							    System.out.println("EdgeType is NULL.");   
//							}
							edge.setType(et);

							obj.set("prettyid", edge.getPrettyId(), getXWikiContext());

							edge.setTarget(ObjectQuery.getNode(obj.getStringValue("nodetarget"), queryManager, getXWikiContext()));
							edge.setSource(ObjectQuery.getNode(obj.getStringValue("nodesource"), queryManager, getXWikiContext()));

							components.add(edge);
						} catch (Exception ex) {
							ex.printStackTrace();
							throw new WikiComponentException(String.format("Failed to build " + Edge.CLASS + "Class components from document [%s]", reference.toString()), ex);
						}
					}
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			// throw new WikiComponentException(String.format("Failed to build "
			// + Edge.CLASS + "Class components from document [%s]",
			// reference.toString()), e);
		}

		return components;
	}

	@Inject
	private Logger logger;

	private XWikiContext getXWikiContext() {
		return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
	}

}
