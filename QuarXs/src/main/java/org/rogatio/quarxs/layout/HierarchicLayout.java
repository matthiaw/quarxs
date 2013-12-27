package org.rogatio.quarxs.layout;

import y.module.HierarchicLayoutModule;
import y.module.IncrementalHierarchicLayoutModule;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.EnumOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.view.Graph2D;

public class HierarchicLayout implements GraphLayout
{

    public String getDescription()
    {
        return "Hierarchic";
    }

    public void calculate(Graph2D graph)
    {
        IncrementalHierarchicLayoutModule layout = new IncrementalHierarchicLayoutModule();
        OptionHandler optionhandler = layout.getOptionHandler();
        optionhandler.addBool("SELECTED_ELEMENTS_INCREMENTALLY", false);
        optionhandler.set("ORIENTATION", "TOP_TO_BOTTOM");
        optionhandler.addBool("LAYOUT_COMPONENTS_SEPARATELY", false);
        optionhandler.addBool("SYMMETRIC_PLACEMENT", true);
        optionhandler.addInt("MAXIMAL_DURATION", 5);
        optionhandler.addDouble("NODE_TO_NODE_DISTANCE", 30D);
        optionhandler.addDouble("NODE_TO_EDGE_DISTANCE", 15D);
        optionhandler.addDouble("EDGE_TO_EDGE_DISTANCE", 15D);
        optionhandler.addDouble("MINIMUM_LAYER_DISTANCE", 10D);
        optionhandler.addBool("BACKLOOP_ROUTING", false);
        optionhandler.addBool("AUTOMATIC_EDGE_GROUPING_ENABLED", false);
        optionhandler.addDouble("MINIMUM_FIRST_SEGMENT_LENGTH", 10D);
        optionhandler.addDouble("MINIMUM_LAST_SEGMENT_LENGTH", 15D);
        optionhandler.addDouble("MINIMUM_EDGE_LENGTH", 20D);
        optionhandler.addDouble("MINIMUM_EDGE_DISTANCE", 30D);
        optionhandler.addBool("PC_OPTIMIZATION_ENABLED", false);
        optionhandler.set("RANKING_POLICY", "HIERARCHICAL_OPTIMAL");
        optionhandler.set("LAYER_ALIGNMENT", "CENTER");
        optionhandler.set("EDGE_ROUTING", "EDGE_ROUTING_POLYLINE");
        optionhandler.set("EDGE_LABELING", "EDGE_LABELING_HIERARCHIC");
        optionhandler.set("EDGE_LABEL_MODEL", "EDGE_LABEL_MODEL_SIDE_SLIDER");
        
        layout.setMorphingEnabled(false);
        layout.start(graph);
    }

}
