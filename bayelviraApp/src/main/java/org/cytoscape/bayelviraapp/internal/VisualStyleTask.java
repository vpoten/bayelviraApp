/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.bayelviraapp.internal;

import java.awt.Color;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author Victor Potenciano
 */
public class VisualStyleTask extends AbstractNetworkViewTask {

    CyServiceRegistrar serviceRegistrarRef = null;
    
    public static final String VISUAL_STYLE_NAME = "Bayelvira visual style";
    
    
    public VisualStyleTask(CyServiceRegistrar serviceRef, CyNetworkView nv) {
        super(nv);
        serviceRegistrarRef = serviceRef;
    }
    
    
    @Override
    public void run(TaskMonitor tm) throws Exception {
        // Visual mapping
        
        VisualMappingManager vmmServiceRef = serviceRegistrarRef.getService(VisualMappingManager.class);
        
        // If the style already existed, remove it first
        for (VisualStyle curVS : vmmServiceRef.getAllVisualStyles()) {
            if ( curVS.getTitle().equalsIgnoreCase(VISUAL_STYLE_NAME) ) {
                    vmmServiceRef.removeVisualStyle(curVS);
                    break;
            }
        }  
        
        VisualStyleFactory visualStyleFactoryServiceRef = serviceRegistrarRef.getService(VisualStyleFactory.class);
        
        VisualMappingFunctionFactory vmfFactoryP = serviceRegistrarRef.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        VisualMappingFunctionFactory vmfFactoryD = serviceRegistrarRef.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        
        // To create a new VisualStyle object and set the mapping function
        VisualStyle vs = visualStyleFactoryServiceRef.createVisualStyle(VISUAL_STYLE_NAME);
        
        // Use passthrough mapping for edge width
        String attributeName = ImportBDTask.ATT_EDGE_DIST;
        PassthroughMapping pMapping = (PassthroughMapping) vmfFactoryP.createVisualMappingFunction(attributeName, 
                Double.class, BasicVisualLexicon.EDGE_WIDTH );
        
        vs.addVisualMappingFunction(pMapping);
        
        // Use discrete mapping for edge color
        String ctrAttrName1 = ImportBDTask.ATT_EDGE_COMPARE;
        DiscreteMapping dMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction(ctrAttrName1, 
                String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        
        for( String key : ImportBDTask.ATT_COMPARE_MAP.keySet() ) {
            dMapping.putMapValue(key, ImportBDTask.ATT_COMPARE_MAP.get(key) );
        }
        
        vs.addVisualMappingFunction(dMapping);     
        
        // Use discrete mapping for node shape
        String nodeAttType = ImportBDTask.ATT_NODE_TYPE;
        DiscreteMapping dMappingNode = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction(nodeAttType, 
                String.class, BasicVisualLexicon.NODE_SHAPE );
        
        for( String key : ImportBDTask.ATT_NODE_TYPE_MAP.keySet() ) {
            dMappingNode.putMapValue(key, ImportBDTask.ATT_NODE_TYPE_MAP.get(key) );
        }
        
        vs.addVisualMappingFunction(dMappingNode);
        
        // Use pass-through mapping for node label
        ctrAttrName1 = ImportBDTask.ATT_NODE_LABEL;
        pMapping = (PassthroughMapping) vmfFactoryP.createVisualMappingFunction(ctrAttrName1, 
                String.class, BasicVisualLexicon.NODE_LABEL);
        vs.addVisualMappingFunction(pMapping);                
        
        //set default styles for edge fill color
        vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE);
        
        // Add the new style to the VisualMappingManager
        vmmServiceRef.addVisualStyle(vs);
        
        // Apply the visual style to a NetworkView
        vs.apply(this.view);
        this.view.updateView();
    }
    
}
