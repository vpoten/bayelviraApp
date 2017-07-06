/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.bayelviraapp.internal;

import elvira.InvalidEditException;
import elvira.database.DataBaseCases;
import elvira.learning.classification.supervised.discrete.DiscreteClassifier;
import elvira.learning.classification.supervised.discrete.Naive_Bayes;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author victor
 */
public class ImportBDTaskTest {
    
    static protected DataBaseCases dbc = null;
    
    public ImportBDTaskTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, InvalidEditException {
        InputStream istr = ImportBDTaskTest.class.getClassLoader().getSystemResourceAsStream("ms_105snps.arff");
        dbc = ImportBDTask.parseArff(istr, "test");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    

    /**
     * Test of parseCSV method, of class ImportBDTask.
     */
//    @org.junit.Test
//    public void testParseCSV() throws Exception {
//        System.out.println("parseCSV");
//        ImportBDTask instance = null;
//        DataBaseCases expResult = null;
//        DataBaseCases result = instance.parseCSV();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of parseArff method, of class ImportBDTask.
     */
    @org.junit.Test
    public void testParseArff() throws Exception {
        System.out.println("parseArff");
        
        assertNotNull(dbc);
        assertEquals( dbc.getNodeList().size(), 105);
    }

    /**
     * Test of createNetwork method, of class ImportBDTask.
     */
    @org.junit.Test
    public void testCreateNetwork() throws InvalidEditException, Exception {
        System.out.println("createNetwork");
        
        DataBaseCases trainSet = new DataBaseCases();
        DataBaseCases testSet = new DataBaseCases();
        dbc.divideIntoTrainAndTest(trainSet, testSet, 0.7);
            
        DiscreteClassifier classif = new Naive_Bayes(trainSet, true);
        classif.train();
        classif.test(testSet);
        
        NetworkTestSupport nts = new NetworkTestSupport();
        CyNetwork network = nts.getNetwork();
        
        ImportBDTask.createNetwork(null, classif, network);
        ImportBDTask.calcMetrics(classif, trainSet, network);
        
        assertEquals( network.getNodeCount(), 105);
        assertEquals( network.getEdgeCount(), 104);
        
        List<Double> values = network.getDefaultNetworkTable().getColumn("Accuracy").getValues(Double.class);
        assertFalse( values.isEmpty() );
        assertFalse( values.get(0).isNaN() );
        
//        // Test SemiNaiveBayes
//        classif = new WrapperSemiNaiveBayes(dbc,true);
//        classif.train();
//        
//        nts = new NetworkTestSupport();
//        network = nts.getNetwork();
//        
//        ImportBDTask.createNetwork(classif, network);
//        
//        assertTrue( network.getNodeCount()>1);
//        assertTrue( network.getEdgeCount()>1);
    }
    
}