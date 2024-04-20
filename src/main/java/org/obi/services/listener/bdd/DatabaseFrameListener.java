/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.obi.services.listener.bdd;

import org.obi.services.model.DatabaseModel;



/**
 * DatabaseFrameListener class  
 *
 * @author r.hendrick
 */
public interface DatabaseFrameListener {


    // /////////////////////////////////////////////////////////////////////////
    //
    //
    // Container
    // 
    //
    // /////////////////////////////////////////////////////////////////////////
    void databaseFrameEventValidate(DatabaseModel model);
    void databaseFrameEventCancel(DatabaseModel model);

    // /////////////////////////////////////////////////////////////////////////
    //
    //
    // Getter / Setter
    // 
    //
    // /////////////////////////////////////////////////////////////////////////


}
