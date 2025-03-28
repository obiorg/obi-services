package org.obi.services.sessions.tags;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.Form.DatabaseFrame;
import org.obi.services.entities.tags.TagsLists;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class TagsListsFacade extends AbstractFacade<TagsLists>{

    private static TagsListsFacade INSTANCE;

    public static TagsListsFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagsListsFacade();
        }
        return INSTANCE;
    }

    public TagsListsFacade() {
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
//            Util.out("TagsListsFacade >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
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

    /**
     * General method to process a find process from an established query
     *
     * @param findQuery existing query in string format
     * @return list of result found
     */
    private List<TagsLists> find(String findQuery, Boolean easy) {
        String Q_find = findQuery;

        List<TagsLists> lst = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = getConnectionMannager().createStatement();
            ResultSet rs = stmt.executeQuery(Q_find);
            while (rs.next()) {
                TagsLists m = new TagsLists();
                m.update(rs, easy);
                lst.add(m);
            }
        } catch (SQLException ex) {
            Util.out("TagsListsFacade >> find on getConnectionManager() : " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Util.out("TagsListsFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lst;
    }

    /**
     * Find All elment in table
     *
     * Use request : "SELECT * FROM dbo.tags_lists";
     *
     * @return List of tags tabels
     */
    public List<TagsLists> findAll() {
        String Q_findAll = "SELECT * FROM dbo.tags_lists";
        return find(Q_findAll, false);

    }

    /**
     * Find an element specified by id
     *
     * Use request : "SELECT * FROM dbo.tags_lists WHERE id = " + id;
     *
     * @return tags tables or null if empty
     */
    public TagsLists findById(int id) {
        String Q_finBy = "SELECT * FROM dbo.tags_lists WHERE id = " + id;
        List<TagsLists> lst = find(Q_finBy, false);
        if (lst.isEmpty()) {
            return null;
        }
        return lst.get(0);
    }
}
