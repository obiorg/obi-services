package org.obi.services.sessions.machines;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.Form.DatabaseFrame;
import org.obi.services.entities.machines.MachDrivers;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.sessions.tags.TagsFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class MachDriversFacade extends AbstractFacade<MachDrivers>{

    private static MachDriversFacade INSTANCE;

    public static MachDriversFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MachDriversFacade();
        }
        return INSTANCE;
    }

    public MachDriversFacade() {
    }

//    Connection conn = null;
//
//    protected Connection getConnectionMannager() {
//        if (conn == null) {
//            conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());
//        } else try {
//            if (conn.isClosed()) {
//                conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());
//            }
//        } catch (SQLException ex) {
//            Util.out("MachDriversFacade >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
//            Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return conn;
//    }

    /**
     * General method to process a find process from an established query
     *
     * @param findQuery existing query in string format
     * @return list of result found
     */
    private List<MachDrivers> find(String findQuery) {
        String Q_find = findQuery;

        List<MachDrivers> lst = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = getConnectionMannager().createStatement();
            ResultSet rs = stmt.executeQuery(Q_find);
            while (rs.next()) {
                MachDrivers m = new MachDrivers();
                m.update(rs);
                lst.add(m);
            }
        } catch (SQLException ex) {
            Util.out("MachDriversFacade >> find on getConnectionManager() : " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Util.out("MachDriversFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lst;
    }

    /**
     * Find All - Element
     *
     * Using request : "SELECT * FROM dbo.mach_drivers" to find all element
     *
     * @return all element without filter
     */
    public List<MachDrivers> findAll() {
        String Q_findAll = "SELECT * FROM dbo.mach_drivers";
        return find(Q_findAll);

    }

    /**
     * Find By Id - an element
     *
     * Using request : "SELECT * FROM dbo.mach_drivers WHERE id = " + id allow
     * to find specifc element
     *
     * @param id element to find definie by id
     * @return the element specifiy by id
     */
    public MachDrivers findById(int id) {
        String Q_finBy = "SELECT * FROM dbo.mach_drivers WHERE id = " + id;
        return find(Q_finBy).get(0);
    }
}
