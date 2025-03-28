package org.obi.services.sessions.tags;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.Form.DatabaseFrame;
import org.obi.services.app.MachineConnection;
import org.obi.services.entities.business.Companies;
import org.obi.services.entities.tags.Tags;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.util.DateUtil;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public final class TagsFacade extends AbstractFacade<Tags>{

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
//            Util.out(Util.errLine() + TagsFacade.class.getSimpleName()
//                    + " >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
//            Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
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
                    Util.out(Util.errLine() + TagsFacade.class.getSimpleName()
                            + " >> updateOnValue on getConnectionManager() >> null prepared statement !"
                            + " Query = " + updateQuery);
                }
//                conn.close();
            } else {
                Util.out(Util.errLine() + TagsFacade.class.getSimpleName()
                        + " >> updateOnValue on getConnectionManager() >> conn is null !");
            }
            return updateCount;
        } catch (SQLException ex) {
            Util.out(Util.errLine() + TagsFacade.class.getSimpleName()
                    + " >> updateOnValue on getConnectionManager() >> " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public int updateOnValueFloat(Tags tag) {
        String Q_Update = Tags.queryUpdateOn("vFloat", tag);
        return updateOnValue(Q_Update);
    }

    public int updateOnValueInt(Tags tag) {
        String Q_Update = Tags.queryUpdateOn("vInt", tag);
        return updateOnValue(Q_Update);
    }

    public int updateOnValueBool(Tags tag) {
        String Q_Update = Tags.queryUpdateOn("vBool", tag);
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
     * Bool, Int, Real,
     * {@link MachineConnection#readValue(org.obi.services.entities.tags.Tags)}
     *
     * @param tag tag containing value and fully type tag.
     * @return 0 if unknow type or, error, otherwise return number of row
     * affected
     */
    public int updateOnValue(Tags tag) {
        int e = -1;
        switch (tag.getType().getId()) {
            case 1: // Bool
                e = updateOnValueBool(tag);
                break;
            case 20: // 2 : DateTime
                break;
            case 30: // DInt
                break;
            case 4:// 4 : Int
                e = updateOnValueInt(tag);
                break;
            case 50: // 5 : LReal
                break;
            case 6: // 6 : Real
                e = updateOnValueFloat(tag);
                break;
            case 70: // 7 : SInt
                break;
            case 80: // 8 : UDInt
                break;
            case 9: // 9 : UInt
                // Integrate as Int
                e = updateOnValueInt(tag);
                break;
            case 100: //10: USInt
                break;
            case 110: //11: Wide String
                break;
            default:
                Util.out(Util.errLine() + TagsFacade.class.getSimpleName()
                        + " : " + tag.getType().getId() + " is not implemented !");
                break;
        }
        return e;
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
                Util.out("TagsFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger
                        .getLogger(TagsFacade.class
                                .getName()).log(Level.SEVERE, null, ex);
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
     * Find By Id - an element
     *
     * Using request : "SELECT * FROM dbo.Tags WHERE id = " + id allow to find
     * specifc element
     *
     * @param id element to find definie by id
     * @return the element specifiy by id
     */
    public Tags findById(int id) {
        String Q_finBy = "SELECT * FROM dbo.Tags WHERE id = " + id;
        return find(Q_finBy, true).get(0);
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

    /**
     * This method allow to update many tags on main parameter : "UPDATE
     * dbo.tags SET [vFloat] = ? ,[vInt] = ? ,[vBool] = ? ,[vStr] = ?
     * ,[vDateTime] = ? ,[vStamp] = ? WHERE id = ? ";
     *
     * @param tagsUpdating list of tags to update
     */
    public boolean updateValue(List<Tags> tagsUpdating) throws SQLException, IllegalArgumentException {
        // Will request prepare statement and push all at once
        String queryUpdate = "UPDATE dbo.tags SET"
                + " [vFloat] = ? "
                + " ,[vInt] = ? "
                + " ,[vBool] = ? "
                + " ,[vStr] = ? "
                + " ,[vDateTime] = ? "
                + " ,[vStamp] = ? "
                + "WHERE id = ? ";

        PreparedStatement ps = getConnectionMannager().prepareStatement(queryUpdate);
        for (Tags tag : tagsUpdating) {
            if (tag != null) {
//                Util.out("TagsFacadeThread >> updateValue : " + tag.toStringFull());
                ps.setDouble(1, tag.getVFloat());
                ps.setInt(2, tag.getVInt());
                ps.setBoolean(3, tag.getVBool());
                ps.setString(4, tag.getVStr());
                ps.setTimestamp(5, Timestamp.valueOf(tag.getVDateTime()));
                ps.setTimestamp(6, Timestamp.valueOf(tag.getVStamp()));
                ps.setInt(7, tag.getId());
                ps.addBatch();
            }
        }
        int[] result = ps.executeBatch();
        return result.length >= 0;
    }
}
