package eu.openmos.model;

import java.util.Date;
import java.util.List;
import eu.openmos.model.utilities.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import org.bson.Document;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.log4j.Logger;
       

/**
 * Object that describe a recipe of the system.
 * 
 * @author Pedro Lima Monteiro <pedro.monteiro@uninova.pt>
 * @author Luis Ribeiro
 * @author Valerio Gentile <valerio.gentile@we-plus.eu>
 */
@XmlRootElement(name = "recipe")
@XmlAccessorType(XmlAccessType.FIELD)
public class Recipe extends Base implements Serializable {    
    private static final Logger logger = Logger.getLogger(Recipe.class.getName());
    private static final long serialVersionUID = 6529685098267757022L;

    /**
     * Recipe ID.
     */
    @XmlElement(name = "amlId")
    private String uniqueId;
    /**
     * Recipe name.
     */
    @XmlElement(name = "name")
    private String name;
    /**
     * Recipe description.
     */
    @XmlElement(name = "description")    
    private String description;
    /**
     * Whether the recipe is valid or not.
     */
    @XmlElement(name = "valid")
    private boolean valid = false;
    /**
     * Recipe's parameter settings. These must match the skill's parameters.
     */
    private List<ParameterSetting> parameterSettings;
    /**
     * The skills necessary to execute this recipe.
     */
    @XmlElement(name = "skillRequirements")
    private List<SkillRequirement> skillRequirements;
    /**
     * Recipe's KPI Settings. These must match the skill's KPIs.
     */
    private List<KPISetting> kpiSettings;

    /**
     * Pointer to the skill.
     */
    @XmlElement(name = "skill")
    private Skill skill;
    private ControlPort executedBySkillControlPort;
    
    /**
     * The agent capable of executing this recipe.
     * 
     * MSB alignment.
     * on the msb side they have the adapter id, and on the adapter class they have the agent id
     */
    private String uniqueAgentName;
    /**
     * Recipe's equipment.
     */
    private Equipment equipment;
    /**
     * Whether the recipe is optimized or not.
     */
    private boolean optimized = false;
    /**
     * Last optimization timestamp.
    */
    private Date lastOptimizationTime;
    
    /** 
     * MSB alignment.
     */
    private String msbProtocolEndpoint;

    /**
     * Default constructor, for reflection
     */
    public Recipe() {super();}
    
    /**
     * Parameterized constructor.
     * 
     * @param description - Recipe's description.
     * @param uniqueId - Recipe's ID.
     * @param kpisSetting - Recipe's KPI Settings. These must match the skill's 
     * KPIs.
     * @param name - Recipe's name.
     * @param parameterSettings - Recipe's parameter settings. These must match 
     * the skill's parameters.
     * @param uniqueAgentName - The agent capable of executing this recipe.
     * @param skillRequirements - The skills necessary to execute this recipe.
     * @param skill
     * @param equipment
     * @param msbProtocolEndpoint,
     * @param registeredTimestamp
     */
    public Recipe(String description, 
            String uniqueId, 
            List<KPISetting> kpiSettings, 
            String name, 
            List<ParameterSetting> parameterSettings, 
            String uniqueAgentName, 
            List<SkillRequirement> skillRequirements,
            Skill skill,
            ControlPort executedBySkillControlPort,
            Equipment equipment,
            String msbProtocolEndpoint,
            boolean valid,
            Date registeredTimestamp) {
        super(registeredTimestamp);

        this.description = description;
        this.uniqueId = uniqueId;
        this.kpiSettings = kpiSettings;
        this.name = name;
        this.parameterSettings = parameterSettings;
        this.uniqueAgentName = uniqueAgentName;
        this.skillRequirements = skillRequirements;
        
        this.skill = skill;
        this.executedBySkillControlPort = executedBySkillControlPort;
        
        this.equipment = equipment;
        this.msbProtocolEndpoint = msbProtocolEndpoint;        
        this.valid = valid;
    }

    public List<SkillRequirement> getSkillRequirements() {
        return skillRequirements;
    }

    public void setSkillRequirements(List<SkillRequirement> skillRequirements) {
        this.skillRequirements = skillRequirements;
    }

    public String getUniqueAgentName() {
        return uniqueAgentName;
    }

    public void setUniqueAgentName(String uniqueAgentName) {
        this.uniqueAgentName = uniqueAgentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public List<KPISetting> getKpiSettings() {
        return kpiSettings;
    }

    public void setKpiSettings(List<KPISetting> kpiSettings) {
        this.kpiSettings = kpiSettings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParameterSetting> getParameterSettings() {
        return parameterSettings;
    }

    public void setParameterSettings(List<ParameterSetting> parameterSettings) {
        this.parameterSettings = parameterSettings;
    } 

    public Skill getSkill() {
        return skill; 
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public boolean isOptimized() {
        return optimized;
    }

    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
    }

    public Date getLastOptimizationTime() {
        return lastOptimizationTime;
    }

    public void setLastOptimizationTime(Date lastOptimizationTimestamp) {
        this.lastOptimizationTime = lastOptimizationTimestamp;
    }

    public String getMsbProtocolEndpoint() {
        return msbProtocolEndpoint;
    }

    public void setMsbProtocolEndpoint(String msbProtocolEndpoint) {
        this.msbProtocolEndpoint = msbProtocolEndpoint;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public ControlPort getExecutedBySkillControlPort() {
        return executedBySkillControlPort;
    }

    public void setExecutedBySkillControlPort(ControlPort executedBySkillControlPort) {
        this.executedBySkillControlPort = executedBySkillControlPort;
    }

    
     /**
     * Method that serializes the object into a BSON document.
     * 
     * @return BSON form of the object. 
     */
    public Document toBSON() {
        Document doc = new Document();
        
        List<String> kpiSettingIds = kpiSettings.stream().map(kpiSetting -> kpiSetting.getUniqueId()).collect(Collectors.toList());
        List<String> parameterSettingIds = parameterSettings.stream().map(parameterSetting -> parameterSetting.getUniqueId()).collect(Collectors.toList());        
        List<String> skillRequirementIds = skillRequirements.stream().map(skillRequirement -> skillRequirement.getName()).collect(Collectors.toList());        

        SimpleDateFormat sdf = new SimpleDateFormat(SerializationConstants.DATE_REPRESENTATION);
        
        doc.append("description", description);
        doc.append("id", uniqueId);
        doc.append("kpiSettings", kpiSettingIds);
        doc.append("name", name);
        doc.append("parameterSettings", parameterSettingIds);        
        doc.append("uniqueAgentName", uniqueAgentName);
        doc.append("skillRequirements", skillRequirementIds);           
        doc.append("skillId", skill.getUniqueId());
        doc.append("executedBySkillControlPortId", executedBySkillControlPort.getUniqueId());
        doc.append("equipmentId", equipment.getUniqueId());
        doc.append("optimized", optimized);
        doc.append("lastOptimizationTime", this.lastOptimizationTime == null ? "null" : sdf.format(this.lastOptimizationTime));
        doc.append("msbProtocolEndpoint", msbProtocolEndpoint);
        
        doc.append("valid", valid);
        
        doc.append("registered", sdf.format(this.registered));
        
        return doc;
    }
}
