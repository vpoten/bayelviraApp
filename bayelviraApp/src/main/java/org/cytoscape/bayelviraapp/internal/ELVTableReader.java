/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.bayelviraapp.internal;

import java.io.InputStream;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author victor
 */
public class ELVTableReader extends AbstractTask  implements CyTableReader {
    private final InputStream stream;
    private CyTableFactory tableFactory;
        
    public ELVTableReader(InputStream stream, CyTableFactory tableFactory) {
        this.stream = stream;
        this.tableFactory = tableFactory;
    }

    @Override
    public void run(TaskMonitor tm) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public CyTable[] getTables(){
        throw new UnsupportedOperationException("Not supported yet.");
    }
     
}
