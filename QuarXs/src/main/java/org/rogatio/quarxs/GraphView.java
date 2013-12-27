package org.rogatio.quarxs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jgroups.util.UUID;
import org.rogatio.quarxs.layout.CircularLayout;
import org.rogatio.quarxs.layout.GraphLayout;
import org.rogatio.quarxs.layout.HierarchicLayout;
import org.rogatio.quarxs.layout.OrganicLayout;
import org.rogatio.quarxs.layout.OrthogonalLayout;
import org.rogatio.quarxs.layout.TreeLayout;

import y.base.EdgeCursor;
import y.base.NodeCursor;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.module.HierarchicLayoutModule;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.ImageNodeRealizer;
import y.view.MagnifierViewMode;
import y.view.NodeRealizer;
import y.view.ViewMode;

/**
 * This class is the graph layouting core of the framework.
 * 
 * @version $Id$
 */
public class GraphView extends JPanel
{

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 5909538989685200106L;

    /**
     * YGraphView which is used to layout graph
     */
    private Graph2DView view;

    /**
     * Mapping of org.rogatio.quarxs.Node to the prettyId
     */
    private Hashtable<String, org.rogatio.quarxs.Node> quarxsNodes;

    /**
     * Mapping of y.base.Node to the prettyId
     */
    private Hashtable<String, y.base.Node> yworksNodes;

    /**
     * Mapping of org.rogatio.quarxs.Edge to the prettyId
     */
    private Hashtable<String, org.rogatio.quarxs.Edge> quarxsEdges;

    /**
     * Mapping of y.base.Edge to the prettyId
     */
    private Hashtable<String, y.base.Edge> yworksEdges;

    /**
     * Remove Edge from Graph
     * 
     * @param edge
     */
    public void removeEdge(org.rogatio.quarxs.Edge edge)
    {
        String id = edge.getPrettyId();
        quarxsEdges.remove(id);
        yworksEdges.remove(id);
    }

    /**
     * Remove Node from Graph
     * 
     * @param node
     */
    public void removeNode(org.rogatio.quarxs.Node node)
    {
        String id = node.getPrettyId();
        quarxsNodes.remove(id);
        yworksNodes.remove(id);
    }

    /**
     * Table which holds all possible layouts
     */
    private Hashtable<String, GraphLayout> layouts;

    /**
     * Add GraphLayout to Graph
     * 
     * @param layout
     */
    public void addLayout(GraphLayout layout)
    {
        layouts.put(layout.getDescription(), layout);
    }

    /**
     * Initialize Graph
     */
    public void init()
    {
        Graph2D graph = new Graph2D();
        EdgeRealizer er = graph.getDefaultEdgeRealizer();
        er.setArrow(Arrow.STANDARD);
        graph.setDefaultEdgeRealizer(er);
        view = new Graph2DView(graph);
        arrowType = Arrow.STANDARD;
        quarxsNodes = new Hashtable<String, org.rogatio.quarxs.Node>();
        yworksNodes = new Hashtable<String, y.base.Node>();
        quarxsEdges = new Hashtable<String, org.rogatio.quarxs.Edge>();
        yworksEdges = new Hashtable<String, y.base.Edge>();
        layouts = new Hashtable<String, GraphLayout>();

        addLayout(new CircularLayout());
        addLayout(new HierarchicLayout());
        addLayout(new OrganicLayout());
        addLayout(new OrthogonalLayout());
        addLayout(new TreeLayout());

        uuid = UUID.randomUUID();
    }

    /**
     * UUID
     */
    private UUID uuid;

    /**
     * Constructor for Graph
     */
    public GraphView()
    {
        super();
        init();
    }

    /**
     * Width of Graph View
     */
    private int width;

    /**
     * Height of Graph View
     */
    private int height;

    /**
     * Size of Font in Pixels
     */
    private int fontSize;

    /**
     * Returns size of font in pixels
     * 
     * @return
     */
    public int getFontSize()
    {
        return fontSize;
    }

    /**
     * Sets size of Font in Pixels
     * 
     * @param fontSize
     */
    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }

    /**
     * Set width and height of Graph View
     * 
     * @param width
     * @param height
     */
    public void setDimension(int width, int height)
    {
        this.height = height;
        this.width = width;
        Dimension dim = new Dimension(width, height);
        view.setMaximumSize(dim);
        view.setMaximumSize(dim);
        view.setPreferredSize(dim);
    }

    /**
     * Master-Node defines the Node which is bound to the displaying wiki-document
     */
    private org.rogatio.quarxs.Node masterNode;

    /**
     * Set the Master-Node of the displayed graph
     * 
     * @param node
     */
    public void setMaster(org.rogatio.quarxs.Node node)
    {
        masterNode = node;
    }

    /**
     * Size of Node in pixel
     */
    private int nodeSize;

    /**
     * Returns size of Node
     * 
     * @return
     */
    public int getNodeSize()
    {
        return nodeSize;
    }

    /**
     * Set size of Node
     * 
     * @param nodeSize
     */
    public void setNodeSize(int nodeSize)
    {
        this.nodeSize = nodeSize;
    }

    /**
     * Checks if Edge with Source-Node and Target-Node exists in graph
     * 
     * @param source
     * @param target
     * @return
     */
    public boolean containsEdge(org.rogatio.quarxs.Node source, org.rogatio.quarxs.Node target)
    {
        for (Edge edge : getEdges()) {
            if (edge.getSource().getPrettyId().equals(source.getPrettyId())
                && edge.getTarget().getPrettyId().equals(target.getPrettyId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if edge exists in graph
     * 
     * @param edge
     * @return
     */
    public boolean containsEdge(org.rogatio.quarxs.Edge edge)
    {
        if (quarxsEdges.containsKey(edge.getPrettyId())) {
            return true;
        }
        return false;
    }

    /**
     * Add an Edge to Graph and create a YEdge
     * 
     * @param edge
     * @return
     */
    public y.base.Edge addEdge(org.rogatio.quarxs.Edge edge)
    {
        org.rogatio.quarxs.Node source = edge.getSource();
        org.rogatio.quarxs.Node target = edge.getTarget();

        if (source == null) {
            System.err.println("Source-Node in edge '" + edge.getPrettyId() + "' not exists to create Edge");
            return null;
        }
        if (target == null) {
            System.err.println("Target-Node in edge '" + edge.getPrettyId() + "' not exists to create Edge");
            return null;
        }

        quarxsEdges.put(edge.getPrettyId(), edge);

        if (edge.getType()==null) {
            System.out.println("ERROR: Edge '"+edge.getPrettyId()+"' has no defined Type. Edge is not correctly added to graph.");   
        }
        
        if (quarxsNodes.containsKey(source.getPrettyId()) && quarxsNodes.containsKey(target.getPrettyId())) {
            y.base.Edge e =
                this.createEdge(yworksNodes.get(source.getPrettyId()), yworksNodes.get(target.getPrettyId()),
                    edge.getLabel());
            yworksEdges.put(edge.getPrettyId(), e);
            return e;
        } else {
            if (!quarxsNodes.containsKey(source.getPrettyId())) {
                System.err.println("Source-Node '" + source.getPrettyId() + "' not exists to create Edge");
            }
            if (!quarxsNodes.containsKey(target.getPrettyId())) {
                System.err.println("Target-Node '" + target.getPrettyId() + "' not exists to create Edge");
            }
        }
        return null;
    }

    /**
     * Returns the YEdge which corresponds with Edge
     * 
     * @param edge
     * @return
     */
    private y.base.Edge getYEdge(org.rogatio.quarxs.Edge edge)
    {
        return yworksEdges.get(edge.getPrettyId());
    }

    /**
     * Returns YNode which corresponds to Node
     * 
     * @param node
     * @return
     */
    private y.base.Node getYNode(org.rogatio.quarxs.Node node)
    {
        return yworksNodes.get(node.getPrettyId());
    }

    /**
     * Add Node to graph and create YNode with
     * 
     * @param node
     * @return
     */
    public y.base.Node addNode(org.rogatio.quarxs.Node node)
    {

        quarxsNodes.put(node.getPrettyId(), node);

        NodeRealizer nr = view.getGraph2D().getDefaultNodeRealizer().createCopy();
        try {
            if (node.getType() != null) {
                ImageNodeRealizer inr = new ImageNodeRealizer();
                inr.setImageURL(new URL(node.getType().getIconUrl()));
                nr = inr;
            }
        } catch (MalformedURLException e) {
            nr = view.getGraph2D().getDefaultNodeRealizer().createCopy();
            nr.setFillColor(Color.orange);
        }
        nr.setSize(nodeSize, nodeSize);
        nr.setLabelText(node.getLabel());
        y.base.Node n = view.getGraph2D().createNode(nr);
        yworksNodes.put(node.getPrettyId(), n);

        this.addNodeType(node.getType());
        return n;
    }

    /**
     * Calculates the Ending of an edge for given Arrow-Type
     * 
     * @param start
     * @param end
     * @return
     */
    private String getJsArrow(Point start, Point end)
    {
        Point p0 = start;
        Point p1 = end;
        double centerX = (p1.getX() + p0.getX()) * 0.5d;
        double centerY = (p1.getY() + p0.getY()) * 0.5d;
        double dx = (p1.getX() - p0.getX());
        double dy = (p1.getY() - p0.getY());
        double length = Math.sqrt(dx * dx + dy * dy);
        double dxNormalized = dx / length;
        double dyNormalized = dy / length;
        double arrowScaleFactor = zoomFactor;
        double offset = arrowScaleFactor * (arrowType.getArrowLength() + arrowType.getClipLength()) * 0.5d;
        double translationX = p1.getX() - centerX;
        double translationY = p1.getY() - centerY;
        double x = centerX + offset * dxNormalized + translationX;
        double y = centerY + offset * dyNormalized + translationY;
        double dxn = arrowScaleFactor * dxNormalized;
        double dyn = arrowScaleFactor * dyNormalized;
        AffineTransform affinetransform = new AffineTransform(dxn, dyn, -dyn, dxn, x, y);

        // Move arrow to centerd position
        int offsetX = canvasWidth / 2 - view.getGraph2D().getBoundingBox().width / 2;
        int offsetY =
            (int) arrowType.getArrowLength() / 2 + canvasHeight / 2 - view.getGraph2D().getBoundingBox().height / 2;

        // Create Javascript
        StringBuilder sb = new StringBuilder();
        sb.append("    ctx.beginPath();\n");
        PathIterator iterator = arrowType.getShape().getPathIterator(affinetransform);
        double[] segmentCoords = new double[6];
        iterator.currentSegment(segmentCoords);
        double xp = segmentCoords[0];
        double yp = segmentCoords[1];
        sb.append("    ctx.moveTo(" + (offsetX + xp) + "," + (offsetY + yp) + ");\n");
        iterator.next();
        while (!iterator.isDone()) {
            iterator.currentSegment(segmentCoords);
            xp = segmentCoords[0];
            yp = segmentCoords[1];
            sb.append("    ctx.lineTo(" + (offsetX + xp) + "," + (offsetY + yp) + ");\n");
            iterator.next();
        }
        sb.append("    ctx.fill();\n");

        return sb.toString();
    }

    /**
     * Returns HTML-Script which could be embedded in wiki-page
     * 
     * @return
     */
    public String getScript()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("{{html clean=\"false\"}}\n");

        for (NodeType nt : getNodeTypes()) {
            sb.append(getNodeImageJs(nt.getPrettyId(), nt.getIconUrl(), nt.getPrettyId()));
        }

        sb.append(getCanvasJs());

        for (org.rogatio.quarxs.Edge e : getEdges()) {
            sb.append(getJsString(e));
        }

        for (org.rogatio.quarxs.Node n : getNodes()) {
            sb.append(getJsString(n));
        }

        sb.append("  </script>\n");
        sb.append("{{/html}}\n");

        return sb.toString();
    }

    /**
     * Returns Script for NodeImages
     * 
     * @param id
     * @param url
     * @param alt
     * @return
     */
    private String getNodeImageJs(String id, String url, String alt)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("  <img id=\"" + id + "\" src=\"" + url + "\" alt=\"" + alt + "\" style=\"display:none\">\n");
        return sb.toString();
    }

    private static final String ICON_EDITNODE = "shape_square_edit.png";

    private static final String ICON_EDITEDGE = "link_edit.png";

    private static final String ICON_SAVE = "disk.png";

    private static final String ICON_DELETE = "shape_square_delete.png";

    private static final String ICON_DELETEEDGE = "link_delete.png";

    private static final String ICON_CREATE = "shape_square_add.png";

    private static final String ICON_CREATEEDGE = "link_add.png";
    
    private static final String ICON_DATA = "database.png";

    // private static final String ICON_NOEDIT = "control_stop_blue.png";
    private static final String ICON_LAYOUT = "layout_content.png";

    private int canvasWidth;

    private int canvasHeight;

    /**
     * Set Html5-Canvas with Mouse-Action
     * 
     * @return
     */
    private String getCanvasJs()
    {
        canvasWidth = (int) (view.getGraph2D().getBoundingBox().width * 1.5);
        canvasHeight = (int) (view.getGraph2D().getBoundingBox().height * 1.5);

        StringBuilder sb = new StringBuilder();
        // Create Menubar
        sb.append("  <table id=\"graphMenu\" width=\""
            + width
            + "px\"><tr style=\"background: #c3c3c3;\">"
            + "<td id=\"graphBar01\"><form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_EDITNODE
            + "' alt='No Edit'></button></form></td><td id=\"graphBar02\"><form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_DELETE
            + "' alt='No Delete'></button></form></td><td id=\"graphBar03\"><form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_CREATE
            + "' alt='No Create'></button></form></td>"
            + "<td style=\"white-space:nowrap;\" id=\"graphBar04\"><form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_CREATEEDGE + "' alt='No Create'></button></form></td>"
            + "<td style=\"white-space:nowrap;\" id=\"graphBar05\"><form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_DATA + "' alt='No Create'></button></form></td>"
            + "<td style=\"white-space:nowrap;\" id=\"layoutSelect\"><form><select name=\"layout\">");

        for (String layout : layouts.keySet()) {
            sb.append("<option value=\"" + layout + "\">" + layout + "</option>");
        }
        sb.append("</select><button title='Set Layout' type='submit'><img src='../../../resources/icons/silk/"
            + ICON_LAYOUT + "' alt='Layout'></button></form></td>"
            + "<td width=\"100%\">&nbsp;</td></tr><tr><td width=\"100%\" colspan=\"7\" id=\"infoBar\">&nbsp;</td>"
            + "</tr></table>\n");
        // Create Canvas
        sb.append("  <div style=\"overflow: scroll; width: " + width + "px; height: " + height
            + "px; border:1px solid #c3c3c3;\" id=\"graphContainer\">\n");
        sb.append("    <canvas id=\"graphCanvas" + uuid.toString() + "\" width=\"" + canvasWidth + "\" height=\""
            + canvasHeight + "\">\n");
        sb.append("      Your browser does not support the HTML5 canvas tag.\n");
        sb.append("    </canvas>\n");
        sb.append("  </div>\n");
        // get canvas to set mouse listener
        sb.append("  <script>\n");
        sb.append("    var canvas=document.getElementById(\"graphCanvas" + uuid.toString() + "\");\n");
        sb.append("    var ctx=canvas.getContext(\"2d\");\n");
        sb.append("    canvas.addEventListener('mousedown', mouseAction, false);\n");
        sb.append("    canvas.addEventListener('dblclick', mouseClickAction, false);\n");
        sb.append("    // Mouse Action for Selection\n");
        sb.append("    function mouseAction(mouseEvent) {\n");
        sb.append("       var mouseX = mouseEvent.pageX-canvas.offsetLeft + document.getElementById(\"graphContainer\").scrollLeft;\n");
        sb.append("       var mouseY = mouseEvent.pageY-canvas.offsetTop + document.getElementById(\"graphContainer\").scrollTop;\n");
        sb.append("       document.getElementById(\"graphBar01\").innerHTML=\"<form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_EDITNODE + "' alt='No Edit'></button></form>\";\n");
        sb.append("       document.getElementById(\"graphBar02\").innerHTML=\"<form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_DELETE + "' alt='No Delete'></button></form>\";\n");
        sb.append("       document.getElementById(\"graphBar03\").innerHTML=\"<form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_CREATE + "' alt='No Create'></button></form>\";\n");
        sb.append("       document.getElementById(\"graphBar05\").innerHTML=\"<form><button type='submit' disabled><img src='../../../resources/icons/silk/"
            + ICON_DATA + "' alt='No Create'></button></form>\";\n");
        sb.append("       document.getElementById(\"infoBar\").innerHTML=\"&nbsp;\";\n");

        int offsetX = canvasWidth / 2 - view.getGraph2D().getBoundingBox().width / 2;
        int offsetY = canvasHeight / 2 - view.getGraph2D().getBoundingBox().height / 2;

        for (org.rogatio.quarxs.Node n : this.getNodes()) {
            y.base.Node node = getYNode(n);
            sb.append("       // Check if Mouse is in Area of Node '" + n.getLabel() + "'\n");
            sb.append("       if (mouseX>" + (offsetX + getCoordinateUpperLeft(node).x) + "&&mouseX<"
                + (offsetX + getCoordinateUpperLeft(node).x + nodeSize * zoomFactor) + "&&mouseY>"
                + (offsetY + getCoordinateUpperLeft(node).y) + "&&mouseY<"
                + (offsetY + getCoordinateUpperLeft(node).y + nodeSize * zoomFactor) + ") {\n");
            StringTokenizer st = new StringTokenizer(n.getPrettyId(), ".");
            String space = st.nextToken();
            String wiki = st.nextToken();
            sb.append("         document.getElementById(\"infoBar\").innerHTML=\"<b>" + n.getLabel() + "</b>\";\n");
            sb.append("         document.getElementById(\"graphBar01\").innerHTML=\"<form method='get' action='../../../bin/edit/"
                + space
                + "/"
                + wiki
                + "?editor=object'><button title='Edit "
                + n.getLabel()
                + "' type='submit'><img src='../../../resources/icons/silk/"
                + ICON_EDITNODE
                + "' alt='Edit"
                + n.getLabel() + "'></button></form>\";\n");
            sb.append("         document.getElementById(\"graphBar02\").innerHTML=\"<form><button title='Delete Node "
                + n.getLabel() + "' type='submit' name='deletenode' value='" + n.getPrettyId()
                + "'><img src='../../../resources/icons/silk/" + ICON_DELETE + "' alt='Delete'></button></form>\";\n");
            sb.append("         document.getElementById(\"graphBar03\").innerHTML=\"<form><button title='Add Node' type='submit' name='createnode' value='node'><img src='../../../resources/icons/silk/"
                + ICON_CREATE + "' alt='Create Node'></button></form>\";\n");
            sb.append("         document.getElementById(\"graphBar05\").innerHTML=\"<form><button title='Show Data' type='submit' name='showdata' value='" + n.getPrettyId()
                + "'><img src='../../../resources/icons/silk/"
                + ICON_DATA + "' alt='Show Data'></button></form>\";\n");
            sb.append("       }\n");
        }
        for (org.rogatio.quarxs.Edge e : getEdges()) {
            y.base.Edge edge = getYEdge(e);
            if (edge != null) {
                StringTokenizer st = new StringTokenizer(e.getPrettyId(), ".");
                String space = st.nextToken();
                String wiki = st.nextToken();
                Point pos = this.getLabelPosition(edge);
                double w = view.getGraph2D().getRealizer(edge).getLabel().getWidth();
                double h = view.getGraph2D().getRealizer(edge).getLabel().getHeight();
                sb.append("       // Check if Mouse is in Area of Edge '" + e.getLabel() + "'\n");
                sb.append("       if (mouseX>" + (offsetX + pos.x) + "&&mouseX<" + (offsetX + pos.x + w) + "&&mouseY>"
                    + (offsetY + pos.y) + "&&mouseY<" + (offsetY + pos.y + h) + ") {\n");
                sb.append("         document.getElementById(\"graphBar01\").innerHTML=\"<form method='get' action='../../../bin/edit/"
                    + space
                    + "/"
                    + wiki
                    + "?editor=object'><button type='submit'><img src='../../../resources/icons/silk/"
                    + ICON_EDITEDGE
                    + "' alt='Edit" + e.getLabel() + "'></button></form>\";\n");
                sb.append("         document.getElementById(\"graphBar02\").innerHTML=\"<form><button title='Delete Edge "
                    + e.getLabel()
                    + "' type='submit' name='deleteedge' value='"
                    + e.getPrettyId()
                    + "'><img src='../../../resources/icons/silk/"
                    + ICON_DELETEEDGE
                    + "' alt='Delete'></button></form>\";\n");
                sb.append("         document.getElementById(\"graphBar05\").innerHTML=\"<form><button title='Show Data' type='submit' name='showdata' value='" + e.getPrettyId()
                    + "'><img src='../../../resources/icons/silk/"
                    + ICON_DATA + "' alt='Show Data'></button></form>\";\n");
                sb.append("         document.getElementById(\"infoBar\").innerHTML=\"<b>" + e.getSource().getLabel()
                    + "</b> " + e.getLabel() + " <b>" + e.getTarget().getLabel() + "</b>\";\n");
                sb.append("       }\n");
            }
        }

        sb.append("    }\n");
        sb.append("    // Action for Doubleclick on Node\n");
        sb.append("    function mouseClickAction(mouseEvent) {\n");
        sb.append("       var mouseX = mouseEvent.pageX-canvas.offsetLeft + document.getElementById(\"graphContainer\").scrollLeft;\n");
        sb.append("       var mouseY = mouseEvent.pageY-canvas.offsetTop + document.getElementById(\"graphContainer\").scrollTop;\n");
        for (org.rogatio.quarxs.Node n : this.getNodes()) {
            y.base.Node node = getYNode(n);
            sb.append("       // Check if Mouse is in Area of Node '" + n.getLabel() + "'\n");
            sb.append("       if (mouseX>" + (offsetX + getCoordinateUpperLeft(node).x) + "&&mouseX<"
                + (offsetX + getCoordinateUpperLeft(node).x + nodeSize * zoomFactor) + "&&mouseY>"
                + (offsetY + getCoordinateUpperLeft(node).y) + "&&mouseY<"
                + (offsetY + getCoordinateUpperLeft(node).y + nodeSize * zoomFactor) + ") {\n");
            StringTokenizer st = new StringTokenizer(n.getPrettyId(), ".");
            String space = st.nextToken();
            String wiki = st.nextToken();
            sb.append("          window.location.href = \"../../../bin/view/" + space + "/" + wiki + "\";\n");
            sb.append("       }\n");
        }
        sb.append("    }\n");
        return sb.toString();
    }

    public Point getLabelPosition(y.base.Edge edge)
    {

        if (edge == null) {
            return new Point();
        }

        YPoint point = view.getGraph2D().getRealizer(edge).getLabel().getTextLocation();
        Point p = new Point();
        p.setLocation(point.x, point.y);
        return p;
    }

    /**
     * List of NodeType
     */
    private List<NodeType> nodeTypes = new ArrayList<NodeType>();

    /**
     * Gives back NodeTypes in actual graph
     * 
     * @return
     */
    public List<NodeType> getNodeTypes()
    {
        return nodeTypes;
    }

    /**
     * Add NodeType to graph
     * 
     * @param nodeType
     */
    public void addNodeType(NodeType nodeType)
    {
        if (nodeType == null) {
            return;
        }
        if (!contains(nodeType)) {
            nodeTypes.add(nodeType);
        }
    }

    /**
     * Checks if NodeType already exists in graph
     * 
     * @param nodeType
     * @return
     */
    public boolean contains(NodeType nodeType)
    {
        for (NodeType nt : nodeTypes) {
            if (nt.getPrettyId().equals(nodeType.getPrettyId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Arrow which is used by edges
     */
    private Arrow arrowType;

    /**
     * Creates Script for Node
     * 
     * @param n
     * @return
     */
    private String getJsString(org.rogatio.quarxs.Node n)
    {
        // get corresponding y-Node for Node
        y.base.Node node = getYNode(n);

        // Text for Node-Label
        String label = n.getLabel();

        // Calculates Offset to Center Graph in Size of Html5-Canvas (incl.
        // y-Offset for Nodes to fit with Edges)
        int offsetX = canvasWidth / 2 - view.getGraph2D().getBoundingBox().width / 2;
        int offsetY = canvasHeight / 2 - view.getGraph2D().getBoundingBox().height / 2;

        StringBuilder script = new StringBuilder();

        script.append("    // Generated Drawing of Node '" + label + "'\n");

        if (n.getType() != null) {
            if (!n.getType().hasIcon()) {
                // If no Icon is found then draw grey box
                script.append("    ctx.strokeStyle = \""+masterNode.getType().getColor()+"\";\n");
                script.append("    ctx.lineWidth = "+masterNode.getType().getWidth()+";\n");
                script.append("    ctx.fillStyle = \"grey\";\n");
                script.append("    ctx.beginPath();\n");
                script.append("    ctx.arc(" + (offsetX + getCoordinateCenter(node).x) + ","
                    + (offsetY + getCoordinateCenter(node).y) + "," + nodeSize * zoomFactor / 2 + ",0,2*Math.PI);\n");
                script.append("    ctx.fill();\n");
                script.append("    ctx.fillStyle = \"black\";\n");
            } else {
                String nodeTypePrettyId = n.getType().getPrettyId();
                // Draw Image
                script.append("    ctx.drawImage( document.getElementById(\"" + nodeTypePrettyId + "\") ,"
                    + (offsetX + getCoordinateUpperLeft(node).x) + "," + (offsetY + getCoordinateUpperLeft(node).y)
                    + "," + nodeSize * zoomFactor + "," + nodeSize * zoomFactor + ");\n");
                script.append("    ctx.drawImage( document.getElementById(\"" + nodeTypePrettyId + "\") ,"
                    + (offsetX + getCoordinateUpperLeft(node).x) + "," + (offsetY + getCoordinateUpperLeft(node).y)
                    + "," + (nodeSize * zoomFactor + 0.5) + "," + (nodeSize * zoomFactor + 0.5) + ");\n");
            }
        } else {
            // If nodeType is not defined
            script.append("    ctx.fillStyle = \"red\";\n");
            script.append("    ctx.beginPath();\n");
            script.append("    ctx.arc(" + (offsetX + getCoordinateCenter(node).x) + ","
                + (offsetY + getCoordinateCenter(node).y) + "," + nodeSize * zoomFactor / 2 + ",0,2*Math.PI);\n");
            script.append("    ctx.fill();\n");
            script.append("    ctx.fillStyle = \"black\";\n");
        }

        if (masterNode != null) {
            if (n.getPrettyId().equals(masterNode.getPrettyId())) {
//                System.out.println(masterNode.getLabel());
//                System.out.println(masterNode.getType());
//                System.out.println(masterNode.getType().getColor());
                script.append("    ctx.strokeStyle = \""+masterNode.getType().getColor()+"\";\n");
                script.append("    ctx.lineWidth = "+masterNode.getType().getWidth()+";\n");
                script.append("    ctx.beginPath();\n");
                script.append("    ctx.arc(" + (offsetX + getCoordinateCenter(node).x) + ","
                    + (offsetY + getCoordinateCenter(node).y) + "," + nodeSize * zoomFactor / 2 + 2
                    + ",0,2*Math.PI);\n");
                script.append("    ctx.stroke();\n");
                script.append("    ctx.lineWidth = 1;\n");
                script.append("    ctx.strokeStyle = \"black\";\n");
            }
        }

        // Calculates in JavaScript Width of Text
        script.append("    var textWidth = ctx.measureText(\"" + label + "\").width;\n");
        // Place x-Position of Node
        script.append("    var xPosNode = " + (offsetX + getCoordinateCenter(node).x) + "- textWidth/2;\n");
        // Center Text at y-Position
        script.append("    ctx.textBaseline = 'middle';\n");
        // Set Foreground-Color
        script.append("    ctx.fillStyle = \"black\";\n");
        // Draw Foreground-Text
        script.append("    ctx.font=\"" + fontSize + "px Arial\";\n");
        script
            .append("    ctx.fillText(\"" + label + "\",xPosNode," + (offsetY + getCoordinateCenter(node).y) + ");\n");
        script.append("    ctx.fillText(\"" + label + "\",xPosNode+0.25,"
            + (offsetY + getCoordinateCenter(node).y + 0.25) + ");\n");

        return script.toString();
    }

    /**
     * Creates Script for Edge
     * 
     * @param e
     * @return
     */
    private String getJsString(org.rogatio.quarxs.Edge e)
    {

        y.base.Edge edge = getYEdge(e);

        if (edge == null) {
            return "";
        }

        if (e.getType()==null) {
//            System.err.println("ERROR: Edge '"+e.getPrettyId()+"' has no Type defined.");
            return "";
        }
        
        String edgeTypeConnection = e.getType().getConnection();
        StringBuilder sb = new StringBuilder();
        sb.append("    // Generated Drawing of Edge '"+e.getSource().getLabel()+" " + e.getLabel() +" "+ e.getTarget().getLabel()+"'\n");

        List<Point> points = getCoordinates(edge);

        sb.append("    ctx.strokeStyle = \""+e.getType().getColor()+"\";\n");
        sb.append("    ctx.fillStyle = \""+e.getType().getColor()+"\";\n");
        sb.append("    ctx.lineWidth="+e.getType().getWidth()+";\n");
        
        // calculates Offset which centers Graph
        int offsetX = canvasWidth / 2 - view.getGraph2D().getBoundingBox().width / 2;
        int offsetY =
            (int) arrowType.getArrowLength() / 2 + canvasHeight / 2 - view.getGraph2D().getBoundingBox().height / 2;

        if (edgeTypeConnection.equals("Bidirectional") || edgeTypeConnection.equals("Unidirectional")) {

            if (points.size() == 0) {
                System.out.println("ERROR: Points of Edge '" + e.getLabel() + "' are unknown!");
                return "";
            }

            // draw End-Arrow
            Point p0 = points.get(points.size() - 2);
            Point p1 = points.get(points.size() - 1);
            sb.append(this.getJsArrow(p0, p1));
        }

        if (edgeTypeConnection.equals("Bidirectional")) {
            // draw Start-Arrow
            Point p0 = points.get(1);
            Point p1 = points.get(0);
            sb.append(this.getJsArrow(p0, p1));
        }

        if (edgeTypeConnection.equals("Related")) {
            // do nothing specific
        }

        sb.append("    ctx.beginPath();\n");
        
        // draw Lines from Edge
        for (int i = 0; i < points.size(); i++) {
            if ((i + 1) < points.size()) {
                Point pFrom = points.get(i);
                sb.append("    ctx.moveTo(" + (offsetX + pFrom.x) + "," + (offsetY + pFrom.y) + ");\n");
                Point pTo = points.get(i + 1);
                sb.append("    ctx.lineTo(" + (offsetX + pTo.x) + "," + (offsetY + pTo.y) + ");\n");
                sb.append("    ctx.stroke();\n");
            }
        }

        sb.append("    ctx.textBaseline = 'middle';\n");
//        sb.append("    ctx.fillStyle = \"black\";\n");
        sb.append("    ctx.font=\"" + fontSize + "px Arial\";\n");
        sb.append("    ctx.fillText(\"" + e.getLabel() + "\"," + (offsetX + getLabelPosition(edge).x) + ","
            + (offsetY + getLabelPosition(edge).y) + ");\n");

        return sb.toString();
    }

    /**
     * Returns List of y.base.Edge
     * 
     * @return
     */
    private List<y.base.Edge> getYEdges()
    {
        ArrayList<y.base.Edge> list = new ArrayList<y.base.Edge>();
        for (EdgeCursor nc = view.getGraph2D().edges(); nc.ok(); nc.next()) {
            list.add(nc.edge());
        }
        return list;
    }

    /**
     * returns List of org.rogatio.quarxs.Edge
     * 
     * @return
     */
    public List<org.rogatio.quarxs.Edge> getEdges(org.rogatio.quarxs.Node node)
    {
        List<org.rogatio.quarxs.Edge> list = new ArrayList<org.rogatio.quarxs.Edge>();
        Collection<org.rogatio.quarxs.Edge> c = quarxsEdges.values();
        for (Iterator<org.rogatio.quarxs.Edge> iterator = c.iterator(); iterator.hasNext();) {
            org.rogatio.quarxs.Edge edge = (org.rogatio.quarxs.Edge) iterator.next();
            if (edge.contains(node)) {
                list.add(edge);
            }
        }
        return list;
    }

    public List<org.rogatio.quarxs.Edge> getEdges()
    {
        List<org.rogatio.quarxs.Edge> list = new ArrayList<org.rogatio.quarxs.Edge>();
        Collection<org.rogatio.quarxs.Edge> c = quarxsEdges.values();
        for (Iterator<org.rogatio.quarxs.Edge> iterator = c.iterator(); iterator.hasNext();) {
            org.rogatio.quarxs.Edge edge = (org.rogatio.quarxs.Edge) iterator.next();
            list.add(edge);
        }
        return list;
    }

    public List<org.rogatio.quarxs.Node> getNeighbours(org.rogatio.quarxs.Node node)
    {
        List<org.rogatio.quarxs.Node> list = new ArrayList<org.rogatio.quarxs.Node>();

        Collection<org.rogatio.quarxs.Edge> c = quarxsEdges.values();
        for (Iterator<org.rogatio.quarxs.Edge> iterator = c.iterator(); iterator.hasNext();) {
            org.rogatio.quarxs.Edge edge = (org.rogatio.quarxs.Edge) iterator.next();

            if (edge.contains(node)) {
                org.rogatio.quarxs.Node neigbour = edge.getOpposite(node);
                if (!list.contains(neigbour)) {
                    list.add(neigbour);
                }
            }
        }

        return list;
    }

    /**
     * Returns List of org.rogatio.quarxs.Node
     * 
     * @return
     */
    public List<org.rogatio.quarxs.Node> getNodes()
    {
        List<org.rogatio.quarxs.Node> list = new ArrayList<org.rogatio.quarxs.Node>();
        Collection<org.rogatio.quarxs.Node> c = quarxsNodes.values();
        for (Iterator<org.rogatio.quarxs.Node> iterator = c.iterator(); iterator.hasNext();) {
            org.rogatio.quarxs.Node node = (org.rogatio.quarxs.Node) iterator.next();
            list.add(node);
        }
        return list;
    }

    /**
     * Returns List of y.base.Node
     * 
     * @return
     */
    private List<y.base.Node> getYNodes()
    {
        ArrayList<y.base.Node> list = new ArrayList<y.base.Node>();
        for (NodeCursor nc = view.getGraph2D().nodes(); nc.ok(); nc.next()) {
            list.add(nc.node());
        }
        return list;
    }

    /**
     * Label of YNode
     * 
     * @param node
     * @return
     */
    private String getYLabel(y.base.Node node)
    {
        return view.getGraph2D().getRealizer(node).getLabelText();
    }

    /**
     * Get UpperLeft-Point of YNode
     * 
     * @param node
     * @return
     */
    public Point getCoordinateUpperLeft(y.base.Node node)
    {
        Point p = new Point();
        p.setLocation(view.getGraph2D().getRealizer(node).getCenterX() - this.nodeSize * zoomFactor / 2, view
            .getGraph2D().getRealizer(node).getCenterY()
            - this.nodeSize * zoomFactor / 2);
        return p;
    }

    /**
     * Get Center-Point of YNode
     * 
     * @param node
     * @return
     */
    public Point getCoordinateCenter(y.base.Node node)
    {
        Point p = new Point();
        p.setLocation(view.getGraph2D().getRealizer(node).getCenterX(), view.getGraph2D().getRealizer(node)
            .getCenterY());
        return p;
    }

    /**
     * Points of Path from YEdge
     * 
     * @param edge
     * @return
     */
    public List<Point> getCoordinates(y.base.Edge edge)
    {
        List<Point> points = new ArrayList<Point>();
        double[] segmentCoords = new double[6];

        PathIterator iterator = view.getGraph2D().getRealizer(edge).getPath().getPathIterator(new AffineTransform());
        while (!iterator.isDone()) {
            iterator.currentSegment(segmentCoords);
            Point p = new Point();
            p.setLocation(segmentCoords[0], segmentCoords[1]);
            points.add(p);
            iterator.next();
        }
        return points;
    }

    /**
     * Create YEdge from YNodes
     * 
     * @param source
     * @param target
     * @return
     */
    private y.base.Edge createEdge(y.base.Node source, y.base.Node target, String label)
    {
        EdgeRealizer er = view.getGraph2D().getDefaultEdgeRealizer().createCopy();
        er.setLabelText(label);
        return view.getGraph2D().createEdge(source, target, er);
    }

    /**
     * Create YEdge from YNodes
     * 
     * @param source
     * @param target
     * @return
     */
    private y.base.Edge createEdge(y.base.Node source, y.base.Node target)
    {
        return view.getGraph2D().createEdge(source, target);
    }

    /**
     * Action to Fit Graph to given View
     */
    private class ZoomAll implements ActionListener
    {

        Graph2DView _view;

        public ZoomAll(Graph2DView view)
        {
            _view = view;
        }

        public void actionPerformed(ActionEvent e)
        {
            _view.fitContent();
            _view.updateView();
        }

    }

    /**
     * Fit Graph to given View
     */
    public void zoomAll()
    {
        view.fitContent();
        view.updateView();
    }

    /**
     * Action to Zoom into graph
     */
    private class ZoomIn implements ActionListener
    {

        Graph2DView _view;

        public ZoomIn(Graph2DView view)
        {
            _view = view;
        }

        public void actionPerformed(ActionEvent e)
        {
            _view.setZoom(_view.getZoom() * 1.2);
            _view.updateView();
        }

    }

    /**
     * Zoom Graph with Magnifier
     */
    private class ZoomMagnifier implements ActionListener
    {

        MagnifierViewMode _magnifierMode = new MagnifierViewMode();

        Graph2DView _view;

        public ZoomMagnifier(Graph2DView view)
        {
            _view = view;
            _magnifierMode.setMagnifierRadius(100);
            _magnifierMode.setMagnifierZoomFactor(2.0);
        }

        public void actionPerformed(ActionEvent e)
        {

            boolean hasMagnifier = false;
            Iterator<?> it = _view.getViewModes();
            while (it.hasNext()) {
                ViewMode vm = (ViewMode) it.next();
                if (vm.getName().equals("NAVIGATION_MAGNIFIER")) {
                    hasMagnifier = true;
                }
            }

            if (hasMagnifier) {
                _view.removeViewMode(_magnifierMode);
            } else {
                _view.addViewMode(_magnifierMode);
            }

            _view.updateView();
        }

    }

    /**
     * Action to zoom out of graph in given view
     */
    private class ZoomOut implements ActionListener
    {

        Graph2DView _view;

        public ZoomOut(Graph2DView view)
        {
            _view = view;
        }

        public void actionPerformed(ActionEvent e)
        {
            _view.setZoom(_view.getZoom() * 0.8);
            _view.updateView();
        }

    }

    /**
     * Zoom selected nodes and edges into given view
     */
    private class ZoomSelection implements ActionListener
    {

        Graph2DView _view;

        public ZoomSelection(Graph2DView view)
        {
            _view = view;
        }

        public void actionPerformed(ActionEvent e)
        {
            NodeCursor nc = _view.getGraph2D().selectedNodes();
            if (nc.ok()) // selected nodes present?
            {
                Rectangle2D box = _view.getGraph2D().getRealizer(nc.node()).getBoundingBox();
                for (nc.next(); nc.ok(); nc.next())
                    _view.getGraph2D().getRealizer(nc.node()).calcUnionRect(box);
                // zoom to box area
                _view.zoomToArea(box.getX(), box.getY(), box.getWidth(), box.getHeight());
                // allow a maximum zoom level of 2.0.
                if (_view.getZoom() > 2.0)
                    _view.setZoom(2.0);
                // update view
                _view.updateView();
            }
        }
    }

    /**
     * Resize YNodes to label size of YNode
     */
    public void resizeNodesByLabel()
    {
        for (NodeCursor nc = view.getGraph2D().nodes(); nc.ok(); nc.next()) {
            YRectangle xy = view.getGraph2D().getRealizer(nc.node()).getLabel().getBox();
            view.getGraph2D().getRealizer(nc.node()).setHeight(xy.getHeight() + 10);
            view.getGraph2D().getRealizer(nc.node()).setWidth(xy.getWidth() + 40);
        }
    }

    public void resizeNodesBySize()
    {
        for (NodeCursor nc = view.getGraph2D().nodes(); nc.ok(); nc.next()) {
            YRectangle xy = view.getGraph2D().getRealizer(nc.node()).getLabel().getBox();
            view.getGraph2D().getRealizer(nc.node()).setHeight(zoomFactor * nodeSize);
            view.getGraph2D().getRealizer(nc.node()).setWidth(zoomFactor * nodeSize);
        }
    }

    public void calculate(String layoutName, double zoom)
    {
        view.setZoom(zoom);
        GraphLayout layout = layouts.get(layoutName);
        layout.calculate(view.getGraph2D());
        view.updateView();
        zoomFactor = zoom;
    }

    public void calculate(String layoutName)
    {
        GraphLayout layout = layouts.get(layoutName);
        layout.calculate(view.getGraph2D());
        view.fitContent();
        view.updateView();
        zoomFactor = view.getZoom();
    }

    private double zoomFactor = 1.0;

    public double getZoomFactor()
    {
        return zoomFactor;
    }

    public void setZoomFactor(double zoomFactor)
    {
        this.zoomFactor = zoomFactor;
    }

    /**
     * Add Action-Buttons to Graph-View-Panel
     */
    private void addLayout()
    {

        JButton zoomAll = new JButton("1:1");
        zoomAll.setToolTipText("Zoom All");
        zoomAll.addActionListener(new ZoomAll(view));

        JButton zoomIn = new JButton("In");
        zoomIn.setToolTipText("Zoom In");
        zoomIn.addActionListener(new ZoomIn(view));

        JButton zoomOut = new JButton("Out");
        zoomOut.setToolTipText("Zoom Out");
        zoomOut.addActionListener(new ZoomOut(view));

        JButton zoomSelection = new JButton("Selected");
        zoomSelection.setToolTipText("Zoom Selection");
        zoomSelection.addActionListener(new ZoomSelection(view));

        JButton magnifier = new JButton("Magnifier");
        magnifier.setToolTipText("Magnifier");
        magnifier.addActionListener(new ZoomMagnifier(view));

        JPanel buttonBar = new JPanel(new GridLayout(1, 5));
        buttonBar.add(zoomAll);
        buttonBar.add(zoomIn);
        buttonBar.add(zoomOut);
        buttonBar.add(zoomSelection);
        buttonBar.add(magnifier);

        this.setLayout(new BorderLayout());
        this.add(buttonBar, BorderLayout.NORTH);
        this.add(view, BorderLayout.CENTER);

    }

}
