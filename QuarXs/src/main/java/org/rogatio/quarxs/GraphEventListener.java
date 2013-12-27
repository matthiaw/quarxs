package org.rogatio.quarxs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.rogatio.quarxs.util.Constants;
import org.rogatio.quarxs.util.ObjectQuery;
import org.rogatio.quarxs.util.PrettyIdConverter;
import org.slf4j.Logger;
import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
@Named("GraphEventListener")
public class GraphEventListener implements EventListener
{

    @Inject
    private Logger logger;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private QueryManager queryManager;

    @Override
    public List<Event> getEvents()
    {
        List<Event> events = new ArrayList<Event>();
        events.add(new DocumentUpdatedEvent());
        events.add(new DocumentCreatedEvent());
        return events;
    }

    @Inject
    private Execution execution;

    @Override
    public String getName()
    {
        return "graph";
    }

    private BaseObject getNodeObject(XWikiDocument doc)
    {
        EntityReference entRef = doc.resolveClassReference(Constants.SPACE + "." + Node.CLASS + "Class");
        if (doc.getXObjects(entRef) != null) {
            return doc.getXObjects(entRef).get(0);

        }
        return null;
    }

    private boolean documentContainsNodeObject(XWikiDocument doc)
    {
        EntityReference entRef = doc.resolveClassReference(Constants.SPACE + "." + Node.CLASS + "Class");

        List<BaseObject> list = doc.getXObjects(entRef);

        if (list != null) {
            if (list.size() != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {

        XWikiDocument doc = (XWikiDocument) source;
        XWikiContext context = (XWikiContext) data;

        // Is Document created?
        if (event instanceof DocumentCreatedEvent) {
            // Check if a Graph Node exists. When not then create one
            System.out.println(doc.getName() + " contains Node: " + documentContainsNodeObject(doc));
            if (!documentContainsNodeObject(doc)) {
                createNode(doc, context);
            }
        }

        // Document potentially renamed?
        if (event instanceof AbstractDocumentEvent) {
            BaseObject node = getNodeObject(doc);
            if (node != null) {
                String prettyId = node.getStringValue("prettyid");

                if (prettyId != null) {
                    if (prettyId.trim().equals("")) {
                        System.out.println("ERROR: PrettyId of Node is not set. Force Change to Document '"
                            + doc.getName() + "' to create one.");
                    }
                }

                String label = node.getStringValue("label");
                String guid = node.getGuid();
                String nodeDocName = PrettyIdConverter.getDocumentName(prettyId);

                if (nodeDocName == null) {
                    System.out.println("ERROR: DocumentName from PrettyId could not be found in Node '" + prettyId
                        + "'");
                }

                String nodeDocSpace = PrettyIdConverter.getSpace(prettyId);
                if (nodeDocSpace == null) {
                    System.out.println("ERROR: Space from PrettyId could not be found in Node '" + prettyId + "'");
                }

                String docName = doc.getDocumentReference().getName();
                String docSpace = doc.getDocumentReference().getLastSpaceReference().getName();
                // Rename PrettyId if Document-Name is not correct
                if (!(docSpace + "." + docName).equals(nodeDocSpace + "." + nodeDocName)) {
                    node.set("prettyid", doc.getDocumentReference().getLastSpaceReference().getName() + "."
                        + doc.getDocumentReference().getName() + "." + label + " (" + guid + ")", context);

                    try {
                        context.getWiki().saveDocument(doc, context);
                    } catch (XWikiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // is document created or changed?
        if (((event instanceof DocumentUpdatedEvent) || (event instanceof DocumentCreatedEvent))
            && documentContainsNodeObject(doc)) {

            EdgeType edgeWikiType = getEdgeTypeWikiRelation();

            EntityReference refEdge = doc.resolveClassReference("QuarXs.EdgeClass");

            List<ResourceReference> links = new ArrayList<ResourceReference>();
            getLinks(links, doc.getXDOM().getRoot());

            /**
             * Delete the Wiki-Relation-Edge-Object when no Link is used
             */
            if (doc.getXObjects(refEdge) != null) {
                boolean onefound = false;
                for (BaseObject baseObject : doc.getXObjects(refEdge)) {
                    if (baseObject != null) {
                        String type = baseObject.getStringValue("edgetype");
                        String nodeSource = baseObject.getStringValue("nodesource");
                        String nodeTarget = baseObject.getStringValue("nodetarget");

                        boolean found = false;
                        for (ResourceReference resourceReference : links) {
                            if (nodeSource.contains(doc.getDocumentReference().getName())
                                && nodeTarget.contains(resourceReference.getReference())
                                && type.equals(edgeWikiType.getPrettyId())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            if (type.equals(edgeWikiType.getPrettyId())) {
                                doc.removeXObject(baseObject);
                                onefound = true;
                            }
                        }
                    }
                }
                if (onefound) {
                    try {
                        context.getWiki().saveDocument(doc, context);
                    } catch (XWikiException e) {
                        e.printStackTrace();
                    }
                }
            }

            /**
             * Create Wiki-Relation-Edge when Link is set
             */
            for (ResourceReference resourceReference : links) {
                boolean createEdge = false;
                if (doc.getXObjects(refEdge) != null) {
                    boolean found = false;
                    for (BaseObject baseObject : doc.getXObjects(refEdge)) {
                        if (baseObject != null) {
                            String type = baseObject.getStringValue("edgetype");
                            String nodeSource = baseObject.getStringValue("nodesource");
                            String nodeTarget = baseObject.getStringValue("nodetarget");
                            if (nodeSource.contains(doc.getDocumentReference().getName())
                                && nodeTarget.contains(resourceReference.getReference())
                                && type.equals(edgeWikiType.getPrettyId())) {
                                found = true;
                            }
                        }
                    }
                    if (!found) {
                        createEdge = true;
                    }
                } else {
                    createEdge = true;
                }

                if (createEdge) {
                    createEdge(doc, context, resourceReference);
                }
            }
        }

        // Is Document changed?
        if ((event instanceof DocumentUpdatedEvent)) {
            EntityReference entRef = doc.resolveClassReference(Constants.SPACE + "." + Node.CLASS + "Class");

            /**
             * Create Data-Object for Node if it not exists
             */
            // Do the Page contain a Node?
            if (doc.getXObjects(entRef) != null) {
                // When yes, then get Nodes
                for (BaseObject obj : doc.getXObjects(entRef)) {
                    if (obj != null) {
                        String nodeLabel = obj.getStringValue("label");
                        // Node contains NodeType
                        if (!obj.getStringValue("nodetype").equals("")) {
                            // Get NodeTypeObject from PrettyId
                            NodeType nodeType = getNodeType(obj.getStringValue("nodetype"));
                            // If NodeType contains DataEntity
                            String entity = nodeType.getEntity();
                            if (!entity.equals("")) {
                                // Create Object-Instance of DataEntity if it
                                // not already exists
                                logger.info("DataObject '" + entity + "' created for Node '"
                                    + obj.getStringValue("label") + "'.");
                                System.out.println("DataObject '" + entity + "' created for Node '"
                                    + obj.getStringValue("label") + "'.");
                                BaseObject dataObject =
                                    this.createDataObjectNode(doc, context, entity, obj.getStringValue("prettyid"));
                                // If dataObject exists, check if prettyid is correct
                                if (dataObject != null) {
                                    String id = dataObject.getStringValue("node");
                                    String label = PrettyIdConverter.getName(id);
                                    if (!label.equals(nodeLabel)) {
                                        dataObject.set("node", PrettyIdConverter.replaceName(id, nodeLabel),
                                            context);
                                        try {
                                            context.getWiki().saveDocument(doc, context);
                                        } catch (XWikiException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            entRef = doc.resolveClassReference(Constants.SPACE + "." + Edge.CLASS + "Class");
            /**
             * Create Data-Object for Edge if it not exists
             */
            // Do the Page contain a Edge?
            if (doc.getXObjects(entRef) != null) {
//                System.out.println("Size: "+doc.getXObjects(entRef).size());
                // When yes, then get Edges
                for (BaseObject obj : doc.getXObjects(entRef)) {
                    if (obj != null) {
                        String edgeLabel = obj.getStringValue("label");
//                        System.out.println("label: "+edgeLabel);
                        // Edge contains EdgeType
                        if (!obj.getStringValue("edgetype").equals("")) {
                            // Get EdgeTypeObject from PrettyId
                           EdgeType edgeType = getEdgeType(obj.getStringValue("edgetype"));
//                           System.out.println("EdgeType: "+edgeType.getName());
                            // If EdgeType contains DataEntity
                            String entity = edgeType.getEntity();
                            if (!entity.equals("")) {
                                // Create Object-Instance of DataEntity if it
                                // not already exists
                                logger.info("DataObject '" + entity + "' created for Edge '"
                                    + obj.getStringValue("prettyid") + "'.");
                                System.out.println("DataObject '" + entity + "' created for Edge '"
                                    + obj.getStringValue("prettyid") + "'.");
                                BaseObject dataObject =
                                    this.createDataObjectEdge(doc, context, entity, obj.getStringValue("prettyid"));
                                // If dataObject exists, check if prettyid is correct
                                
//                                System.out.println(obj.getStringValue("prettyid")+" is Data-created to "+dataObject);
                                
                                if (dataObject != null) {
                                    String id = dataObject.getStringValue("edge");
                                    
//                                    String label = PrettyIdConverter.getName(id);
                                    if (!obj.getStringValue("prettyid").equals(id)) {
                                        dataObject.set("edge", PrettyIdConverter.replaceName(id, edgeLabel),
                                            context);
                                        try {
                                            context.getWiki().saveDocument(doc, context);
                                        } catch (XWikiException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /**
             * Update edge-links of node-label is changed
             */
            // Do the Page contain a Node?
            if (doc.getXObjects(entRef) != null) {
                // When yes, then get Nodes
                for (BaseObject obj : doc.getXObjects(entRef)) {
                    if (obj != null) {
                        String prettyidNode = obj.getStringValue("prettyid");
                        String nodeLabel = obj.getStringValue("label");
                        String nodeGuid = PrettyIdConverter.getGuid(prettyidNode);
                        // Iterate over all existing edges
                        for (Edge edge : getEdges(context)) {
                            if (edge != null) {
                                String spaceNameEdge = PrettyIdConverter.getSpace(edge.getPrettyId()).trim();
                                String docNameEdge = PrettyIdConverter.getDocumentName(edge.getPrettyId()).trim();
                                // If Source-Node is attached
                                if (edge.getSource() != null) {
                                    String guid = PrettyIdConverter.getGuid(edge.getSource().getPrettyId()).trim();
                                    String label = PrettyIdConverter.getName(edge.getSource().getPrettyId()).trim();
                                    // check if Source-Node is node in document
                                    if (guid.equals(nodeGuid)) {
                                        // if node-label is not the same with
                                        // the edge
                                        if (!label.equals(nodeLabel)) {
                                            EntityReference refEdge = doc.resolveClassReference("QuarXs.EdgeClass");
                                            if (edge.getDocument().getXObjects(refEdge) != null) {
                                                for (BaseObject baseObject : edge.getDocument().getXObjects(refEdge)) {
                                                    // load relevant
                                                    // edge-baseobject
                                                    if (edge.getPrettyId()
                                                        .equals(baseObject.getStringValue("prettyid"))) {
                                                        // replace prettyid in
                                                        // edge
                                                        baseObject.set("nodesource",
                                                            PrettyIdConverter.replaceName(prettyidNode, nodeLabel),
                                                            context);
                                                        try {
                                                            // save edge in
                                                            // edge-holding
                                                            // document
                                                            XWikiContext wikicontext =
                                                                (XWikiContext) this.execution.getContext().getProperty(
                                                                    "xwikicontext");
                                                            DocumentReference docRef =
                                                                new DocumentReference(context.getDatabase(),
                                                                    spaceNameEdge, docNameEdge);
                                                            XWikiDocument docEdge =
                                                                wikicontext.getWiki().getDocument(docRef, wikicontext);
                                                            wikicontext.getWiki().saveDocument(docEdge, wikicontext);
                                                        } catch (XWikiException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (edge.getTarget() != null) {
                                    String guid = PrettyIdConverter.getGuid(edge.getTarget().getPrettyId()).trim();
                                    String label = PrettyIdConverter.getName(edge.getTarget().getPrettyId()).trim();
                                    if (guid.equals(nodeGuid)) {
                                        if (!label.equals(nodeLabel)) {
                                            EntityReference refEdge = doc.resolveClassReference("QuarXs.EdgeClass");
                                            if (edge.getDocument().getXObjects(refEdge) != null) {
                                                for (BaseObject baseObject : edge.getDocument().getXObjects(refEdge)) {
                                                    if (edge.getPrettyId()
                                                        .equals(baseObject.getStringValue("prettyid"))) {
                                                        baseObject.set("nodetarget",
                                                            PrettyIdConverter.replaceName(prettyidNode, nodeLabel),
                                                            context);
                                                        try {
                                                            XWikiContext wikicontext =
                                                                (XWikiContext) this.execution.getContext().getProperty(
                                                                    "xwikicontext");
                                                            DocumentReference docRef =
                                                                new DocumentReference(context.getDatabase(),
                                                                    spaceNameEdge, docNameEdge);
                                                            XWikiDocument docEdge =
                                                                wikicontext.getWiki().getDocument(docRef, wikicontext);
                                                            wikicontext.getWiki().saveDocument(docEdge, wikicontext);
                                                        } catch (XWikiException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void getLinks(List<ResourceReference> list, Block block)
    {

        if (block instanceof LinkBlock) {
            LinkBlock linkBlock = (LinkBlock) block;
            ResourceReference ref = linkBlock.getReference();

            if (ref.getType().equals(ResourceType.DOCUMENT)) {
                list.add(ref);
            }
        }

        List<Block> children = block.getChildren();
        for (Block cBlock : children) {
            getLinks(list, cBlock);
        }

    }

    private EdgeType getEdgeTypeWikiRelation()
    {
        try {
            if (componentManagerProvider.get().getInstanceList(EdgeType.class).size() == 0) {
                System.out.println("ERROR: No EdgeTypes found!");
            }

            for (Object edgeTypeObj : componentManagerProvider.get().getInstanceList(EdgeType.class)) {
                EdgeType edgeType = (EdgeType) edgeTypeObj;
                if (edgeType.getName().equals("DocumentRelation")) {
                    return edgeType;
                }
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        System.out.println("ERROR: DocumentRelation-EdgeType is missing!");
        return null;
    }

    private NodeType getNodeTypeDefault()
    {
        try {
            for (Object nodeTypeObj : componentManagerProvider.get().getInstanceList(NodeType.class)) {
                NodeType nodeType = (NodeType) nodeTypeObj;
                if (nodeType.getName().equals("Default")) {
                    return nodeType;
                }
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        System.out.println("ERROR: Default-NodeType is missing!");
        return null;
    }

    private void createEdge(XWikiDocument doc, XWikiContext context, ResourceReference resourceReference)
    {
        String space = doc.getDocumentReference().getLastSpaceReference().getName();
        String name = doc.getDocumentReference().getName();
        EdgeType edgeWikiType = getEdgeTypeWikiRelation();

        try {
            boolean targetContainsNode = false;
            EntityReference entRef = doc.resolveClassReference(Constants.SPACE + "." + Edge.CLASS + "Class");
            int objectIndex = doc.createXObject(entRef, context);
            BaseObject obj = doc.getXObjects(entRef).get(objectIndex);
            obj.set("prettyid", space + "." + name + "." + " (" + obj.getGuid() + ")", context);

            List<Node> nodes = getNodes();
            for (Node node : nodes) {
                if (node.getPrettyId().contains(space + "." + name)) {
                    obj.set("nodesource", node.getPrettyId(), context);
                }
                if (node.getPrettyId().contains(space + "." + resourceReference.getReference())) {
                    obj.set("nodetarget", node.getPrettyId(), context);
                    targetContainsNode = true;
                }
            }

            obj.set("edgetype", edgeWikiType.getPrettyId(), context);

            if (targetContainsNode) {
                context.getWiki().saveDocument(doc, context);
            }
        } catch (XWikiException e) {
            e.printStackTrace();
        }
    }

    private void createNode(XWikiDocument doc, XWikiContext context)
    {
        EntityReference entRef = doc.resolveClassReference(Constants.SPACE + "." + Node.CLASS + "Class");
        try {
            int objectIndex = doc.createXObject(entRef, context);
            BaseObject obj = doc.getXObjects(entRef).get(objectIndex);

            obj.set("label", doc.getDocumentReference().getName(), context);
            obj.set(
                "prettyid",
                doc.getDocumentReference().getLastSpaceReference().getName() + "."
                    + doc.getDocumentReference().getName() + "." + doc.getDocumentReference().getName() + " ("
                    + obj.getGuid() + ")", context);

            NodeType nt = getNodeTypeDefault();
            if (nt != null) {
                obj.set("nodetype", nt.getPrettyId(), context);

                context.getWiki().saveDocument(doc, context);

                logger.info("Node in Document '" + doc.getName() + "' created.");
                System.out.println("Node in Document '" + doc.getName() + "' created.");
            } else {
                System.out.println("ERROR: Default NodeType not exists! Could not create Node!");
            }
        } catch (XWikiException e) {
            e.printStackTrace();
        }
    }

    private BaseObject createDataObjectEdge(XWikiDocument doc, XWikiContext context, String dataClass, String edgePrettyId)
    {
        EntityReference entRefData = doc.resolveClassReference(dataClass);

        if (doc == null) {
            return null;
        }

        if (doc.getXObjects(entRefData) != null) {
            for (BaseObject baseObject : doc.getXObjects(entRefData)) {
                if (baseObject == null) {
                    return null;
                }

                if (baseObject.getStringValue("edge") == null) {
                    return null;
                }
                if (baseObject.getStringValue("edge").equals("")) {
                    return null;
                }
                if (PrettyIdConverter.getGuid(baseObject.getStringValue("edge")).equals(
                    PrettyIdConverter.getGuid(edgePrettyId))) {
                    return baseObject;
                }

            }
        }

        try {
            int objectIndex = doc.createXObject(entRefData, context);
            BaseObject objData = doc.getXObjects(entRefData).get(objectIndex);
            objData.set("edge", edgePrettyId, context);
            context.getWiki().saveDocument(doc, context);
        } catch (XWikiException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private BaseObject createDataObjectNode(XWikiDocument doc, XWikiContext context, String dataClass, String nodePrettyId)
    {
        EntityReference entRefData = doc.resolveClassReference(dataClass);

        if (doc == null) {
            return null;
        }

        if (doc.getXObjects(entRefData) != null) {
            for (BaseObject baseObject : doc.getXObjects(entRefData)) {
                if (baseObject == null) {
                    return null;
                }

                if (baseObject.getStringValue("node") == null) {
                    return null;
                }
                if (baseObject.getStringValue("node").equals("")) {
                    return null;
                }
                if (PrettyIdConverter.getGuid(baseObject.getStringValue("node")).equals(
                    PrettyIdConverter.getGuid(nodePrettyId))) {
                    return baseObject;
                }

            }
        }

        try {
            int objectIndex = doc.createXObject(entRefData, context);
            BaseObject objData = doc.getXObjects(entRefData).get(objectIndex);
            objData.set("node", nodePrettyId, context);
            context.getWiki().saveDocument(doc, context);
        } catch (XWikiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Edge> getEdges(XWikiContext context)
    {
        List<Edge> list = new ArrayList<Edge>();
        try {
            for (Object edgeTypeObj : componentManagerProvider.get().getInstanceList(Edge.class)) {
                Edge edge = (Edge) edgeTypeObj;
                Edge edgeFromBo = ObjectQuery.getEdge(edge.getPrettyId(), queryManager, context);
                list.add(edgeFromBo);
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }

        return list;
    }

    private Node getNode(XWikiDocument doc)
    {

        for (Node node : getNodes()) {
            if (node.getDocument().getDocumentReference().getName().equals(doc.getDocumentReference().getName())) {
                return node;
            }
        }

        return null;
    }

    private List<Node> getNodes()
    {
        List<Node> list = new ArrayList<Node>();
        try {
            for (Object nodeTypeObj : componentManagerProvider.get().getInstanceList(Node.class)) {
                Node node = (Node) nodeTypeObj;
                list.add(node);
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Edge getEdge(String prettyId)
    {
        try {
            for (Object eo : componentManagerProvider.get().getInstanceList(Edge.class)) {
                Edge edge = (Edge) eo;
                if (edge.getPrettyId().equals(prettyId)) {
                    return edge;
                }
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        return null;
    }

    private NodeType getNodeType(String prettyId)
    {
        try {
            for (Object nodeTypeObj : componentManagerProvider.get().getInstanceList(NodeType.class)) {
                NodeType nodeType = (NodeType) nodeTypeObj;
                if (nodeType.getPrettyId().equals(prettyId)) {
                    return nodeType;
                }
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private EdgeType getEdgeType(String prettyId)
    {
        try {
            for (Object edgeTypeObj : componentManagerProvider.get().getInstanceList(EdgeType.class)) {
                EdgeType edgeType = (EdgeType) edgeTypeObj;
                if (edgeType.getPrettyId().equals(prettyId)) {
                    return edgeType;
                }
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        return null;
    }
}
