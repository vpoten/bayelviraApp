/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package elvira.inference.clustering.lazyid.test;

import java.io.*;

/**
 *
 * @author mgomez
 */
class ConcreteFileFilter implements FileFilter {
    private String extension;
    private String prefix;

    /**
     * Complete constructor
     * @param prefix
     * @param extension
     */
    public ConcreteFileFilter(String prefix, String extension ) {
        this.prefix=prefix;
        this.extension = extension;
    }
    
    public ConcreteFileFilter(String extension){
        this.extension=extension;
    }

    public boolean accept(java.io.File f) {
        boolean condition=false;
        String name=null;
        
        if (!f.isDirectory()) {
            name = f.getName();
            if (extension != null){
                condition=name.endsWith(extension);
            }
            if (prefix != null && condition != false){
               condition=name.startsWith(prefix);
            }
        }
        return condition;
    }
}

