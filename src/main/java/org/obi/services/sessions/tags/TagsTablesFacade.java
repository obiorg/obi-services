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
import org.obi.services.entities.tags.TagsTables;
import org.obi.services.model.DatabaseModel;
import org.obi.services.sessions.AbstractFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class TagsTablesFacade {

    private static TagsTablesFacade INSTANCE;

    public static TagsTablesFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagsTablesFacade();
        }
        return INSTANCE;
    }

    public TagsTablesFacade() {
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
            Util.out("TagsTablesFacade >> getConnectionMannager on DatabaseFrame.toConnection : " + ex.getLocalizedMessage());
            Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

    /**
     * General method to process a find process from an established query
     *
     * @param findQuery existing query in string format
     * @return list of result found
     */
    private List<TagsTables> find(String findQuery) {
        String Q_find = findQuery;

        List<TagsTables> lst = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = getConnectionMannager().createStatement();
            ResultSet rs = stmt.executeQuery(Q_find);
            while (rs.next()) {
                TagsTables m = new TagsTables();
                m.update(rs);
                lst.add(m);
            }
        } catch (SQLException ex) {
            Util.out("TagsTablesFacade >> find on getConnectionManager() : " + ex.getLocalizedMessage());
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Util.out("TagsTablesFacade >> find on close statement : " + ex.getLocalizedMessage());
                Logger.getLogger(TagsFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lst;
    }

    /**
     * Find All elment in table
     *
     * Use request : "SELECT * FROM dbo.tags_tables"
     *
     * @return List of tags tabels
     */
    public List<TagsTables> findAll() {
        String Q_findAll = "SELECT * FROM dbo.tags_tables";
        return find(Q_findAll);

    }

    /**
     * Find an element specified by id
     *
     * Use request : "SELECT * FROM dbo.tags_tables WHERE id = " + id
     *
     * @return tags tables or null if empty
     */
    public TagsTables findById(int id) {
        String Q_finBy = "SELECT * FROM dbo.tags_tables WHERE id = " + id;
        List<TagsTables> lst = find(Q_finBy);
        if (lst.isEmpty()) {
            return null;
        }
        return lst.get(0);
    }
}
