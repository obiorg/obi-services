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
import org.obi.services.entities.machines.Machines;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.sessions.measures.MeasUnitsFacade;
import org.obi.services.sessions.tags.TagsFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class MachinesFacade extends AbstractFacade<Machines>{

    private static MachinesFacade INSTANCE;

    public static MachinesFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MachinesFacade();
        }
        return INSTANCE;
    }

    public MachinesFacade() {
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
//            Util.out("MachinesFacade >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
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
    private List<Machines> find(String findQuery) {
        String Q_find = findQuery;

        List<Machines> lst = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = getConnectionMannager().createStatement();
            ResultSet rs = stmt.executeQuery(Q_find);
            while (rs.next()) {
                Machines m = new Machines();
                m.update(rs);
                lst.add(m);
            }
        } catch (SQLException ex) {
            Util.out("MachineFacade >> find on getConnectionManager() : " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Util.out("MachineFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lst;
    }

    /**
     * Find All elment in table
     *
     * Use request : "SELECT * FROM dbo.machines WHERE deleted = 0"
     *
     * @return List of tags tabels
     */
    public List<Machines> findAll() {
        String Q_findAll = "SELECT * FROM dbo.machines WHERE deleted = 0";
        return find(Q_findAll);

    }

    /**
     * Find All elment in table by companyId
     *
     * Use request : "SELECT * FROM dbo.machines WHERE company = " + companyId
     *
     * @return List of tags table
     */
    public List<Machines> findByCompanyId(int companyId) {
        String Q_find = "SELECT * FROM dbo.machines "
                + "WHERE deleted = 0 and company = " + companyId;
        return find(Q_find);
    }

    /**
     * Find an element specified by id
     *
     * Use request : "SELECT * FROM dbo.tags_tables WHERE id = " + id
     *
     * @return tags tables or null if empty
     */
    public Machines findById(int id) {
        String Q_finBy = "SELECT * FROM dbo.machines WHERE id = " + id;
        List<Machines> lst = find(Q_finBy);
        if (lst.isEmpty()) {
            return null;
        }
        return lst.get(0);
    }

}
