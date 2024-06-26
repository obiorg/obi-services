package org.obi.services.sessions.tags;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.Form.DatabaseFrame;
import org.obi.services.entities.tags.Tags;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public final class TagsFacade {

    public enum ValueType {
        BOOL("Boolean"),
        INT("Integer"),
        FLOAT("Float");

        public final String label;

        private ValueType(String label) {
            this.label = label;
        }

    }

    private static TagsFacade INSTANCE;

    public static TagsFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagsFacade();
        }
        return INSTANCE;
    }

    public TagsFacade() {
    }

    Connection conn = null;

    protected Connection getConnectionMannager() {
        if (conn == null) {
            conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());
        } else try {
            if (conn.isClosed()) {
                conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());
            }
        } catch (SQLException ex) {
            Util.out("TagsFacade >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
            Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

    private int updateOnValue(String updateQuery) {
        String Q_Update = updateQuery;
        try {
            Connection conn = getConnectionMannager();
            int updateCount = 0;
            if (conn != null) {
                PreparedStatement pstmt = conn.prepareStatement(Q_Update);
                if (pstmt != null) {
                    updateCount = pstmt.executeUpdate();
                } else {
                    Util.out("TagsFacade >> updateOnValue on getConnectionManager() >> null prepared statement !");
                }
//                conn.close();
            } else {
                Util.out("TagsFacade >> updateOnValue on getConnectionManager() >> conn is null !");
            }
            return updateCount;
        } catch (SQLException ex) {
            Util.out("TagsFacade >> updateOnValue on getConnectionManager() >> " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public int updateOnValueFloat(Tags tag) {
        String Q_Update = Tags.queryUpdateOn("vFloat",
                tag
        );
        return updateOnValue(Q_Update);
    }

    public int updateOnValueInt(Tags tag) {
        String Q_Update = Tags.queryUpdateOn("vInt",
                tag
        );
        return updateOnValue(Q_Update);
    }

    public int updateOnValueBool(Tags tag) {
        String Q_Update = Tags.queryUpdateOn("vBool",
                tag
        );
        return updateOnValue(Q_Update);
    }

    /**
     * Use if type of tag is not defined ans want to specify which value to
     * affected.
     *
     * @param tag tag containing value not taking care of tag type.
     * @return 0 if unknow type or, error, otherwise return number of row
     * affected
     */
    public int updateOnValue(Tags tag, ValueType type) {
        if (type.equals(ValueType.BOOL)) {
            return updateOnValueBool(tag);
        } else if (type.equals(ValueType.INT)) {
            return updateOnValueInt(tag);
        } else if (type.equals(ValueType.BOOL)) {
            return updateOnValueFloat(tag);
        }
        return 0;
    }

    /**
     * Use if type of tag is fully defined. Function will check type matchin,
     * Bool, Int, Real.
     *
     * @param tag tag containing value and fully type tag.
     * @return 0 if unknow type or, error, otherwise return number of row
     * affected
     */
    public int updateOnValue(Tags tag) {
        if (tag.getType().getId() == 1) { // 1 : Bool
            return updateOnValueBool(tag);
        } // INT
        else if (tag.getType().getId() == 4) { // 4 : Int
            return updateOnValueInt(tag);
        } // REAL
        else if (tag.getType().getId() == 6) { // 6 : Real
            return updateOnValueFloat(tag);
        }
        return 0;
    }

    /**
     * General method to process a find process from an established query
     *
     * @param findQuery existing query in string format
     * @return list of result found
     */
    private List<Tags> find(String findQuery, Boolean easy) {
        String Q_find = findQuery;

        List<Tags> lst = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = getConnectionMannager().createStatement();
            ResultSet rs = stmt.executeQuery(Q_find);
            while (rs.next()) {
                Tags m = new Tags();
                m.update(rs, easy);
                lst.add(m);
            }
        } catch (SQLException ex) {
            Util.out("TagsFacade >> find on getConnectionManager() : " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
                Util.out("TagsFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lst;
    }

    /**
     * Convenient method to find all content
     *
     * @return list of result find
     */
    public List<Tags> findAll() {
        String Q_findAll = "SELECT * FROM dbo.tags";
        return find(Q_findAll, false);
    }

    /**
     * Convenient method to find tags by specified machine
     *
     * @param machineId specied code to reduce amound of data
     * @return list of result find
     */
    public List<Tags> findByMachineId(int machineId) {
        String Q_finByMachine = "SELECT * FROM dbo.tags WHERE  machine = " + machineId;
        return find(Q_finByMachine, false);
    }

    /**
     * Convenient method to find tags by specified machine
     *
     * SELECT * FROM dbo.tags " + "WHERE company = " + companyId + " AND machine
     * = " + machineId
     *
     * @param machineId specied code to reduce amound of data
     * @return list of result find
     */
    public List<Tags> findByCompanyAndMachineId(int companyId, int machineId) {
        String Q_finByMachine = "SELECT * FROM dbo.tags "
                + "WHERE deleted = 0 AND company = " + companyId + " AND machine = " + machineId;
        return find(Q_finByMachine, false);
    }

    /**
     * Convenient method to find tags only by activated machine
     *
     * @param machine specied code to reduce amound of data
     * @return
     */
    public List<Tags> findActiveByCompanyAndMachine(int companyId, int machineId) {
        String Q_find = "SELECT * FROM dbo.tags "
                + "WHERE "
                + "deleted = 0 AND active = 1 AND company = " + companyId
                + " AND machine = " + machineId;
        return find(Q_find, false);
    }

    /**
     * Convenient method to find tags only by activated machine in simplify way
     *
     * @param machine specied code to reduce amound of data
     * @return
     */
    public List<Tags> _findActiveByCompanyAndMachine(int companyId, int machineId) {
        String Q_find = "SELECT * FROM dbo.tags "
                + "WHERE "
                + "deleted = 0 AND active = 1 AND company = " + companyId
                + " AND machine = " + machineId;
        return find(Q_find, true);
    }

}
