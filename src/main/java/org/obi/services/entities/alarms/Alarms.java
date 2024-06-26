/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.obi.services.entities.alarms;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import org.obi.services.entities.business.Companies;
import org.obi.services.entities.tags.Tags;
import org.obi.services.entities.tags.TagsTables;
import org.obi.services.sessions.alarms.AlarmClassesFacade;
import org.obi.services.sessions.alarms.AlarmGroupsFacade;
import org.obi.services.sessions.business.CompaniesFacade;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
//@Entity
//@Table(catalog = "OBI", schema = "dbo")
//@XmlRootElement
//@NamedQueries({
//    @NamedQuery(name = "Alarms.findAll", query = "SELECT a FROM Alarms a"),
//    @NamedQuery(name = "Alarms.findById", query = "SELECT a FROM Alarms a WHERE a.id = :id"),
//    @NamedQuery(name = "Alarms.findByDeleted", query = "SELECT a FROM Alarms a WHERE a.deleted = :deleted"),
//    @NamedQuery(name = "Alarms.findByCreated", query = "SELECT a FROM Alarms a WHERE a.created = :created"),
//    @NamedQuery(name = "Alarms.findByChanged", query = "SELECT a FROM Alarms a WHERE a.changed = :changed"),
//    @NamedQuery(name = "Alarms.findByAlarm", query = "SELECT a FROM Alarms a WHERE a.alarm = :alarm"),
//    @NamedQuery(name = "Alarms.findByName", query = "SELECT a FROM Alarms a WHERE a.name = :name"),
//    @NamedQuery(name = "Alarms.findByDescirption", query = "SELECT a FROM Alarms a WHERE a.descirption = :descirption"),
//    @NamedQuery(name = "Alarms.findByLanguage", query = "SELECT a FROM Alarms a WHERE a.language = :language"),
//    @NamedQuery(name = "Alarms.findByComment", query = "SELECT a FROM Alarms a WHERE a.comment = :comment")})
public class Alarms implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private Boolean deleted;
    private Date created;
    private Date changed;
    private String alarm;
    private String name;
    private String descirption;
    private Integer language;
    private String comment;
    private AlarmClasses class1;
    private AlarmGroups group1;
    private Companies company;
    private Collection<Tags> tagsCollection;

    public Alarms() {
    }

    public Alarms(Integer id) {
        this.id = id;
    }

    public Alarms(Integer id, String alarm) {
        this.id = id;
        this.alarm = alarm;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getChanged() {
        return changed;
    }

    public void setChanged(Date changed) {
        this.changed = changed;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescirption() {
        return descirption;
    }

    public void setDescirption(String descirption) {
        this.descirption = descirption;
    }

    public Integer getLanguage() {
        return language;
    }

    public void setLanguage(Integer language) {
        this.language = language;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public AlarmClasses getClass1() {
        return class1;
    }

    public void setClass1(AlarmClasses class1) {
        this.class1 = class1;
    }

    public AlarmGroups getGroup1() {
        return group1;
    }

    public void setGroup1(AlarmGroups group1) {
        this.group1 = group1;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public Collection<Tags> getTagsCollection() {
        return tagsCollection;
    }

    public void setTagsCollection(Collection<Tags> tagsCollection) {
        this.tagsCollection = tagsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Alarms)) {
            return false;
        }
        Alarms other = (Alarms) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
//        return "org.obi.services.entities.Alarms[ id=" + id + " ]";
        return "" + this.alarm + " - " + this.name + "(" + this.language + ") [ id=" + id + " ]";
    }
    
    
    
    /**
     * Allow to affect result object
     *
     * @param rs a set of data of the row
     * @throws SQLException exception
     */
    public void update(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
            String c = rsMetaData.getColumnName(i);

            if (c.matches("id")) {
                this.id = rs.getInt(c);
            } else if (c.matches("deleted")) {
                this.deleted = rs.getBoolean(c);
            } else if (c.matches("created")) {
                this.created = rs.getDate(c);
            } else if (c.matches("changed")) {
                this.changed = rs.getDate(c);
            } else if (c.matches("company")) {
                CompaniesFacade facade = new CompaniesFacade();
                company = facade.findById(rs.getInt(c));
            } else if (c.matches("alarm")) {
                this.alarm = rs.getString(c);
            } else if (c.matches("name")) {
                this.name = rs.getString(c);
            } else if (c.matches("descirption")) {
                this.descirption = rs.getString(c);
            }
             else if (c.matches("group")) {
                AlarmGroupsFacade facade = new AlarmGroupsFacade();
                group1 = facade.findById(rs.getInt(c));
            }
              else if (c.matches("class")) {
                AlarmClassesFacade facade = new AlarmClassesFacade();
                class1 = facade.findById(rs.getInt(c));
            }
            
            else if (c.matches("language")) {
                this.language = rs.getInt(c);
            }
            /**
             *
             * informations
             */
            else if (c.matches("comment")) {
                this.comment = rs.getString(c);
            } else {
                Util.out(Alarms.class + " >> update >> unknown column name " + c);
                System.out.println(Alarms.class + " >> update >> unknown column name " + c);
            }

        }
    }
}
