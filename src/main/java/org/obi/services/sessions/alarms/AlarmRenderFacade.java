package org.obi.services.sessions.alarms;

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
import org.obi.services.entities.alarms.AlarmRender;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class AlarmRenderFacade extends AbstractFacade<AlarmRender>{

    private static AlarmRenderFacade INSTANCE;

    public static AlarmRenderFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AlarmRenderFacade();
        }
        return INSTANCE;
    }

    public AlarmRenderFacade() {
    }

//    Connection conn = null;
//
//
//    protected Connection getConnectionMannager() {
//        if (conn == null) {
//            conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());
//        } else try {
//            if (conn.isClosed()) {
//                conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());
//            }
//        } catch (SQLException ex) {
//            Util.out("AlarmRenderFacade >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
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
    private List<AlarmRender> find(String findQuery) {
        String Q_find = findQuery;

        List<AlarmRender> lst = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = getConnectionMannager().createStatement();
            ResultSet rs = stmt.executeQuery(Q_find);
            while (rs.next()) {
                AlarmRender m = new AlarmRender();
                m.update(rs);
                lst.add(m);
            }
        } catch (SQLException ex) {
            Util.out("AlarmRenderFacade >> find on getConnectionManager() : " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Util.out("AlarmRenderFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lst;
    }

    /**
     * Find All elment in table
     *
     * Use request : "SELECT * FROM dbo.alarm_render"
     *
     * @return List of tags tabels
     */
    public List<AlarmRender> findAll() {
        String Q_findAll = "SELECT * FROM dbo.alarm_render";
        return find(Q_findAll);

    }

    /**
     * Find an element specified by id
     *
     * Use request : "SELECT * FROM dbo.alarm_render WHERE id = " + id
     *
     * @return tags tables or null if empty
     */
    public AlarmRender findById(int id) {
        String Q_finBy = "SELECT * FROM dbo.alarm_render WHERE id = " + id;
        List<AlarmRender> lst = find(Q_finBy);
        if (lst.isEmpty()) {
            return null;
        }
        return lst.get(0);
    }
}
