package org.cytoscape.bayelviraapp.internal;

import java.util.HashSet;
import java.util.Properties;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Victor Potenciano
 */
public class CyActivator extends AbstractCyActivator {

    @Override
    public void start(BundleContext context) throws Exception {

        /// "Hello World!" example
        ///CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
        ///MenuAction action = new MenuAction(cyApplicationManager, "bayelvira App");
        ///Properties properties = new Properties();
        ///registerAllServices(context, action, properties);
        
        CyServiceRegistrar cyServiceRegistrarRef = getService(context,CyServiceRegistrar.class);
        
        // 1 - Register ImportBD task
        
        ImportBDTaskFactory taskFactory = new ImportBDTaskFactory(cyServiceRegistrarRef);
        Properties taskFactoryProps = new Properties();
        taskFactoryProps.setProperty("preferredMenu","Apps");
        taskFactoryProps.setProperty("menuGravity","11.0");
        taskFactoryProps.setProperty("title","bayelvira");
        registerService(context, taskFactory, NetworkViewTaskFactory.class, taskFactoryProps);
        
        // 2 - Register arff table reader
        
        StreamUtil streamUtil = getService(context,StreamUtil.class);		
        CyTableFactory cyDataTableFactoryServiceRef = getService(context,CyTableFactory.class);

        // Define a filter
        HashSet<String> extensions = new HashSet<String>();
        extensions.add( ImportBDTask.ARFF_EXT );
        HashSet<String> contentTypes = new HashSet<String>();
        contentTypes.add("txt");
        String description = "Arff table filter";
        BasicCyFileFilter filter = new BasicCyFileFilter(extensions,contentTypes, description, DataCategory.TABLE, streamUtil);

        // Create an instance of the ReaderFactory
        ArffTableReaderFactory myTableReader = new ArffTableReaderFactory(filter, cyDataTableFactoryServiceRef);

        //register the ReaderFactory as an InputStreamTaskFactory.
        Properties props = new Properties();
        props.setProperty("readerDescription","Arff Table reader");
        props.setProperty("readerId","ArffTableReader");

        registerService(context,myTableReader, InputStreamTaskFactory.class, props);
        
        // 3 - Register dbc table reader        
        
        // Define a filter
        extensions = new HashSet<String>();
        extensions.add( ImportBDTask.DBC_EXT );
        contentTypes = new HashSet<String>();
        contentTypes.add("txt");
        description = "dbc table filter";
        filter = new BasicCyFileFilter(extensions,contentTypes, description, DataCategory.TABLE, streamUtil);

        // Create an instance of the ReaderFactory
        DBCTableReaderFactory dbcTableReader = new DBCTableReaderFactory(filter, cyDataTableFactoryServiceRef);

        //register the ReaderFactory as an InputStreamTaskFactory.
        props = new Properties();
        props.setProperty("readerDescription","dbc Table reader");
        props.setProperty("readerId","DBCTableReader");

        registerService(context, dbcTableReader, InputStreamTaskFactory.class, props);
        
        // 4 - Register elv net reader        
        
        // Define a filter
        extensions = new HashSet<String>();
        extensions.add( ImportBDTask.ELV_EXT );
        contentTypes = new HashSet<String>();
        contentTypes.add("txt");
        description = "elv table filter";
        filter = new BasicCyFileFilter(extensions,contentTypes, description, DataCategory.TABLE, streamUtil);

        // Create an instance of the ReaderFactory
        ELVTableReaderFactory elvTableReader = new ELVTableReaderFactory(filter, cyDataTableFactoryServiceRef);

        //register the ReaderFactory as an InputStreamTaskFactory.
        props = new Properties();
        props.setProperty("readerDescription","elvira native format reader");
        props.setProperty("readerId","ELVTableReader");

        registerService(context, elvTableReader, InputStreamTaskFactory.class, props);
        
    }

}
