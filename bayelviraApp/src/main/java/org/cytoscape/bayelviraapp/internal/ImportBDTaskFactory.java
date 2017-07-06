/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.bayelviraapp.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

/**
 *
 * @author Victor Potenciano
 */
public class ImportBDTaskFactory extends AbstractNetworkViewTaskFactory {

    protected CyServiceRegistrar serviceRegistrarRef;
    
    ImportBDTaskFactory(CyServiceRegistrar serviceRef) {
        this.serviceRegistrarRef = serviceRef;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetworkView cnv) {
        return new TaskIterator(new ImportBDTask(serviceRegistrarRef, cnv));
    }

}
