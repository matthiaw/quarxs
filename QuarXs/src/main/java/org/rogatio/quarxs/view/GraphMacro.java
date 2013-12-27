/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.rogatio.quarxs.view;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.rendering.transformation.TransformationManager;

/**
 * Graph Macro
 */
@Component
@Named("graph")
public class GraphMacro extends AbstractMacro<GraphMacroParameters>
{
    @Inject
    private MacroContentParser contentParser;

    @Inject
    private TransformationManager transformationManager;

    /**
     * The description of the graph macro.
     */
    private static final String DESCRIPTION = "Graph Macro which shows Nodes and Edges (v1.0)";

    /**
     * Create and initialize the descriptor of the macro.
     */
    public GraphMacro()
    {
        super("Graph", DESCRIPTION, GraphMacroParameters.class);
    }

    @Override
    public List<Block> execute(GraphMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {

        StringBuilder contentBuilder = new StringBuilder();

        String layout = parameters.getLayout();
        if (layout != null) {
            if (layout.equals("")) {
                layout = "Hierarchic";
            }
        } else {
            layout = "Hierarchic";
        }

        String width = parameters.getWidth();
        if (width == null) {
            width = "640";
            if (width.equals("")) {
                width = "640";
            }
        }

        String height = parameters.getHeight();
        if (height == null) {
            height = "200";
            if (height.equals("")) {
                height = "640";
            }
        }

        String showMenu = parameters.getMenu();
        if (showMenu != null) {
            if (showMenu.equals("null")) {
                showMenu = "true";
            }
            if (showMenu.equals("")) {
                showMenu = "true";
            }

            if (showMenu.equals("false")) {
                showMenu = "false";
            }
        } else {
            showMenu = "true";
        }

        String showData = parameters.getData();
        if (showData != null) {
            if (showData.equals("null")) {
                showData = "true";
            }
            if (showData.equals("")) {
                showData = "true";
            }

            if (showData.equals("false")) {
                showData = "false";
            }
        } else {
            showData = "true";
        }

        String overwrite = parameters.getOverwrite();
        if (overwrite != null) {
            if (overwrite.equals("true")) {
                overwrite = "true";
            }
        } else {
            overwrite = "false";
        }

        // Hide or Show Menu of Graph if parameter is set
        if (showMenu.equals("false")) {
            contentBuilder.append("{{html clean=\"false\"}}\n");
            contentBuilder.append("<style>\n");
            contentBuilder.append("#graphMenu {\n");
            contentBuilder.append("  display: none;\n");
            contentBuilder.append("}\n");
            contentBuilder.append("</style>\n");
            contentBuilder.append("{{/html}}\n");
        }

        // Hidden Script to check if y-Libary with necessary classes exists
        contentBuilder.append("{{groovy}}\n");
        contentBuilder.append("import org.rogatio.quarxs.DefaultGraph;\n");
        contentBuilder.append("import java.lang.NoClassDefFoundError;\n");
        contentBuilder.append("if (services.graph==null) {\n");
        contentBuilder.append("DefaultGraph g = new DefaultGraph();\n");
        contentBuilder.append("try {\n");
        contentBuilder.append("g.initialize()\n");
        contentBuilder.append("} catch (NoClassDefFoundError e) {\n");
        contentBuilder
            .append("println(\"{{error}} Class \"+e.getMessage().replace(\"/\", \".\")+\" is missing. Is needed Library copied to WEB-INF/lib?{{/error}}\")\n");
        contentBuilder.append("}\n");
        contentBuilder.append("}\n");
        contentBuilder.append("{{/groovy}}\n");

        contentBuilder.append("{{velocity}}\n");
        if (!overwrite.equals("true")) {
            contentBuilder.append("   ## PARAMETER\n");
            contentBuilder.append("   #set($defaultLayout=\"" + layout + "\")\n");
            contentBuilder.append("   #set($width=" + width + ")\n");
            contentBuilder.append("   #set($height=" + height + ")\n");
            // contentBuilder.append("   #set($showTable=\"" + showTable + "\")\n");
            // contentBuilder.append("   #set($showData=\"" + showData + "\")\n");
            // contentBuilder.append("   ## DATAOBJECT\n");
            // contentBuilder
            // .append("   #set($masterId=$doc.getObject(\"QuarXs.NodeClass\").getProperty(\"prettyid\").value)\n");
            // contentBuilder
            // .append("   #set($masterType=$doc.getObject(\"QuarXs.NodeClass\").getProperty(\"nodetype\").value)\n");
            // contentBuilder.append("   #set($typeDocName = $masterType.substring(0, $masterType.lastIndexOf(\".\")))\n");
            // contentBuilder
            // .append("   #set($typeName = $masterType.substring($masterType.lastIndexOf(\".\"), $masterType.indexOf(\"(\")).replace(\".\", \"\").trim())\n");
            // contentBuilder.append("   #set($typeDoc = $xwiki.getDocument($typeDocName))\n");
            // contentBuilder.append("   #set($typeObj = \"\")\n");
            // contentBuilder.append("   #set($dataEnt = \"\")\n");
            // contentBuilder.append("   #set($data = \"\")\n");
            // contentBuilder.append("   #foreach($type in $typeDoc.getObjects(\"QuarXs.NodeTypeClass\"))\n");
            // contentBuilder.append("     #if ($type.get(\"name\").equals(\"$typeName\"))\n");
            // contentBuilder.append("       #set($typeObj = $type)\n");
            // contentBuilder.append("       #set($dataEnt = $typeObj.get(\"entity\"))\n");
            // contentBuilder.append("       #set($data = $doc.getObject($dataEnt))\n");
            // contentBuilder.append("     #end\n");
            // contentBuilder.append("   #end\n");
            
            contentBuilder.append("   ## ACTION: SET AND FILTER GRAPH\n");
            contentBuilder
                .append("   #set($masterId=$doc.getObject(\"QuarXs.NodeClass\").getProperty(\"prettyid\").value)\n");
            contentBuilder
                .append("   #set($masterType=$doc.getObject(\"QuarXs.NodeClass\").getProperty(\"nodetype\").value)\n");
            contentBuilder.append("   $services.graph.setMaster($masterId)\n");
            contentBuilder.append("   #set($filter = {\n");
            contentBuilder.append("     \"mode\": \"neighbour\"\n");
            contentBuilder.append("   })\n");
            contentBuilder.append("   $services.graph.setFilter($filter)\n");
            contentBuilder.append("   ## ACTION: LAYOUT AND DISPLAY GRAPH\n");
            contentBuilder.append("   #if($request.getParameter(\"layout\"))\n");
            contentBuilder.append("     #set($layout = $request.getParameter(\"layout\"))\n");
            contentBuilder.append("     $services.graph.calculate($width, $height, $layout)\n");
            contentBuilder.append("   #else\n");
            contentBuilder.append("     $services.graph.calculate($width, $height, $defaultLayout)\n");
            contentBuilder.append("   #end\n");
            contentBuilder.append("   ## ACTION: CREATE DOC and NODE\n");
            contentBuilder.append("   #if($request.getParameter(\"createnode\"))\n");
            contentBuilder.append("     #set($create = $request.getParameter(\"createnode\"))\n");
            contentBuilder.append("     #set ($newdoc = $xwiki.getDocument(\"${doc.space}.NewNodePage\"))\n");
            contentBuilder.append("     $newdoc.setContent(\"\")\n");
            contentBuilder.append("     $newdoc.save()\n");
            contentBuilder.append("     $response.sendRedirect($doc.getURL('view'))\n");
            contentBuilder.append("   #end\n");
            contentBuilder.append("   ## ACTION: CREATE EDGE\n");
            contentBuilder.append("   #if($request.getParameter(\"createedge\"))\n");
            contentBuilder.append("      #set($create = $request.getParameter(\"createedge\"))\n");
            contentBuilder.append("      $services.graph.createEdge($create)\n");
            contentBuilder.append("      $response.sendRedirect($doc.getURL('view'))\n");
            contentBuilder.append("   #end\n");
            contentBuilder.append("   ## ACTION: DELETE NODE\n");
            contentBuilder.append("   #if($request.getParameter(\"deletenode\"))\n");
            contentBuilder.append("     #set($delete = $request.getParameter(\"deletenode\"))\n");
            contentBuilder.append("     $services.graph.removeNode($delete)\n");
            contentBuilder.append("     $response.sendRedirect($doc.getURL('view'))\n");
            contentBuilder.append("   #end\n");
            contentBuilder.append("   ## ACTION: DELETE EDGE\n");
            contentBuilder.append("   #if($request.getParameter(\"deleteedge\"))\n");
            contentBuilder.append("     #set($delete = $request.getParameter(\"deleteedge\"))\n");
            contentBuilder.append("     $services.graph.removeEdge($delete)\n");
            contentBuilder.append("     $response.sendRedirect($doc.getURL('view'))\n");
            contentBuilder.append("   #end\n");
            contentBuilder.append("   ## ACTION: SHOW DATA\n");
            contentBuilder.append("   #if($request.getParameter(\"showdata\"))\n");
            contentBuilder.append("     #set($dataid = $request.getParameter(\"showdata\"))\n");
            contentBuilder.append("     #set($data = \"\")\n");
            contentBuilder.append("     #set($label = \"\")\n");
            contentBuilder.append("     #set($label = $services.graph.getNode(\"$dataid\").getLabel())\n");
//            contentBuilder.append("     $label\n");
            contentBuilder.append("     #if(!$label)\n");
            contentBuilder.append("        #set($label = $services.graph.getEdge(\"$dataid\").getLabel())\n");
            contentBuilder.append("        #set($src = $services.graph.getEdge(\"$dataid\").getSource().getLabel())\n");
            contentBuilder.append("        #set($trg = $services.graph.getEdge(\"$dataid\").getTarget().getLabel())\n");
            contentBuilder.append("        #set($label = \"Data of Edge: **$src** $label **$trg**\")\n");
            contentBuilder.append("     #else\n");
            contentBuilder.append("        #set($label = \"Data of Node: **$label**\")\n");
            contentBuilder.append("     #end\n");
//            contentBuilder.append("     $label\n");
//            contentBuilder.append("     #if($label.equals(\"\"))\n");
//            contentBuilder.append("        #set($label = $services.graph.getEdge(\"$dataid\").getLabel())\n");
//            contentBuilder.append("     #end\n");
            contentBuilder.append("     $label\n");
            contentBuilder.append("     #set($data = $!services.graph.getData($dataid))\n");
            contentBuilder.append("     #if(!$data.equals(\"\"))\n");
            contentBuilder.append("        #foreach($prop in $data.getPropertyNames())\n");
            contentBuilder.append("            |$prop|$data.getStringValue($prop)\n");
            contentBuilder.append("        #end\n");
            contentBuilder.append("     #end\n");
            contentBuilder.append("   #end\n");
            // contentBuilder.append("   ## DATA\n");
            // contentBuilder.append("   #if (\"true\"==\"$showData\")\n");
            // contentBuilder.append("     #set($check = \"$!data\")\n");
            // contentBuilder.append("     #if(!$check.equals(\"\"))\n");
            // contentBuilder.append("   	  |=Property|=Value\n");
            // contentBuilder.append("   	  #foreach($prop in $data.getPropertyNames())\n");
            // contentBuilder.append("   	    |$prop|$data.get($prop)\n");
            // contentBuilder.append("   	  #end\n");
            // contentBuilder.append("   	#end\n");
            // contentBuilder.append("   #end\n");
            // contentBuilder.append("   ## LIVETABLE\n");
            // contentBuilder.append("   #if (\"true\"==\"$showTable\")\n");
            // contentBuilder.append("     #set($columns = [\"doc.name\", \"label\", \"doc.author\", \"_actions\"])\n");
            // contentBuilder.append("     #set($columnsProperties = {\n");
            // contentBuilder.append("      \"doc.name\": {\"type\":\"text\",\"size\":10,\"link\":\"view\"},\n");
            // contentBuilder.append("      \"label\" : { \"type\" : \"text\" , \"size\" : 20, \"link\" : \"view\"},\n");
            // contentBuilder.append("      \"doc.author\": {\"type\":\"text\",\"size\":10,\"link\":\"author\"},\n");
            // contentBuilder.append("      \"_actions\": {\"sortable\":false,\"filterable\":false,\"html\":true,\"actions\":[\"edit\",\"delete\"]}\n");
            // contentBuilder.append("     })\n");
            // contentBuilder.append("     #set($options = {\n");
            // contentBuilder.append("       \"className\" : \"QuarXs.NodeClass\",\n");
            // contentBuilder.append("       \"translationPrefix\" : \"\",\n");
            // contentBuilder.append("       \"tagCloud\" : true,\n");
            // contentBuilder.append("       \"rowCount\" : 10,\n");
            // contentBuilder.append("       \"selectedColumn\": \"doc.name\",\n");
            // contentBuilder.append("       \"defaultOrder\": \"asc\"\n");
            // contentBuilder.append("     })\n");
            // contentBuilder.append("     #livetable(\"nodetable\" $columns $columnsProperties $options)\n");
            // contentBuilder.append("   #end\n");
        } else {
            contentBuilder.append(content);
        }
        contentBuilder.append("{{/velocity}}\n");

        try {
            transformationManager.performTransformations((Block) new WordBlock(contentBuilder.toString()),
                context.getTransformationContext());
        } catch (TransformationException e) {
            e.printStackTrace();
        }

        List<Block> result =
            this.contentParser.parse(contentBuilder.toString(), context, true, context.isInline()).getChildren();

        return result;
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }
}
