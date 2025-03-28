package org.obi.services.sessions.business;

import org.obi.services.sessions.tags.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.Form.DatabaseFrame;
import org.obi.services.entities.business.Entities;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class EntitiesFacade extends AbstractFacade<Entities>{

    private static EntitiesFacade INSTANCE;

    public static EntitiesFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EntitiesFacade();
        }
        return INSTANCE;
    }

    public EntitiesFacade() {
    }

//    Connection conn = null;

//    protected Connection getConnectionMannager() {
//        int requestTest = 0;
//        while (requestTest <= 2) {
//            if (conn == null) {
//                conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());
//            } else try {
//                if (conn.isClosed()) {
//                    conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());
//                }
//            } catch (SQLException ex) {
//                Util.out(Util.errLine() + EntitiesFacade.class.getSimpleName()
//                        + " >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
//                Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
//                conn = null;
//            }
//
//            if (conn == null) {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(EntitiesFacade.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }else{
//                requestTest = 2;
//            }
//            requestTest++;
//        }
//
//        return conn;
//    }

    /**
     * General method to process a find process from an established query
     *
     * @param findQuery existing query in string format
     * @return list of result found
     */
    private List<Entities> find(String findQuery) {
        String Q_find = findQuery;

        List<Entities> lst = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = getConnectionMannager().createStatement();
            ResultSet rs = stmt.executeQuery(Q_find);
            while (rs.next()) {
                Entities m = new Entities();
                m.update(rs);
                lst.add(m);
            }
        } catch (SQLException ex) {
            Util.out("EntitiesFacade >> find on getConnectionManager() : " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Util.out("EntitiesFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lst;
    }

    /**
     * Find All elment in table
     *
     * Use request : "SELECT * FROM dbo.entities"
     *
     * @return List of tags tabels
     */
    public List<Entities> findAll() {
        String Q_findAll = "SELECT * FROM dbo.entities";
        return find(Q_findAll);

    }

    /**
     * Find an element specified by id
     *
     * Use request : "SELECT * FROM dbo.entities WHERE id = " + id
     *
     * @return tags tables or null if empty
     */
    public Entities findById(int id) {
        String Q_finBy = "SELECT * FROM dbo.entities WHERE id = " + id;
        List<Entities> lst = find(Q_finBy);
        if (lst.isEmpty()) {
            return null;
        }
        return lst.get(0);
    }

    /**
     * Find an element specified by entity
     *
     * Use request : "SELECT * FROM dbo.entities WHERE entity = '" + entity +
     * "'"
     *
     * @return entity or null if empty
     */
    public Entities findByEntity(String entity) {
        String Q_finBy = "SELECT * FROM dbo.entities WHERE entity = '" + entity + "'";
        List<Entities> lst = find(Q_finBy);
        if (lst.isEmpty()) {
            return null;
        }
        return lst.get(0);
    }
}
