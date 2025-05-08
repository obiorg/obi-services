/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.obi.services.entities.persistence;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.obi.services.entities.business.Companies;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import org.obi.services.entities.tags.Tags;
import org.obi.services.sessions.business.CompaniesFacade;
import org.obi.services.sessions.tags.TagsFacade;
import org.obi.services.util.DateUtil;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
//@Entity
//@Table(name = "pers_standard", catalog = "OBI", schema = "dbo")
//@XmlRootElement
//@NamedQueries({
//    @NamedQuery(name = "PersStandard.findAll", query = "SELECT p FROM PersStandard p"),
//    @NamedQuery(name = "PersStandard.findById", query = "SELECT p FROM PersStandard p WHERE p.id = :id"),
//    @NamedQuery(name = "PersStandard.findByDeleted", query = "SELECT p FROM PersStandard p WHERE p.deleted = :deleted"),
//    @NamedQuery(name = "PersStandard.findByCreated", query = "SELECT p FROM PersStandard p WHERE p.created = :created"),
//    @NamedQuery(name = "PersStandard.findByChanged", query = "SELECT p FROM PersStandard p WHERE p.changed = :changed"),
//    @NamedQuery(name = "PersStandard.findByVFloat", query = "SELECT p FROM PersStandard p WHERE p.vFloat = :vFloat"),
//    @NamedQuery(name = "PersStandard.findByVInt", query = "SELECT p FROM PersStandard p WHERE p.vInt = :vInt"),
//    @NamedQuery(name = "PersStandard.findByVBool", query = "SELECT p FROM PersStandard p WHERE p.vBool = :vBool"),
//    @NamedQuery(name = "PersStandard.findByVStr", query = "SELECT p FROM PersStandard p WHERE p.vStr = :vStr"),
//    @NamedQuery(name = "PersStandard.findByVDateTime", query = "SELECT p FROM PersStandard p WHERE p.vDateTime = :vDateTime"),
//    @NamedQuery(name = "PersStandard.findByVStamp", query = "SELECT p FROM PersStandard p WHERE p.vStamp = :vStamp"),
//    @NamedQuery(name = "PersStandard.findByStampStart", query = "SELECT p FROM PersStandard p WHERE p.stampStart = :stampStart"),
//    @NamedQuery(name = "PersStandard.findByStampEnd", query = "SELECT p FROM PersStandard p WHERE p.stampEnd = :stampEnd"),
//    @NamedQuery(name = "PersStandard.findByTbf", query = "SELECT p FROM PersStandard p WHERE p.tbf = :tbf"),
//    @NamedQuery(name = "PersStandard.findByTtr", query = "SELECT p FROM PersStandard p WHERE p.ttr = :ttr"),
//    @NamedQuery(name = "PersStandard.findByError", query = "SELECT p FROM PersStandard p WHERE p.error = :error"),
//    @NamedQuery(name = "PersStandard.findByErrorMsg", query = "SELECT p FROM PersStandard p WHERE p.errorMsg = :errorMsg")})
public class PersStandard implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private Boolean deleted;
    private Date created;
    private Date changed;
    private Double vFloat;
    private Integer vInt;
    private Boolean vBool;
    private String vStr;
    private LocalDateTime vDateTime;
    private LocalDateTime vStamp;
    private LocalDateTime stampStart;
    private LocalDateTime stampEnd;
    private Double tbf;
    private Double ttr;
    private Boolean error;
    private String errorMsg;
    private Companies company;
    private Tags tag;
    private Collection<PersStandardLimits> persStandardLimitsCollection;

    public PersStandard() {
    }

    public PersStandard(Integer id) {
        this.id = id;
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

    public Double getVFloat() {
        return vFloat;
    }

    public void setVFloat(Double vFloat) {
        this.vFloat = vFloat;
    }

    public Integer getVInt() {
        return vInt;
    }

    public void setVInt(Integer vInt) {
        this.vInt = vInt;
    }

    public Boolean getVBool() {
        return vBool;
    }

    public void setVBool(Boolean vBool) {
        this.vBool = vBool;
    }

    public String getVStr() {
        return vStr;
    }

    public void setVStr(String vStr) {
        this.vStr = vStr;
    }

    public LocalDateTime getVDateTime() {
        return vDateTime;
    }

    public void setVDateTime(LocalDateTime vDateTime) {
        this.vDateTime = vDateTime;
    }

    public LocalDateTime getVStamp() {
        return vStamp;
    }

    public void setVStamp(LocalDateTime vStamp) {
        this.vStamp = vStamp;
    }

    public LocalDateTime getStampStart() {
        return stampStart;
    }

    public void setStampStart(LocalDateTime stampStart) {
        this.stampStart = stampStart;
    }

    public LocalDateTime getStampEnd() {
        return stampEnd;
    }

    public void setStampEnd(LocalDateTime stampEnd) {
        this.stampEnd = stampEnd;
    }

    public Double getTbf() {
        return tbf;
    }

    public void setTbf(Double tbf) {
        this.tbf = tbf;
    }

    public Double getTtr() {
        return ttr;
    }

    public void setTtr(Double ttr) {
        this.ttr = ttr;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public Tags getTag() {
        return tag;
    }

    public void setTag(Tags tag) {
        this.tag = tag;
    }

    public Collection<PersStandardLimits> getPersStandardLimitsCollection() {
        return persStandardLimitsCollection;
    }

    public void setPersStandardLimitsCollection(Collection<PersStandardLimits> persStandardLimitsCollection) {
        this.persStandardLimitsCollection = persStandardLimitsCollection;
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
        if (!(object instanceof PersStandard)) {
            return false;
        }
        PersStandard other = (PersStandard) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return tag.getName() + " [" + vFloat + "; "
                + vInt + "; "
                + vBool + "; "
                + vStr + "; "
                + vDateTime + "; "
                + vStamp + "; "
                + " [" + id + "]";
    }

    /**
     * Allow to affect result object
     *
     * @param rs set of data
     * @param easy indicate no class is required
     * @throws SQLException
     */
    public void update(ResultSet rs, Boolean easy) throws SQLException {
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
                if (easy) {
                    company = new Companies(rs.getInt(c));
                } else {
                    CompaniesFacade cf = CompaniesFacade.getInstance();
                    company = cf.findById(rs.getInt(c));
                }
            } else if (c.matches("tag")) {
                if (easy) {
                    tag = new Tags(rs.getInt(c));
                } else {
                    TagsFacade ttf = TagsFacade.getInstance();
                    tag = ttf.findById(rs.getInt(c));
                }
            } /**
             *
             * VALUE
             */
            else if (c.matches("vFloat")) {
                this.vFloat = rs.getDouble(c);
            } else if (c.matches("vInt")) {
                this.vInt = rs.getInt(c);
            } else if (c.matches("vBool")) {
                this.vBool = rs.getBoolean(c);
            } else if (c.matches("vStr")) {
                this.vStr = rs.getString(c);
            } else if (c.matches("vDateTime")) {
                Timestamp timestamp = rs.getTimestamp(c);
                if (timestamp == null) {
                    timestamp = Timestamp.valueOf(LocalDateTime.now());
                }
                LocalDateTime dt = timestamp.toLocalDateTime();
                if (dt != null) {
                    this.vDateTime = dt;
                } else {
                    this.vDateTime = null;
                }
            } else if (c.matches("vStamp")) {
                Timestamp timestamp = rs.getTimestamp(c);
                if (timestamp == null) {
                    timestamp = Timestamp.valueOf(LocalDateTime.now());
                }
                LocalDateTime dt = timestamp.toLocalDateTime();
                if (dt != null) {
                    this.vStamp = dt;
                } else {
                    this.vStamp = null;
                }
            } /**
             *
             * Bit event
             */
            else if (c.matches("stampStart")) {
                LocalDateTime dt = DateUtil.toLocalDateTime(rs.getTimestamp(c));
                if (dt != null) {
                    this.stampStart = dt;
                } else {
                    this.stampStart = null;
                }
            } else if (c.matches("stampEnd")) {
                LocalDateTime dt = DateUtil.toLocalDateTime(rs.getTimestamp(c));
                if (dt != null) {
                    this.stampEnd = dt;
                } else {
                    this.stampEnd = null;
                }
            } else if (c.matches("tbf")) {
                this.tbf = rs.getDouble(c);
            } else if (c.matches("ttr")) {
                this.ttr = rs.getDouble(c);
            } else if (c.matches("error")) {
                this.error = rs.getBoolean(c);
            } else if (c.matches("errorMsg")) {
                this.errorMsg = rs.getString(c);
            } /**
             *
             * informations
             */
            else {
                Util.out(getClass().getSimpleName() + " >> update >> unknown column name " + c);
                System.out.println(getClass().getSimpleName() + " >> update >> unknown column name " + c);
            }

        }
    }

    public static class Serializer implements JsonSerializer<PersStandard> {

        @Override
        public JsonElement serialize(PersStandard src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("company", context.serialize(src.getCompany().getId()));
            jsonObject.add("tag", context.serialize(src.getTag().getId()));
            jsonObject.add("vFloat", context.serialize(src.getVFloat()));
            jsonObject.add("vInt", context.serialize(src.getVInt()));
            jsonObject.add("vBool", context.serialize(src.getVBool()));
            jsonObject.add("vStr", context.serialize(src.getVStr(), String.class));
            jsonObject.add("vDateTime", context.serialize(src.getVDateTime(), LocalDateTime.class));
            jsonObject.add("vStamp", context.serialize(src.getVStamp(), LocalDateTime.class));
            jsonObject.add("StampStart", context.serialize(src.getVStamp(), LocalDateTime.class));
            jsonObject.add("StampEnd", context.serialize(src.getVStamp(), LocalDateTime.class));
            jsonObject.add("tbf", context.serialize(0.0));
            jsonObject.add("ttr", context.serialize(0.0));
            jsonObject.add("error", context.serialize(src.getError()));
            jsonObject.add("errorMsg", context.serialize(src.getErrorMsg(), String.class));

            return jsonObject;
        }

    }

    public static class Deserializer implements JsonDeserializer<PersStandard> {

        @Override
        public PersStandard deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            PersStandard p = new PersStandard();
            
            p.setCompany(new Companies(ctx.deserialize(jsonObject.get("company"), Integer.class)));
            p.setTag(new Tags(ctx.deserialize(jsonObject.get("tag"), Integer.class)));
            p.setVFloat(ctx.deserialize(jsonObject.get("vFloat"), Double.class));
            p.setVInt(ctx.deserialize(jsonObject.get("vInt"), Integer.class));
            p.setVBool(ctx.deserialize(jsonObject.get("vBool"), Boolean.class));
            p.setVStr(ctx.deserialize(jsonObject.get("vStr"), String.class));
            p.setVDateTime(ctx.deserialize(jsonObject.get("vDateTime"), LocalDateTime.class));
            p.setVStamp(ctx.deserialize(jsonObject.get("vStamp"), LocalDateTime.class));
            p.setStampStart(ctx.deserialize(jsonObject.get("StampStart"), LocalDateTime.class));
            p.setStampEnd(ctx.deserialize(jsonObject.get("StampEnd"), LocalDateTime.class));
            p.setTbf(ctx.deserialize(jsonObject.get("tbf"), Double.class));
            p.setTtr(ctx.deserialize(jsonObject.get("ttr"), Double.class));
            p.setError(ctx.deserialize(jsonObject.get("error"), Boolean.class));
            p.setErrorMsg(ctx.deserialize(jsonObject.get("errorMsg"), String.class));
            
            return p;
        }
    }

}
