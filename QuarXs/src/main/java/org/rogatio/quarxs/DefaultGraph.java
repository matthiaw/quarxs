package org.rogatio.quarxs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.rogatio.quarxs.util.Constants;
import org.rogatio.quarxs.util.ObjectQuery;
import org.rogatio.quarxs.util.PrettyIdConverter;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.ObservationManager;
import org.xwiki.query.QueryManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Graph which is used as Data AccessHandler for the Script to the GraphView.
 * 
 * @author Matthias Wegner
 */
@Component
@Singleton
public class DefaultGraph implements Graph, Initializable
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    // @Inject
    // private ObservationManager observationManager;

    @Inject
    private Logger logger;

    private GraphView graph;

    /**
     * Calculates Graph with default layout and size
     */
    @Override
    public String calculate()
    {
        return calculate(640, 320);
    }

    /**
     * Initializes Graph
     */
    @Override
    public void initialize() throws InitializationException
    {
        graph = new GraphView();
        graph.setDimension(640, 320);
        graph.setNodeSize(30);
        graph.setFontSize(12);
        // System.out.println("Graph initialized");
        // observationManager.removeListener("graph");
        // observationManager.addListener(new GraphEventListener());
    }

    @Override
    public String calculate(int width, int height)
    {
        return calculate(width, height, "Hierarchic");
    }

    @Inject
    private Execution execution;

    @Override
    public void removeEdge(String prettyId)
    {
        // System.out.println(prettyId);
        try {
            QueryManager queryManager = (QueryManager) componentManagerProvider.get().getInstance(QueryManager.class);
            Edge edge = ObjectQuery.getEdge(prettyId, queryManager, getContext());
            remove(edge);
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeNode(String prettyId)
    {
        try {
            QueryManager queryManager = (QueryManager) componentManagerProvider.get().getInstance(QueryManager.class);
            Node node = ObjectQuery.getNode(prettyId, queryManager, getContext());
            remove(node);
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
    }

    private XWikiContext getContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

    @Override
    public void remove(Edge edge)
    {
        // System.out.println(edge);
        if (edge == null) {
            return;
        }
        // System.out.println("PI: "+edge.getPrettyId());
        BaseObject edgeObject =
            edge.getDocument().getXObject(
                edge.getDocument().resolveClassReference(Constants.SPACE + "." + Edge.CLASS + "Class"), "prettyid",
                edge.getPrettyId());
        // System.out.println("EO: "+edgeObject);
        if (edgeObject == null) {
            return;
        }

        graph.removeEdge(edge);

        boolean success = edge.getDocument().removeXObject(edgeObject);
        if (success) {
            try {
                getContext().getWiki().saveDocument(edge.getDocument(), getContext());
            } catch (XWikiException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Edge " + edge.getPrettyId() + " deleted!");
    }

    @Override
    public void remove(Node node)
    {
        if (node == null) {
            return;
        }

        // Search for all edges which have a connection to the node and delete
        // them
        try {
            List<Edge> toRemove = new ArrayList<Edge>();
            for (Object edgeObj : componentManagerProvider.get().getInstanceList(Edge.class)) {
                Edge edge = (Edge) edgeObj;
                boolean markAsRemoveItem = false;
                if (edge.getSource() != null) {
                    if (edge.getSource().getPrettyId().equals(node.getPrettyId())) {
                        markAsRemoveItem = true;
                    }
                }
                if (edge.getTarget() != null) {
                    if (edge.getTarget().getPrettyId().equals(node.getPrettyId())) {
                        markAsRemoveItem = true;
                    }
                }
                if (markAsRemoveItem) {
                    toRemove.add(edge);
                }
            }
            for (Edge edge : toRemove) {
                remove(edge);
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }

        // Find BaseObject of Node which is neccesary to delete the node
        BaseObject nodeObject =
            node.getDocument().getXObject(
                node.getDocument().resolveClassReference(Constants.SPACE + "." + Node.CLASS + "Class"), "prettyid",
                node.getPrettyId());
        if (nodeObject == null) {
            return;
        }

        // Delete Node and save the change of deletion to the corresponding
        // document of the node
        boolean success = node.getDocument().removeXObject(nodeObject);

        graph.removeNode(node);

        if (success) {
            try {
                // System.out.println("Save");
                getContext().getWiki().saveDocument(node.getDocument(), getContext());
                // System.out.println("delete");
                getContext().getWiki().deleteDocument(node.getDocument(), getContext());
            } catch (XWikiException e) {
                e.printStackTrace();
            }
        }

        // getContext().getWiki().deleteDocument(node.getDocument(),
        // getContext());
    }

    //
    // public void create() {
    // getContext().getWiki().getDocument("", "", context)
    // }

    public boolean isAddable(Node node)
    {
        boolean add = true;

        if (node == null) {
            return false;
        }

        if (node.getDocument() == null) {
            return false;
        }

        if (node.getDocument().isHidden()) {
            return false;
        }

        if (filter == null) {
            return true;
        } else {
            String prettyId = node.getPrettyId();
            String space = PrettyIdConverter.getSpace(prettyId);
            String doc = PrettyIdConverter.getDocumentName(prettyId);

            if (filter.getMode() != null) {
                if (!filter.getMode().equals("")) {
                    if (filter.getMode().equals("neighbour")) {

                        boolean tempadd = false;

                        if (master != null) {
                            if (node.getPrettyId().equals(master.getPrettyId())) {
                                tempadd = true;
                                // System.out.println("Neighbour: Master "+master.getLabel());
                            } else {
                                tempadd = false;
                            }
                        }

                        try {
                            for (Object edgeObj : componentManagerProvider.get().getInstanceList(Edge.class)) {
                                Edge edge = (Edge) edgeObj;
                                if (master != null) {

                                    if (edge.contains(master)) {
                                        Node n = edge.getOpposite(master);

                                        if (n != null) {

                                            // System.out.println(edge.getLabel());
                                            // System.out.println(n.getLabel());

                                            // tempadd = false;
                                            // } else// {
                                            if (node.getPrettyId().equals(n.getPrettyId())) {
                                                tempadd = true;
                                            }
                                        }

                                    }

                                    // if (add) {
                                    // System.out.println(node.getLabel());
                                    // }
                                }
                            }

                            add = add && tempadd;

                        } catch (ComponentLookupException e) {
                            System.out.println(e);
                        }
                    }
                }
            }

            if (filter.getNodelabel() != null) {
                if (!filter.getNodelabel().equals("")) {
                    if (node.getLabel().contains(filter.getNodelabel())) {
                        add = add && true;
                        // System.out.println("Filter '" + filter.getNodelabel()
                        // + "' on NodeLabel: " + node.getLabel());
                    } else {
                        add = add && false;
                        // System.out.println("NodeName does not match '" +
                        // filter.getNodelabel() + "'");
                    }
                } else {
                    // System.out.println("NameFilter is Empty");
                }
            } else {
                // System.out.println("NameFilter is Null");
            }

            if (filter.getSpace() != null) {
                if (!filter.getSpace().equals("")) {
                    if (space.contains(filter.getSpace())) {
                        add = add && true;
                        // System.out.println("Filter '" + filter.getSpace() +
                        // "' on Space: " + space);
                    } else {
                        add = add && false;
                        // System.out.println("Space does not match '" +
                        // filter.getSpace() + "'");
                    }
                } else {
                    // System.out.println("SpaceFilter is Empty");
                }
            } else {
                // System.out.println("SpaceFilter is Null");
            }

            if (filter.getDoc() != null) {
                if (!filter.getDoc().equals("")) {
                    if (doc.contains(filter.getDoc())) {
                        add = add && true;
                        // System.out.println("Filter '" + filter.getDoc() +
                        // "' Doc: " + doc);
                    } else {
                        add = add && false;
                        // System.out.println("Doc does not match '" +
                        // filter.getDoc() + "'");
                    }
                } else {
                    // System.out.println("DocFilter is Empty");
                }
            } else {
                // System.out.println("DocFilter is Null");
            }
            if (filter.getNodetype() != null) {
                if (!filter.getNodetype().equals("")) {
                    if (node.getType() != null) {
                        if (node.getType().getName() != null) {
                            if (node.getType().getName().contains(filter.getNodetype())) {
                                add = add && true;
                                // System.out.println("Filter '" +
                                // filter.getNodetype() + "' on NodeType: " +
                                // node.getType().getName());
                            } else {
                                // System.out.println("Nodetype does not match '"
                                // + filter.getNodetype() + "'");
                                add = add && false;
                            }
                        } else {
                            // System.out.println("Nodetype has no name");
                            add = add && false;
                        }
                    } else {
                        // System.out.println("Nodetype is null");
                        add = add && false;
                    }
                } else {
                    // System.out.println("TypeFilter is Empty");
                }
            } else {
                // System.out.println("TypeFilter is Null");
            }
        }

        if (add) {
            // System.out.println("Node '" + node.getLabel() + "' is addable.");
        }

        return add;
    }

    @Override
    public String calculate(int width, int height, String layout)
    {
        graph.init();
        // System.out.println("Graph: Initialized");
        graph.setDimension(width, height);
        graph.setNodeSize(30);
        graph.setFontSize(12);
        graph.setZoomFactor(1.0);
        graph.setMaster(master);
        // System.out.println("Graph: Master Node '"+master.getLabel()+"' set");
        try {
            for (Object nodeObj : componentManagerProvider.get().getInstanceList(Node.class)) {
                Node node = (Node) nodeObj;
                if (isAddable(node)) {
                    graph.addNode(node);
                    // System.out.println("   Add Node "+node.getLabel());
                }
            }
            for (Object edgeObj : componentManagerProvider.get().getInstanceList(Edge.class)) {
                Edge edge = (Edge) edgeObj;
                boolean add = false;

                if (isAddable(edge.getSource()) && isAddable(edge.getTarget())) {
                    add = true;
                }

                if (add) {
                    graph.addEdge(edge);
                    // System.out.println("   Add Edge "+edge.getLabel());
                }
            }
        } catch (ComponentLookupException e) {
            return "No Graph Elements could be read.";
        }
        graph.calculate(layout);

        String script = graph.getScript();

        StringBuilder result = new StringBuilder();
        StringBuilder formResult = new StringBuilder();
        result.append(script);

        result.append("\n{{html}}\n");
        formResult.append("<form name='edgeForm'>");
        
        List<Node> possibleNodes = new ArrayList<Node>(); 
        try {
            for (Object nodeObj : componentManagerProvider.get().getInstanceList(Node.class)) {
                Node node = (Node) nodeObj;
                
                if ((!graph.containsEdge(master, node))&&(!master.getPrettyId().equals(node.getPrettyId()))) {
                    possibleNodes.add(node);
                }
            }
        } catch (ComponentLookupException e) {
            return "No Graph Elements could be read.";
        }
        
        if (possibleNodes.size()>0) {
            formResult.append("<select name='createedge'>");
            for (Node node : possibleNodes) {
                formResult.append("<option value='"+master.getPrettyId()+", "+node.getPrettyId()+"'> "+node.getLabel()+"</option>");
            }
            formResult.append("</select>");
            formResult.append("<button title='Add Edge from "+master.getLabel()+" to Selection' type='submit'><img src='../../../resources/icons/silk/link_add.png' alt='No Create'></button>");            
        } else {
            formResult.append("</select>");
            formResult.append("<button title='No Edges to Add' type='submit' disabled><img src='../../../resources/icons/silk/link_add.png' alt='No Create'></button>");    
        }

        
        
        formResult.append("</form>");
        result.append("<script>\n");
        
        result.append("  document.getElementById(\"graphBar04\").innerHTML=\""+formResult.toString()+"\";\n");
        
        result.append("</script>\n");
        result.append("{{/html}}\n");

        return result.toString();
    }

    private GraphFilter filter;

    @Override
    public void setFilter(String jsonFilter)
    {
        // System.out.println(jsonFilter);
        Gson gson = new GsonBuilder().create();
        filter = gson.fromJson(jsonFilter, GraphFilter.class);
    }

    Node master;

    @Override
    public void setMasterNode(String prettyidMaster)
    {
        try {
            for (Object nodeObj : componentManagerProvider.get().getInstanceList(Node.class)) {
                Node node = (Node) nodeObj;
                if (node.getPrettyId().contains(prettyidMaster)) {
                    master = node;
                }
            }
        } catch (ComponentLookupException e) {
        }
    }

    @Override
    public void createEdge(String prettyidConnection)
    {
        // prettyidConnection = prettyidConnection.replace("edge:", "");
        String source = prettyidConnection.substring(0, prettyidConnection.indexOf(",")).trim();
        String target =
            prettyidConnection.substring(prettyidConnection.indexOf(",") + 1, prettyidConnection.length()).trim();

        // try {
        // QueryManager queryManager = (QueryManager)
        // componentManagerProvider.get().getInstance(QueryManager.class);
        // Edge sourceEdge = ObjectQuery.getEdge(source, queryManager,
        // getContext());
        // } catch (ComponentLookupException e) {
        // }
        //
        // try {
        // QueryManager queryManager = (QueryManager)
        // componentManagerProvider.get().getInstance(QueryManager.class);
        // Edge targetEdge = ObjectQuery.getEdge(target, queryManager,
        // getContext());
        // } catch (ComponentLookupException e) {
        // }

        try {
            String spaceName = PrettyIdConverter.getSpace(source);
            String docName = PrettyIdConverter.getDocumentName(source);
            XWikiContext context = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
            DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);
            XWikiDocument doc = context.getWiki().getDocument(docRef, context);

            EntityReference entRef = doc.resolveClassReference(Constants.SPACE + "." + Edge.CLASS + "Class");

            int objectIndex = doc.createXObject(entRef, context);
            BaseObject obj = doc.getXObjects(entRef).get(objectIndex);

            obj.set("nodesource", source, context);
            obj.set("nodetarget", target, context);
            obj.set("prettyid", doc.getDocumentReference().getLastSpaceReference().getName() + "."
                + doc.getDocumentReference().getName() + ". (" + obj.getGuid() + ")", context);
            obj.set("edgetype", getEdgeTypeDefault().getPrettyId(), context);

            context.getWiki().saveDocument(doc, context);
        } catch (XWikiException e) {
            e.printStackTrace();
        }

        // System.out.println(source + " --> " + target);

    }

    private EdgeType getEdgeTypeDefault()
    {
        try {
            for (Object edgeTypeObj : componentManagerProvider.get().getInstanceList(EdgeType.class)) {
                EdgeType edgeType = (EdgeType) edgeTypeObj;
                if (edgeType.getName().equals("Default")) {
                    return edgeType;
                }
            }
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        return null;
    }

}
