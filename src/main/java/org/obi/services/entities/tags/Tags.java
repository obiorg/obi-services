/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.obi.services.entities.tags;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.obi.services.entities.persistence.PersStandardLimits;
import org.obi.services.entities.persistence.Persistence;
import org.obi.services.entities.persistence.PersStandard;
import org.obi.services.entities.measures.MeasUnits;
import org.obi.services.entities.measures.MeasLimits;
import org.obi.services.entities.machines.Machines;
import org.obi.services.entities.business.Companies;
import org.obi.services.entities.analyses.AnalyseAllowed;
import org.obi.services.entities.alarms.Alarms;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import org.obi.services.sessions.alarms.AlarmsFacade;
import org.obi.services.sessions.business.CompaniesFacade;
import org.obi.services.sessions.machines.MachinesFacade;
import org.obi.services.sessions.measures.MeasUnitsFacade;
import org.obi.services.sessions.tags.TagsListsFacade;
import org.obi.services.sessions.tags.TagsMemoriesFacade;
import org.obi.services.sessions.tags.TagsTablesFacade;
import org.obi.services.sessions.tags.TagsTypesFacade;
import org.obi.services.util.DateUtil;
import org.obi.services.util.Settings;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
//@Entity
//@Table(catalog = "OBI", schema = "dbo")
//@XmlRootElement
//@NamedQueries({
//    @NamedQuery(name = "Tags.findAll", query = "SELECT t FROM Tags t"),
//    @NamedQuery(name = "Tags.findById", query = "SELECT t FROM Tags t WHERE t.id = :id"),
//    @NamedQuery(name = "Tags.findByDeleted", query = "SELECT t FROM Tags t WHERE t.deleted = :deleted"),
//    @NamedQuery(name = "Tags.findByCreated", query = "SELECT t FROM Tags t WHERE t.created = :created"),
//    @NamedQuery(name = "Tags.findByChanged", query = "SELECT t FROM Tags t WHERE t.changed = :changed"),
//    @NamedQuery(name = "Tags.findByName", query = "SELECT t FROM Tags t WHERE t.name = :name"),
//    @NamedQuery(name = "Tags.findByDb", query = "SELECT t FROM Tags t WHERE t.db = :db"),
//    @NamedQuery(name = "Tags.findByByte1", query = "SELECT t FROM Tags t WHERE t.byte1 = :byte1"),
//    @NamedQuery(name = "Tags.findByBit", query = "SELECT t FROM Tags t WHERE t.bit = :bit"),
//    @NamedQuery(name = "Tags.findByActive", query = "SELECT t FROM Tags t WHERE t.active = :active"),
//    @NamedQuery(name = "Tags.findByCycle", query = "SELECT t FROM Tags t WHERE t.cycle = :cycle"),
//    @NamedQuery(name = "Tags.findByDelta", query = "SELECT t FROM Tags t WHERE t.delta = :delta"),
//    @NamedQuery(name = "Tags.findByDeltaFloat", query = "SELECT t FROM Tags t WHERE t.deltaFloat = :deltaFloat"),
//    @NamedQuery(name = "Tags.findByDeltaInt", query = "SELECT t FROM Tags t WHERE t.deltaInt = :deltaInt"),
//    @NamedQuery(name = "Tags.findByDeltaBool", query = "SELECT t FROM Tags t WHERE t.deltaBool = :deltaBool"),
//    @NamedQuery(name = "Tags.findByDeltaDateTime", query = "SELECT t FROM Tags t WHERE t.deltaDateTime = :deltaDateTime"),
//    @NamedQuery(name = "Tags.findByVFloat", query = "SELECT t FROM Tags t WHERE t.vFloat = :vFloat"),
//    @NamedQuery(name = "Tags.findByVInt", query = "SELECT t FROM Tags t WHERE t.vInt = :vInt"),
//    @NamedQuery(name = "Tags.findByVBool", query = "SELECT t FROM Tags t WHERE t.vBool = :vBool"),
//    @NamedQuery(name = "Tags.findByVStr", query = "SELECT t FROM Tags t WHERE t.vStr = :vStr"),
//    @NamedQuery(name = "Tags.findByVDateTime", query = "SELECT t FROM Tags t WHERE t.vDateTime = :vDateTime"),
//    @NamedQuery(name = "Tags.findByVStamp", query = "SELECT t FROM Tags t WHERE t.vStamp = :vStamp"),
//    @NamedQuery(name = "Tags.findByVDefault", query = "SELECT t FROM Tags t WHERE t.vDefault = :vDefault"),
//    @NamedQuery(name = "Tags.findByVFloatDefault", query = "SELECT t FROM Tags t WHERE t.vFloatDefault = :vFloatDefault"),
//    @NamedQuery(name = "Tags.findByVIntDefault", query = "SELECT t FROM Tags t WHERE t.vIntDefault = :vIntDefault"),
//    @NamedQuery(name = "Tags.findByVBoolDefault", query = "SELECT t FROM Tags t WHERE t.vBoolDefault = :vBoolDefault"),
//    @NamedQuery(name = "Tags.findByVStrDefault", query = "SELECT t FROM Tags t WHERE t.vStrDefault = :vStrDefault"),
//    @NamedQuery(name = "Tags.findByVDateTimeDefault", query = "SELECT t FROM Tags t WHERE t.vDateTimeDefault = :vDateTimeDefault"),
//    @NamedQuery(name = "Tags.findByVStampDefault", query = "SELECT t FROM Tags t WHERE t.vStampDefault = :vStampDefault"),
//    @NamedQuery(name = "Tags.findByCounter", query = "SELECT t FROM Tags t WHERE t.counter = :counter"),
//    @NamedQuery(name = "Tags.findByCounterType", query = "SELECT t FROM Tags t WHERE t.counterType = :counterType"),
//    @NamedQuery(name = "Tags.findByMesure", query = "SELECT t FROM Tags t WHERE t.mesure = :mesure"),
//    @NamedQuery(name = "Tags.findByMesureMin", query = "SELECT t FROM Tags t WHERE t.mesureMin = :mesureMin"),
//    @NamedQuery(name = "Tags.findByMesureMax", query = "SELECT t FROM Tags t WHERE t.mesureMax = :mesureMax"),
//    @NamedQuery(name = "Tags.findByMqqtTopic", query = "SELECT t FROM Tags t WHERE t.mqttTopic = :mqttTopic"),
//    @NamedQuery(name = "Tags.findByWebhook", query = "SELECT t FROM Tags t WHERE t.webhook = :webhook"),
//    @NamedQuery(name = "Tags.findByLaboratory", query = "SELECT t FROM Tags t WHERE t.laboratory = :laboratory"),
//    @NamedQuery(name = "Tags.findByFormula", query = "SELECT t FROM Tags t WHERE t.formula = :formula"),
//    @NamedQuery(name = "Tags.findByFormCalculus", query = "SELECT t FROM Tags t WHERE t.formCalculus = :formCalculus"),
//    @NamedQuery(name = "Tags.findByFormProcessing", query = "SELECT t FROM Tags t WHERE t.formProcessing = :formProcessing"),
//    @NamedQuery(name = "Tags.findByError", query = "SELECT t FROM Tags t WHERE t.error = :error"),
//    @NamedQuery(name = "Tags.findByErrorMsg", query = "SELECT t FROM Tags t WHERE t.errorMsg = :errorMsg"),
//    @NamedQuery(name = "Tags.findByErrorStamp", query = "SELECT t FROM Tags t WHERE t.errorStamp = :errorStamp"),
//    @NamedQuery(name = "Tags.findByAlarmEnable", query = "SELECT t FROM Tags t WHERE t.alarmEnable = :alarmEnable"),
//    @NamedQuery(name = "Tags.findByPersistenceEnable", query = "SELECT t FROM Tags t WHERE t.persistenceEnable = :persistenceEnable"),
//    @NamedQuery(name = "Tags.findByPersOffsetEnable", query = "SELECT t FROM Tags t WHERE t.persOffsetEnable = :persOffsetEnable"),
//    @NamedQuery(name = "Tags.findByPersOffsetFloat", query = "SELECT t FROM Tags t WHERE t.persOffsetFloat = :persOffsetFloat"),
//    @NamedQuery(name = "Tags.findByPersOffsetInt", query = "SELECT t FROM Tags t WHERE t.persOffsetInt = :persOffsetInt"),
//    @NamedQuery(name = "Tags.findByPersOffsetBool", query = "SELECT t FROM Tags t WHERE t.persOffsetBool = :persOffsetBool"),
//    @NamedQuery(name = "Tags.findByPersOffsetDateTime", query = "SELECT t FROM Tags t WHERE t.persOffsetDateTime = :persOffsetDateTime"),
//    @NamedQuery(name = "Tags.findByComment", query = "SELECT t FROM Tags t WHERE t.comment = :comment")})
public class Tags implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Boolean deleted;
    private Date created;
    private Date changed;
    private String name;
    private Integer db;
    private Integer byte1;
    private Integer bit;
    private Boolean active;
    private Integer cycle;
    private Boolean delta;
    private Double deltaFloat;
    private Integer deltaInt;
    private Integer deltaBool;
    private BigInteger deltaDateTime;
    private Double vFloat;
    private Integer vInt;
    private Boolean vBool;
    private String vStr;
    private LocalDateTime vDateTime;
    private LocalDateTime vStamp;
    private Boolean vDefault;
    private Double vFloatDefault;
    private Integer vIntDefault;
    private Boolean vBoolDefault;
    private String vStrDefault;
    private Date vDateTimeDefault;
    private Date vStampDefault;
    private Boolean counter;
    private Integer counterType;
    private Boolean mesure;
    private Double mesureMin;
    private Double mesureMax;
    private String mqttTopic;
    private String webhook;
    private Boolean laboratory;
    private Boolean formula;
    private String formCalculus;
    private Integer formProcessing;
    private Boolean error;
    private String errorMsg;
    private Date errorStamp;
    private Boolean alarmEnable;
    private Boolean persistenceEnable;
    private Boolean persOffsetEnable;
    private Double persOffsetFloat;
    private Integer persOffsetInt;
    private Boolean persOffsetBool;
    private Date persOffsetDateTime;
    private String comment;
    private Collection<AnalyseAllowed> analyseAllowedCollection;
    private Collection<PersStandard> persStandardCollection;
    private Collection<MeasLimits> measLimitsCollection;
    private Collection<PersStandardLimits> persStandardLimitsCollection;
    private Alarms alarm;
    private Companies company;
    private Machines machine;
    private MeasUnits measureUnit;
    private TagsMemories memory;
    private TagsTables table;
    private TagsTypes type;
    private Collection<Persistence> persistenceCollection;
    private TagsLists list;

    public Tags() {
    }

    public Tags(Integer id) {
        this.id = id;
    }

    public Tags(Integer id, String name) {
        this.id = id;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDb() {
        return db;
    }

    public void setDb(Integer db) {
        this.db = db;
    }

    public Integer getByte1() {
        return byte1;
    }

    public void setByte1(Integer byte1) {
        this.byte1 = byte1;
    }

    public Integer getBit() {
        return bit;
    }

    public void setBit(Integer bit) {
        this.bit = bit;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getCycle() {
        return cycle;
    }

    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

    public Boolean getDelta() {
        return delta;
    }

    public void setDelta(Boolean delta) {
        this.delta = delta;
    }

    public Double getDeltaFloat() {
        return deltaFloat;
    }

    public void setDeltaFloat(Double deltaFloat) {
        this.deltaFloat = deltaFloat;
    }

    public Integer getDeltaInt() {
        return deltaInt;
    }

    public void setDeltaInt(Integer deltaInt) {
        this.deltaInt = deltaInt;
    }

    public Integer getDeltaBool() {
        return deltaBool;
    }

    public void setDeltaBool(Integer deltaBool) {
        this.deltaBool = deltaBool;
    }

    public BigInteger getDeltaDateTime() {
        return deltaDateTime;
    }

    public void setDeltaDateTime(BigInteger deltaDateTime) {
        this.deltaDateTime = deltaDateTime;
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

    public Boolean getVDefault() {
        return vDefault;
    }

    public void setVDefault(Boolean vDefault) {
        this.vDefault = vDefault;
    }

    public Double getVFloatDefault() {
        return vFloatDefault;
    }

    public void setVFloatDefault(Double vFloatDefault) {
        this.vFloatDefault = vFloatDefault;
    }

    public Integer getVIntDefault() {
        return vIntDefault;
    }

    public void setVIntDefault(Integer vIntDefault) {
        this.vIntDefault = vIntDefault;
    }

    public Boolean getVBoolDefault() {
        return vBoolDefault;
    }

    public void setVBoolDefault(Boolean vBoolDefault) {
        this.vBoolDefault = vBoolDefault;
    }

    public String getVStrDefault() {
        return vStrDefault;
    }

    public void setVStrDefault(String vStrDefault) {
        this.vStrDefault = vStrDefault;
    }

    public Date getVDateTimeDefault() {
        return vDateTimeDefault;
    }

    public void setVDateTimeDefault(Date vDateTimeDefault) {
        this.vDateTimeDefault = vDateTimeDefault;
    }

    public Date getVStampDefault() {
        return vStampDefault;
    }

    public void setVStampDefault(Date vStampDefault) {
        this.vStampDefault = vStampDefault;
    }

    public Boolean getCounter() {
        return counter;
    }

    public void setCounter(Boolean counter) {
        this.counter = counter;
    }

    public Integer getCounterType() {
        return counterType;
    }

    public void setCounterType(Integer counterType) {
        this.counterType = counterType;
    }

    public Boolean getMesure() {
        return mesure;
    }

    public void setMesure(Boolean mesure) {
        this.mesure = mesure;
    }

    public Double getMesureMin() {
        return mesureMin;
    }

    public void setMesureMin(Double mesureMin) {
        this.mesureMin = mesureMin;
    }

    public Double getMesureMax() {
        return mesureMax;
    }

    public void setMesureMax(Double mesureMax) {
        this.mesureMax = mesureMax;
    }

    public String getMqttTopic() {
        return mqttTopic;
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public Boolean getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(Boolean laboratory) {
        this.laboratory = laboratory;
    }

    public Boolean getFormula() {
        return formula;
    }

    public void setFormula(Boolean formula) {
        this.formula = formula;
    }

    public String getFormCalculus() {
        return formCalculus;
    }

    public void setFormCalculus(String formCalculus) {
        this.formCalculus = formCalculus;
    }

    public Integer getFormProcessing() {
        return formProcessing;
    }

    public void setFormProcessing(Integer formProcessing) {
        this.formProcessing = formProcessing;
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

    public Date getErrorStamp() {
        return errorStamp;
    }

    public void setErrorStamp(Date errorStamp) {
        this.errorStamp = errorStamp;
    }

    public Boolean getAlarmEnable() {
        return alarmEnable;
    }

    public void setAlarmEnable(Boolean alarmEnable) {
        this.alarmEnable = alarmEnable;
    }

    public Boolean getPersistenceEnable() {
        return persistenceEnable;
    }

    public void setPersistenceEnable(Boolean persistenceEnable) {
        this.persistenceEnable = persistenceEnable;
    }

    public Boolean getPersOffsetEnable() {
        return persOffsetEnable;
    }

    public void setPersOffsetEnable(Boolean persOffsetEnable) {
        this.persOffsetEnable = persOffsetEnable;
    }

    public Double getPersOffsetFloat() {
        return persOffsetFloat;
    }

    public void setPersOffsetFloat(Double persOffsetFloat) {
        this.persOffsetFloat = persOffsetFloat;
    }

    public Integer getPersOffsetInt() {
        return persOffsetInt;
    }

    public void setPersOffsetInt(Integer persOffsetInt) {
        this.persOffsetInt = persOffsetInt;
    }

    public Boolean getPersOffsetBool() {
        return persOffsetBool;
    }

    public void setPersOffsetBool(Boolean persOffsetBool) {
        this.persOffsetBool = persOffsetBool;
    }

    public Date getPersOffsetDateTime() {
        return persOffsetDateTime;
    }

    public void setPersOffsetDateTime(Date persOffsetDateTime) {
        this.persOffsetDateTime = persOffsetDateTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Collection<AnalyseAllowed> getAnalyseAllowedCollection() {
        return analyseAllowedCollection;
    }

    public void setAnalyseAllowedCollection(Collection<AnalyseAllowed> analyseAllowedCollection) {
        this.analyseAllowedCollection = analyseAllowedCollection;
    }

    public Collection<PersStandard> getPersStandardCollection() {
        return persStandardCollection;
    }

    public void setPersStandardCollection(Collection<PersStandard> persStandardCollection) {
        this.persStandardCollection = persStandardCollection;
    }

    public Collection<MeasLimits> getMeasLimitsCollection() {
        return measLimitsCollection;
    }

    public void setMeasLimitsCollection(Collection<MeasLimits> measLimitsCollection) {
        this.measLimitsCollection = measLimitsCollection;
    }

    public Collection<PersStandardLimits> getPersStandardLimitsCollection() {
        return persStandardLimitsCollection;
    }

    public void setPersStandardLimitsCollection(Collection<PersStandardLimits> persStandardLimitsCollection) {
        this.persStandardLimitsCollection = persStandardLimitsCollection;
    }

    public Alarms getAlarm() {
        return alarm;
    }

    public void setAlarm(Alarms alarm) {
        this.alarm = alarm;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public Machines getMachine() {
        return machine;
    }

    public void setMachine(Machines machine) {
        this.machine = machine;
    }

    public MeasUnits getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(MeasUnits measureUnit) {
        this.measureUnit = measureUnit;
    }

    public TagsMemories getMemory() {
        return memory;
    }

    public void setMemory(TagsMemories memory) {
        this.memory = memory;
    }

    public TagsTables getTable() {
        return table;
    }

    public void setTable(TagsTables table) {
        this.table = table;
    }

    public TagsTypes getType() {
        return type;
    }

    public void setType(TagsTypes type) {
        this.type = type;
    }

    public TagsLists getList() {
        return list;
    }

    public void setList(TagsLists list) {
        this.list = list;
    }

    public Collection<Persistence> getPersistenceCollection() {
        return persistenceCollection;
    }

    public void setPersistenceCollection(Collection<Persistence> persistenceCollection) {
        this.persistenceCollection = persistenceCollection;
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
        if (!(object instanceof Tags)) {
            return false;
        }
        Tags other = (Tags) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
//        return "org.obi.services.entities.Tags[ id=" + id + " ]";
        return "" + this.name + " - " + this.getMachine().getName() + " [ id=" + id + " ]";
    }

    public String toStringFull() {
//        return "org.obi.services.entities.Tags[ id=" + id + " ]";
        return "" + this.name + " - " + this.getMachine().getName() + " [ id=" + id + " ] "
                + " " + this.table + " "
                + " " + this.machine + " "
                + " " + this.type + " "
                + " " + this.memory + " "
                + " " + this.vFloat + " "
                + " " + this.vInt + " ";
    }

    /**
     * Allow to affect result object
     *
     * @param rs
     * @param easy indicate no class is required
     * @throws SQLException
     */
    public void update(ResultSet rs, Boolean easy) throws SQLException {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int count = rsMetaData.getColumnCount();
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
            } else if (c.matches("table")) {
                if (easy) {
                    table = new TagsTables(rs.getInt(c));
                } else {
                    TagsTablesFacade ttf = TagsTablesFacade.getInstance();
                    table = ttf.findById(rs.getInt(c));
                }
            } /**
             *
             * PARAMETER
             */
            else if (c.matches("name")) {
                this.name = rs.getString(c);
            } else if (c.matches("machine")) {
                if (easy) {
                    machine = new Machines(rs.getInt(c));
                } else {
                    MachinesFacade mf = MachinesFacade.getInstance();
                    machine = mf.findById(rs.getInt(c));
                }
            } else if (c.matches("type")) {
                if (easy) {
                    type = new TagsTypes(rs.getInt(c));
                } else {
                    TagsTypesFacade ttf = TagsTypesFacade.getInstance();
                    type = ttf.findById(rs.getInt(c));
                }
            } else if (c.matches("memory")) {
                if (easy) {
                    memory = new TagsMemories(rs.getInt(c));
                } else {
                    TagsMemoriesFacade tmf = TagsMemoriesFacade.getInstance();
                    memory = tmf.findById(rs.getInt(c));
                }
            } else if (c.matches("db")) {
                this.db = rs.getInt(c);
            } else if (c.matches("byte")) {
                this.byte1 = rs.getInt(c);
            } else if (c.matches("bit")) {
                this.bit = rs.getInt(c);
            } /**
             *
             * ACTIVATION CYCLE
             */
            else if (c.matches("active")) {
                this.active = rs.getBoolean(c);
            } else if (c.matches("cycle")) {
                this.cycle = rs.getInt(c);
            } /**
             *
             * DELTA
             */
            else if (c.matches("delta")) {
                this.delta = rs.getBoolean(c);
            } else if (c.matches("deltaFloat")) {
                this.deltaFloat = rs.getDouble(c);
            } else if (c.matches("deltaInt")) {
                this.deltaInt = rs.getInt(c);
            } else if (c.matches("deltaBool")) {
                this.deltaBool = rs.getInt(c);
            } else if (c.matches("deltaDateTime")) {
                this.deltaDateTime = BigInteger.valueOf(rs.getInt(c));
            } /**
             *
             * VALUE
             */
            else if (c.matches("vFloat")) {
                this.vFloat = rs.getDouble(c);
            } else if (c.matches("vInt")) {
                this.vInt = rs.getInt(c);
            } else if (c.matches("list")) {
                if (easy) {
                    list = new TagsLists(rs.getInt(c));
                } else {
                    TagsListsFacade ttf = TagsListsFacade.getInstance();
                    list = ttf.findById(rs.getInt(c));
                }
            } else if (c.matches("vBool")) {
                this.vBool = rs.getBoolean(c);
            } else if (c.matches("vStr")) {
                this.vStr = rs.getString(c);
            } else if (c.matches("vDateTime")) {
                LocalDateTime dt = DateUtil.toLocalDateTime(rs.getTimestamp(c));
                if (dt != null) {
                    this.vDateTime = dt;
                } else {
                    this.vDateTime = null;
                }
            } else if (c.matches("vStamp")) {
                LocalDateTime dt = DateUtil.toLocalDateTime(rs.getTimestamp(c));
                if (dt != null) {
                    this.vStamp = dt;
                } else {
                    this.vStamp = null;
                }
            } /**
             *
             * DEFAULT VALUE
             */
            else if (c.matches("vDefault")) {
                this.vDefault = rs.getBoolean(c);
            } else if (c.matches("vFloatDefault")) {
                this.vFloatDefault = rs.getDouble(c);
            } else if (c.matches("vIntDefault")) {
                this.vIntDefault = rs.getInt(c);
            } else if (c.matches("vBoolDefault")) {
                this.vBoolDefault = rs.getBoolean(c);
            } else if (c.matches("vStrDefault")) {
                this.vStrDefault = rs.getString(c);
            } else if (c.matches("vDateTimeDefault")) {
                Timestamp timestamp = rs.getTimestamp(c);
                if (timestamp != null) {
                    this.vDateTimeDefault = new java.util.Date(timestamp.getTime());
                } else {
                    this.vDateTimeDefault = null;
                }
            } else if (c.matches("vStampDefault")) {
                Timestamp timestamp = rs.getTimestamp(c);
                if (timestamp != null) {
                    this.vStampDefault = new java.util.Date(timestamp.getTime());
                } else {
                    this.vStampDefault = null;
                }
            } /**
             * Counter
             */
            else if (c.matches("counter")) {
                this.counter = rs.getBoolean(c);
            } else if (c.matches("counterType")) {
                this.counterType = rs.getInt(c);
            } /**
             * Mesures
             */
            else if (c.matches("mesure")) {
                this.mesure = rs.getBoolean(c);
            } else if (c.matches("mesureMin")) {
                this.mesureMin = rs.getDouble(c);
            } else if (c.matches("mesureMax")) {
                this.mesureMax = rs.getDouble(c);
            } else if (c.matches("measureUnit")) {
                if (easy) {
                    measureUnit = new MeasUnits(rs.getInt(c));
                } else {
                    MeasUnitsFacade muf = new MeasUnitsFacade();
                    measureUnit = muf.findById(rs.getInt(c));
                }
            }/**
             *
             * LINKS
             */
            else if (c.matches("mqtt_topic")) {
                this.mqttTopic = rs.getString(c);
            } else if (c.matches("webhook")) {
                this.webhook = rs.getString(c);
            } else if (c.matches("laboratory")) {
                this.laboratory = rs.getBoolean(c);
            } else if (c.matches("formula")) {
                this.formula = rs.getBoolean(c);
            } else if (c.matches("formCalculus")) {
                this.formCalculus = rs.getString(c);
            } else if (c.matches("formProcessing")) {
                this.formProcessing = rs.getInt(c);
            }/**
             *
             * ERRORS
             */
            else if (c.matches("error")) {
                this.error = rs.getBoolean(c);
            } else if (c.matches("errorMsg")) {
                this.errorMsg = rs.getString(c);
            } else if (c.matches("errorStamp")) {
                Timestamp timestamp = rs.getTimestamp(c);
                if (timestamp != null) {
                    this.errorStamp = new java.util.Date(timestamp.getTime());
                } else {
                    this.errorStamp = null;
                }
            }/**
             *
             * ALARMS
             */
            else if (c.matches("alarmEnable")) {
                this.alarmEnable = rs.getBoolean(c);
            } else if (c.matches("alarm")) {
                AlarmsFacade facade = new AlarmsFacade();
                this.alarm = facade.findById(rs.getInt(c));
            }/**
             *
             * PERSISTENCE
             */
            else if (c.matches("persistenceEnable")) {
                this.persistenceEnable = rs.getBoolean(c);
            } else if (c.matches("persOffsetEnable")) {
                this.persOffsetEnable = rs.getBoolean(c);
            } else if (c.matches("persOffsetFloat")) {
                this.persOffsetFloat = rs.getDouble(c);
            } else if (c.matches("persOffsetInt")) {
                this.persOffsetInt = rs.getInt(c);
            } else if (c.matches("persOffsetBool")) {
                this.persOffsetBool = rs.getBoolean(c);
            } else if (c.matches("persOffsetDateTime")) {
                Timestamp timestamp = rs.getTimestamp(c);
                if (timestamp != null) {
                    this.persOffsetDateTime = new java.util.Date(timestamp.getTime());
                } else {
                    this.persOffsetDateTime = null;
                }
            } /**
             *
             * informations
             */
            else if (c.matches("comment")) {
                this.comment = rs.getString(c);
            } else {
                Util.out(Tags.class + " >> update >> unknown column name " + c);
                System.out.println(Tags.class + " >> update >> unknown column name " + c);
            }

        }
    }

    /**
     * Preapare Statement allowing to update a colum defined by c
     *
     * @param c colum concern to prepared by the statement
     * @return the prepare statement like UPDATE dbo.tags set [column] = ? where
     * id = ?
     */
    public String prepareStatementUpdateOn(String c) {
        if (c.matches("t_name")) {
            return "UPDATE dbo.tags set [t_name] = ? WHERE t_id = ?";
        } else if (c.matches("t_table")) {
            return "UPDATE dbo.tags set [t_table] = ? WHERE t_id = ?";
        } else if (c.matches("t_machine")) {
            return "UPDATE dbo.tags set [t_machine] = ? WHERE t_id = ?";
        } else if (c.matches("t_type")) {
            return "UPDATE dbo.tags set [t_type] = ? WHERE t_id = ?";
        } else if (c.matches("t_memory")) {
            return "UPDATE dbo.tags set [t_memory] = ? WHERE t_id = ?";
        } else if (c.matches("t_db")) {
            return "UPDATE dbo.tags set [t_db] = ? WHERE t_id = ?";
        } else if (c.matches("t_byte")) {
            return "UPDATE dbo.tags set [t_byte] = ? WHERE t_id = ?";
        } else if (c.matches("t_bit")) {
            return "UPDATE dbo.tags set [t_bit] = ? WHERE t_id = ?";
        } else if (c.matches("t_cycle")) {
            return "UPDATE dbo.tags set [t_cycle] = ? WHERE t_id = ?";
        } else if (c.matches("t_active")) {
            return "UPDATE dbo.tags set [t_active] = ? WHERE t_id = ?";
        } else if (c.matches("t_value_float")) {
            return "UPDATE dbo.tags set [t_value_float] = ? WHERE t_id = ?";
        } else if (c.matches("t_value_int")) {
            return "UPDATE dbo.tags set [t_value_int] = ? WHERE t_id = ?";
        } else if (c.matches("t_value_bool")) {
            return "UPDATE dbo.tags set [t_value_bool] = ? WHERE t_id = ?";
        } else if (c.matches("t_value_date")) {
            return "UPDATE dbo.tags set [t_value_date] = ? WHERE t_id = ?";
        } else if (c.matches("t_comment")) {
            return "UPDATE dbo.tags set [t_comment] = ? WHERE t_id = ?";
        } else if (c.matches("t_deleted")) {
            return "UPDATE dbo.tags set [t_deleted] = ? WHERE t_id = ?";
        } else if (c.matches("t_created")) {
            return "UPDATE dbo.tags set [t_created] = ? WHERE t_id = ?";
        } else if (c.matches("t_changed")) {
            return "UPDATE dbo.tags set [t_changed] = ? WHERE t_id = ?";
        } else {
            Util.out(Tags.class + " >> prepareStatementUpdateOn >> unknown column name " + c);
            System.out.println(Tags.class + " >> prepareStatementUpdateOn >> unknown column name " + c);
        }
        return null;
    }

    public static String queryUpdateOn(String column, Object value, Integer id) {
        if (column.matches("name")) {
            return "UPDATE dbo.tags set [name] = '" + value + "' WHERE id = " + id;
        } else if (column.matches("table")) {
            return "UPDATE dbo.tags set [table] = " + value + " WHERE id = " + id;
        } else if (column.matches("machine")) {
            return "UPDATE dbo.tags set [machine] = " + value + " WHERE id = " + id;
        } else if (column.matches("type")) {
            return "UPDATE dbo.tags set [type] = " + value + " WHERE id = " + id;
        } else if (column.matches("memory")) {
            return "UPDATE dbo.tags set [memory] = " + value + " WHERE id = " + id;
        } else if (column.matches("db")) {
            return "UPDATE dbo.tags set [db] = " + value + " WHERE id = " + id;
        } else if (column.matches("byte")) {
            return "UPDATE dbo.tags set [byte] = " + value + " WHERE id = " + id;
        } else if (column.matches("bit")) {
            return "UPDATE dbo.tags set [bit] = " + value + " WHERE id = " + id;
        } else if (column.matches("cycle")) {
            return "UPDATE dbo.tags set [cycle] = " + value + " WHERE id = " + id;
        } else if (column.matches("active")) {
            return "UPDATE dbo.tags set [active] = " + value + " WHERE id = " + id;
        } else if (column.matches("vFloat")) {
            // Ajuste le nombre de détail en milliseconde
            String timestampstr = Instant.now().toString();
            if (timestampstr.contains(".")) {
                timestampstr = timestampstr.substring(0, 19);
            }
            return "UPDATE dbo.tags set [vFloat] = " + value
                    + ", vStamp = '" + timestampstr + "' WHERE id = " + id;
        } else if (column.matches("vInt")) {
            Date date = new Date();
            java.sql.Date timestamp = new java.sql.Date(date.getTime());
            return "UPDATE dbo.tags set [vInt] = " + value
                    + ", vDateTime = " + timestamp + " WHERE id = " + id;
        } else if (column.matches("vBool")) {
            Date date = new Date();
            java.sql.Date timestamp = new java.sql.Date(date.getTime());
            return "UPDATE dbo.tags set [vBool] = " + value
                    + ", vDateTime = " + timestamp + " WHERE id = " + id;
        } else if (column.matches("vDateTime")) {
            return "UPDATE dbo.tags set [vDateTime] = " + value + " WHERE id = " + id;
        } else if (column.matches("comment")) {
            return "UPDATE dbo.tags set [comment] = " + value + " WHERE id = " + id;
        } else if (column.matches("deleted")) {
            return "UPDATE dbo.tags set [deleted] = " + value + " WHERE id = " + id;
        } else if (column.matches("created")) {
            return "UPDATE dbo.tags set [created] = " + value + " WHERE id = " + id;
        } else if (column.matches("changed")) {
            return "UPDATE dbo.tags set [changed] = " + value + " WHERE id = " + id;
        } else {
            Util.out(Tags.class + " >> queryUpdateOn >> unknown column name " + column);
            System.out.println(Tags.class + " >> queryUpdateOn >> unknown column name " + column);
        }
        return null;
    }

    public static String queryUpdateOn(String column, Tags tag) {
        String vStamp = DateUtil.sqlDTConvert(tag.getVStamp());
        String query = null;
        if (column.matches("vFloat")) {
            query = "UPDATE dbo.tags set [vFloat] = " + tag.getVFloat()
                    + ", vStamp = " + vStamp + " WHERE id = " + tag.getId();
        } else if (column.matches("vInt")) {
            query = "UPDATE dbo.tags set [vInt] = " + tag.getVInt()
                    + ", vStamp =" + vStamp + " WHERE id = " + tag.getId();
        } else if (column.matches("vBool")) {
            query = "UPDATE dbo.tags set [vBool] = " + tag.getVBool()
                    + ", vStamp = " + vStamp + " WHERE id = " + tag.getId();
        } else {
            Util.out(Tags.class + " >> queryUpdateOn >> unknown column name " + column);
            System.out.println(Tags.class + " >> queryUpdateOn >> unknown column name " + column);
        }
        return query;
    }

    public String prepareStatementUpdateOn() {
        String stmts = ""
                + "UPDATE [dbo].[tags] "
                + "SET "
                + "[name] = " + name
                + " ,[table] = " + table.getId()
                + " ,[machine] = " + machine.getId()
                + " ,[type] = " + type.getId()
                + " ,[memory] = " + memory.getId()
                + " ,[db] = " + db
                + " ,[byte] = " + byte1
                + " ,[bit] = " + bit
                + " ,[cycle] = " + cycle
                + " ,[active] = " + (active ? 1 : 0)
                + " ,[value_float] = " + vFloat
                + " ,[value_int] = " + vInt
                + " ,[value_bool] = " + vBool
                + " ,[value_date] = " + vDateTime
                + " ,[comment] = " + comment
                + " ,[deleted] = " + (deleted ? 1 : 0)
                + "  WHERE id = " + id;

        return stmts;
    }


}
