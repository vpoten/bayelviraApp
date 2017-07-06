/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.bayelviraapp.internal;

import java.io.InputStream;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.TaskIterator;

/**
 *
 * @author Victor Potenciano
 */
public class ArffTableReaderFactory extends AbstractInputStreamTaskFactory {

    private CyTableFactory tableFactory;
	
    public ArffTableReaderFactory(CyFileFilter filter, CyTableFactory tableFactory) {
            super(filter);
            this.tableFactory = tableFactory;
    }
        
    @Override
    public TaskIterator createTaskIterator(InputStream stream, String inputName) {
        return new TaskIterator( new ArffTableReader(stream, this.tableFactory) );
    }
    
}
