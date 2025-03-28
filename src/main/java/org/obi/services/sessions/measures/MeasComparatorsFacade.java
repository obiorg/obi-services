package org.obi.services.sessions.measures;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.Form.DatabaseFrame;
import org.obi.services.entities.measures.MeasComparators;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class MeasComparatorsFacade extends AbstractFacade<MeasComparators>{

    private static MeasComparatorsFacade INSTANCE;

    public static MeasComparatorsFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MeasComparatorsFacade();
        }
        return INSTANCE;
    }

    public MeasComparatorsFacade() {
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
//            Util.out(Util.errLine() + MeasComparatorsFacade.class.getSimpleName()
//                    + " >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
//            Logger.getLogger(MeasComparatorsFacade.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return conn;
//    }

    /**
     * Allow to initialize conenction or use it if not exist
     *
     * @return
     */
    public Boolean isConnectionOn() throws SQLException {
        if (conn == null) {
            getConnectionMannager();
        }
        return conn.isValid(10);
    }

    /**
     * General method to process a find process from an established query
     *
     * @param findQuery existing query in string format
     * @return list of result found
     */
    private List<MeasComparators> find(String findQuery, Boolean easy) {
        String Q_find = findQuery;

        List<MeasComparators> lst = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = getConnectionMannager().createStatement();
            ResultSet rs = stmt.executeQuery(Q_find);
            while (rs.next()) {
                MeasComparators m = new MeasComparators();
                m.update(rs, easy);
                lst.add(m);
            }
        } catch (SQLException ex) {
            Util.out("MeasComparatorsFacade >> find on getConnectionManager() : " + ex.getLocalizedMessage());
            Logger
                    .getLogger(AbstractFacade.class
                            .getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                };
            } catch (SQLException ex) {
                Util.out("MeasComparatorsFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger
                        .getLogger(MeasComparatorsFacade.class
                                .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lst;
    }

    /**
     * Find By Id - an element
     *
     * Using request : "SELECT * FROM dbo.meas_comparators WHERE id = " + id
     * allow to find specifc element
     *
     * @param id element to find definie by id
     * @return the element specifiy by id
     */
    public MeasComparators findById(int id) {
        String Q_finBy = "SELECT * FROM dbo.meas_comparators WHERE id = " + id;
        return find(Q_finBy, true).get(0);
    }

}
