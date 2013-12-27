package org.rogatio.quarxs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.rogatio.quarxs.DefaultEdge;
import org.rogatio.quarxs.DefaultEdgeType;
import org.rogatio.quarxs.DefaultNode;
import org.rogatio.quarxs.DefaultNodeType;
import org.rogatio.quarxs.Edge;
import org.rogatio.quarxs.EdgeType;
import org.rogatio.quarxs.Node;
import org.rogatio.quarxs.NodeType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Helper Class to create HSQL-Query-Strings and get Object-Data from Database
 * 
 * @version $Id$
 */
public class ObjectQuery
{

    private static String create(String className, String... values)
    {
        String select = new String();
        String from = new String();
        String where = new String();

        for (int i = 0; i < values.length; i++) {
            String prop = "prop" + i;
            String v = values[i];
            select += prop + ".value";
            from += "StringProperty as " + prop;
            where += "and obj.id=" + prop + ".id.id and " + prop + ".id.name='" + v + "'";
            if (i + 1 < values.length) {
                select += ", ";
                from += ", ";
            }
        }

        return "select " + select + " from BaseObject as obj, " + from + " where obj.className='" + className + "' "
            + where;
    }

    public static Edge getEdge(String objectPrettyId, QueryManager queryManager, XWikiContext context)
    {
        String objectGUID = PrettyIdConverter.getGuid(objectPrettyId);
        if (objectGUID == null) {
            return null;
        }

        try {
            List<Object[]> results = new ArrayList<Object[]>();
            // If Label not contained, then use other query
            boolean WITHOUTLABEL = false;
            if (objectPrettyId.contains(". (")) {
                Query query =
                    queryManager.createQuery(ObjectQuery.create(Constants.SPACE + "." + Edge.CLASS + "Class",
                        "prettyid", "edgetype", "nodesource", "nodetarget"), Query.HQL);
                results = query.execute();
                WITHOUTLABEL = true;
            } else {
                Query query =
                    queryManager.createQuery(ObjectQuery.create(Constants.SPACE + "." + Edge.CLASS + "Class",
                        "prettyid", "label", "edgetype", "nodesource", "nodetarget"), Query.HQL);
                results = query.execute();
                WITHOUTLABEL = false;
            }
            for (Object[] result : results) {
                if (result[0].toString().contains(objectGUID)) {

                    String prettyid = "";
                    String label = "";
                    String edgetype = "";
                    String sourcedesc = "";
                    String targetdesc = "";

                    if (WITHOUTLABEL) {
                        prettyid = result[0].toString();
                        label = "";
                        edgetype = result[1].toString();
                        sourcedesc = result[2].toString();
                        targetdesc = result[3].toString();
                    } else {
                        prettyid = result[0].toString();
                        label = result[1].toString();
                        edgetype = result[2].toString();
                        sourcedesc = result[3].toString();
                        targetdesc = result[4].toString();
                    }

                    DefaultEdge e = new DefaultEdge();
                    e.setPrettyId(prettyid);
                    e.setLabel(label);
                    String spaceName = PrettyIdConverter.getSpace(objectPrettyId);
                    String docName = PrettyIdConverter.getDocumentName(objectPrettyId);
                    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);
                    XWikiDocument doc = context.getWiki().getDocument(docRef, context);
                    e.setDocument(doc);
                    e.setAuthorReference(doc.getAuthorReference());
                    EdgeType nt = getEdgeType(edgetype, queryManager);
                    Node source = getNode(sourcedesc, queryManager, context);

                    if (source != null) {
                        if (!source.getPrettyId().equals(sourcedesc)) {
                            System.out.println("In Edge '" + e.getPrettyId() + "' is referenced Source-Node '"
                                + source.getPrettyId() + "' with old '" + result[3].toString() + "'");
                        }
                    }

                    Node target = getNode(targetdesc, queryManager, context);
                    if (target != null) {
                        if (!target.getPrettyId().equals(targetdesc)) {
                            System.out.println("In Edge '" + e.getPrettyId() + "' is referenced Target-Node '"
                                + target.getPrettyId() + "' with old '" + result[4].toString() + "'");
                        }
                    }
                    e.setSource(source);
                    e.setTarget(target);
                    e.setType(nt);
                    // System.out.println("Check: Found NT "+nt+" for Node "+label);
                    e.setGuid(objectGUID);
                    return e;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List<Node> getNodes(QueryManager queryManager, XWikiContext context)
    {
        List<Node> list = new ArrayList<Node>();
        try {
            Query query =
                queryManager.createQuery(
                    ObjectQuery.create(Constants.SPACE + "." + Node.CLASS + "Class", "prettyid", "label", "nodetype"),
                    Query.HQL);
            List<Object[]> results = query.execute();
            for (Object[] result : results) {
                DefaultNode n = new DefaultNode();
                String objectPrettyId = result[0].toString();
                n.setPrettyId(objectPrettyId);
                n.setLabel(result[1].toString());
                String spaceName = PrettyIdConverter.getSpace(objectPrettyId);
                String docName = PrettyIdConverter.getDocumentName(objectPrettyId);
                DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);
                XWikiDocument docTarget = context.getWiki().getDocument(docRef, context);
                n.setDocument(docTarget);
                n.setAuthorReference(docTarget.getAuthorReference());
                NodeType nt = getNodeType(result[2].toString(), queryManager);
                n.setType(nt);
                n.setGuid(PrettyIdConverter.getGuid(objectPrettyId));
                list.add(n);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public static Node getNode(String objectPrettyId, QueryManager queryManager, XWikiContext context)
    {
        String objectGUID = PrettyIdConverter.getGuid(objectPrettyId);
        if (objectGUID == null) {
            return null;
        }
        try {
            Query query =
                queryManager.createQuery(
                    ObjectQuery.create(Constants.SPACE + "." + Node.CLASS + "Class", "prettyid", "label", "nodetype"),
                    Query.HQL);
            List<Object[]> results = query.execute();
            for (Object[] result : results) {
                if (result[0].toString().contains(objectGUID)) {
                    DefaultNode n = new DefaultNode();
                    n.setPrettyId(result[0].toString());
                    n.setLabel(result[1].toString());
                    if (!PrettyIdConverter.getName(objectPrettyId).equals(result[1].toString())) {
                        // System.out.println(result[1].toString()+" is not "+PrettyIdConverter.getNodeName(objectPrettyId));
                    }
                    String spaceName = PrettyIdConverter.getSpace(objectPrettyId);
                    String docName = PrettyIdConverter.getDocumentName(objectPrettyId);
                    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);
                    XWikiDocument docTarget = context.getWiki().getDocument(docRef, context);
                    n.setDocument(docTarget);
                    n.setAuthorReference(docTarget.getAuthorReference());
                    NodeType nt = getNodeType(result[2].toString(), queryManager);
                    n.setType(nt);
                    n.setGuid(objectGUID);
                    return n;
                } else {
                    // System.out.println(objectGUID+"=?="+result[0]);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static EdgeType getEdgeType(String objectPrettyId, QueryManager queryManager)
    {
        String objectGUID = PrettyIdConverter.getGuid(objectPrettyId);
        if (objectGUID == null) {
            return null;
        }

        try {
            Query query =
                queryManager.createQuery(
                    create(Constants.SPACE + "." + EdgeType.CLASS + "Class", "prettyid", "name", "connection",
                        "defaultlabel", "color", "width", "entity"), Query.HQL);
            List<Object[]> results = query.execute();

            if (results.size() == 0) {
                // System.out.println("No Results!");
                query =
                    queryManager.createQuery(
                        create(Constants.SPACE + "." + EdgeType.CLASS + "Class", "prettyid", "name", "connection",
                            "defaultlabel", "color", "width"), Query.HQL);
                results = query.execute();
            } else {
                for (Object[] result : results) {
                    if (result[0].toString().contains(objectGUID)) {
                        DefaultEdgeType t = new DefaultEdgeType();
                        t.setPrettyId(result[0].toString());
                        t.setName(result[1].toString());
                        t.setConnection(result[2].toString());
                        t.setDefaultLabel(result[3].toString());
                        t.setColor(result[4].toString());
                        t.setWidth(Integer.parseInt(result[5].toString()));
                        t.setEntity(result[6].toString());
                        t.setGuid(objectGUID);
                        return t;
                    }
                }
            }

            if (results.size() == 0) {
                // System.out.println("No Results!");
                query =
                    queryManager.createQuery(
                        create(Constants.SPACE + "." + EdgeType.CLASS + "Class", "prettyid", "name", "connection",
                            "defaultlabel", "color"), Query.HQL);
                results = query.execute();
            } else {
                for (Object[] result : results) {
                    if (result[0].toString().contains(objectGUID)) {
                        DefaultEdgeType t = new DefaultEdgeType();
                        t.setPrettyId(result[0].toString());
                        t.setName(result[1].toString());
                        t.setConnection(result[2].toString());
                        t.setDefaultLabel(result[3].toString());
                        t.setColor(result[4].toString());
                         t.setWidth(Integer.parseInt(result[5].toString()));
                        // t.setEntity(result[6].toString());
                        t.setGuid(objectGUID);
                        return t;
                    }
                }
            }

            if (results.size() == 0) {
                // System.out.println("No Results!");
                query =
                    queryManager.createQuery(
                        create(Constants.SPACE + "." + EdgeType.CLASS + "Class", "prettyid", "name", "connection",
                            "defaultlabel"), Query.HQL);
                results = query.execute();
            } else {
                for (Object[] result : results) {
                    if (result[0].toString().contains(objectGUID)) {
                        DefaultEdgeType t = new DefaultEdgeType();
                        t.setPrettyId(result[0].toString());
                        t.setName(result[1].toString());
                        t.setConnection(result[2].toString());
                        t.setDefaultLabel(result[3].toString());
                         t.setColor(result[4].toString());
                        // t.setWidth(Integer.parseInt(result[5].toString()));
                        // t.setEntity(result[6].toString());
                        t.setGuid(objectGUID);
                        return t;
                    }
                }
            }
            if (results.size() != 0) {
                for (Object[] result : results) {
                    if (result[0].toString().contains(objectGUID)) {
                        DefaultEdgeType t = new DefaultEdgeType();
                        t.setPrettyId(result[0].toString());
                        t.setName(result[1].toString());
                        t.setConnection(result[2].toString());
                        t.setDefaultLabel(result[3].toString());
//                         t.setColor(result[4].toString());
                        // t.setWidth(Integer.parseInt(result[5].toString()));
                        // t.setEntity(result[6].toString());
                        t.setGuid(objectGUID);
                        return t;
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static NodeType getNodeType(String objectPrettyId, QueryManager queryManager)
    {
        String objectGUID = null;
        try {
            objectGUID = objectPrettyId.substring(objectPrettyId.indexOf("(") + 1, objectPrettyId.length() - 1);
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
        try {
            Query query =
                queryManager.createQuery(
                    create(Constants.SPACE + "." + NodeType.CLASS + "Class", "prettyid", "name", "iconurl", "entity",
                        "color", "width"), Query.HQL);
            List<Object[]> results = query.execute();
            for (Object[] result : results) {
                if (result[0].toString().contains(objectGUID)) {
                    DefaultNodeType nt = new DefaultNodeType();
                    nt.setPrettyId(result[0].toString());
                    nt.setName(result[1].toString());
                    nt.setIconUrl(result[2].toString());
                    nt.setEntity(result[3].toString());
                    nt.setColor(result[4].toString());
                    nt.setWidth(Integer.parseInt(result[5].toString()));

                    nt.setGuid(objectGUID);
                    return nt;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static NodeType getNodeTypeMaster(QueryManager queryManager)
    {
        return getNodeTypeByName(queryManager, "Master");
    }

    public static NodeType getNodeTypeDefault(QueryManager queryManager)
    {
        return getNodeTypeByName(queryManager, "Default");
    }

    public static NodeType getNodeTypeByName(QueryManager queryManager, String name)
    {

        try {
            Query query =
                queryManager.createQuery(
                    create(Constants.SPACE + "." + NodeType.CLASS + "Class", "prettyid", "name", "iconurl", "entity",
                        "color", "width"), Query.HQL);

            List<Object[]> results = query.execute();
            if (results.size() == 0) {
                query =
                    queryManager.createQuery(
                        create(Constants.SPACE + "." + NodeType.CLASS + "Class", "prettyid", "name", "iconurl", "entity",
                            "color"), Query.HQL);

                results = query.execute();                
            } else {
                for (Object[] result : results) {
                    if (result[0].toString().contains("." + name + " ")) {
                        DefaultNodeType nt = new DefaultNodeType();
                        nt.setPrettyId(result[0].toString());
                        nt.setName(result[1].toString());
                        nt.setIconUrl(result[2].toString());
                        nt.setEntity(result[3].toString());
                        nt.setColor(result[4].toString());
                        nt.setWidth(Integer.parseInt(result[5].toString()));
                        String objectGUID =
                            nt.getPrettyId()
                                .substring(nt.getPrettyId().indexOf("(") + 1, nt.getPrettyId().length() - 1);
                        nt.setGuid(objectGUID);
                        return nt;
                    }
                }
            }
            if (results.size() == 0) {
                query =
                    queryManager.createQuery(
                        create(Constants.SPACE + "." + NodeType.CLASS + "Class", "prettyid", "name", "iconurl", "entity"), Query.HQL);

                results = query.execute();                
            } else {
                for (Object[] result : results) {
                    if (result[0].toString().contains("." + name + " ")) {
                        DefaultNodeType nt = new DefaultNodeType();
                        nt.setPrettyId(result[0].toString());
                        nt.setName(result[1].toString());
                        nt.setIconUrl(result[2].toString());
                        nt.setEntity(result[3].toString());
                        nt.setColor(result[4].toString());
//                        nt.setWidth(Integer.parseInt(result[5].toString()));
                        String objectGUID =
                            nt.getPrettyId()
                                .substring(nt.getPrettyId().indexOf("(") + 1, nt.getPrettyId().length() - 1);
                        nt.setGuid(objectGUID);
                        return nt;
                    }
                }
            }
            
            if (results.size() == 0) {
                query =
                    queryManager.createQuery(
                        create(Constants.SPACE + "." + NodeType.CLASS + "Class", "prettyid", "name", "iconurl"), Query.HQL);
                results = query.execute();                
            } else {
                for (Object[] result : results) {
                    if (result[0].toString().contains("." + name + " ")) {
                        DefaultNodeType nt = new DefaultNodeType();
                        nt.setPrettyId(result[0].toString());
                        nt.setName(result[1].toString());
                        nt.setIconUrl(result[2].toString());
                        nt.setEntity(result[3].toString());
//                        nt.setColor(result[4].toString());
//                        nt.setWidth(Integer.parseInt(result[5].toString()));
                        String objectGUID =
                            nt.getPrettyId()
                                .substring(nt.getPrettyId().indexOf("(") + 1, nt.getPrettyId().length() - 1);
                        nt.setGuid(objectGUID);
                        return nt;
                    }
                }
            }
            
            if (results.size() != 0) {
                for (Object[] result : results) {
                    if (result[0].toString().contains("." + name + " ")) {
                        DefaultNodeType nt = new DefaultNodeType();
                        nt.setPrettyId(result[0].toString());
                        nt.setName(result[1].toString());
                        nt.setIconUrl(result[2].toString());
//                        nt.setEntity(result[3].toString());
//                        nt.setColor(result[4].toString());
//                        nt.setWidth(Integer.parseInt(result[5].toString()));
                        String objectGUID =
                            nt.getPrettyId()
                                .substring(nt.getPrettyId().indexOf("(") + 1, nt.getPrettyId().length() - 1);
                        nt.setGuid(objectGUID);
                        return nt;
                    }
                }   
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
