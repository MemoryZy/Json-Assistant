package cn.memoryzy.json.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PluginUpdateDetail {

    @JsonProperty(value = "id")
    private Long id;

    @JsonProperty(value = "link")
    private String link;

    @JsonProperty(value = "version")
    private String version;

    @JsonProperty(value = "approve")
    private Boolean approve;

    @JsonProperty(value = "listed")
    private Boolean listed;

    @JsonProperty(value = "hidden")
    private Boolean hidden;

    @JsonProperty(value = "recalculateCompatibilityAllowed")
    private Boolean recalculateCompatibilityAllowed;

    @JsonProperty(value = "cdate")
    private Long cdate;

    @JsonProperty(value = "file")
    private String file;

    @JsonProperty(value = "notes")
    private String notes;

    private String zhNotes;

    private String enNotes;

    @JsonProperty(value = "since")
    private String since;

    @JsonProperty(value = "until")
    private String until;

    @JsonProperty(value = "sinceUntil")
    private String sinceUntil;

    @JsonProperty(value = "channel")
    private String channel;

    @JsonProperty(value = "size")
    private Integer size;

    @JsonProperty(value = "downloads")
    private Integer downloads;

    @JsonProperty(value = "pluginId")
    private Integer pluginId;

    @JsonProperty(value = "compatibleVersions")
    private CompatibleVersions compatibleVersions;

    @JsonProperty(value = "author")
    private Author author;

    @JsonProperty(value = "modules")
    private List<?> modules;


    public static class CompatibleVersions {

        @JsonProperty(value = "CLION")
        private String clion;

        @JsonProperty(value = "ANDROID_STUDIO")
        private String androidStudio;

        @JsonProperty(value = "RUST")
        private String rust;

        @JsonProperty(value = "AQUA")
        private String aqua;

        @JsonProperty(value = "CWMGUEST")
        private String cwmguest;

        @JsonProperty(value = "RIDER")
        private String rider;

        @JsonProperty(value = "IDEA_COMMUNITY")
        private String ideaCommunity;

        @JsonProperty(value = "PYCHARM_COMMUNITY")
        private String pycharmCommunity;

        @JsonProperty(value = "IDEA")
        private String idea;

        @JsonProperty(value = "WEBSTORM")
        private String webstorm;

        @JsonProperty(value = "APPCODE")
        private String appcode;

        @JsonProperty(value = "DATASPELL")
        private String dataspell;

        @JsonProperty(value = "GOLAND")
        private String goland;

        @JsonProperty(value = "GATEWAY")
        private String gateway;

        @JsonProperty(value = "PHPSTORM")
        private String phpstorm;

        @JsonProperty(value = "DBE")
        private String dbe;

        @JsonProperty(value = "RUBYMINE")
        private String rubymine;

        @JsonProperty(value = "JBCLIENT")
        private String jbclient;

        @JsonProperty(value = "PYCHARM")
        private String pycharm;

        @JsonProperty(value = "MPS")
        private String mps;

        @JsonProperty(value = "WRITERSIDE")
        private String writerside;


        public String getClion() {
            return clion;
        }

        public void setClion(String clion) {
            this.clion = clion;
        }

        public String getAndroidStudio() {
            return androidStudio;
        }

        public void setAndroidStudio(String androidStudio) {
            this.androidStudio = androidStudio;
        }

        public String getRust() {
            return rust;
        }

        public void setRust(String rust) {
            this.rust = rust;
        }

        public String getAqua() {
            return aqua;
        }

        public void setAqua(String aqua) {
            this.aqua = aqua;
        }

        public String getCwmguest() {
            return cwmguest;
        }

        public void setCwmguest(String cwmguest) {
            this.cwmguest = cwmguest;
        }

        public String getRider() {
            return rider;
        }

        public void setRider(String rider) {
            this.rider = rider;
        }

        public String getIdeaCommunity() {
            return ideaCommunity;
        }

        public void setIdeaCommunity(String ideaCommunity) {
            this.ideaCommunity = ideaCommunity;
        }

        public String getPycharmCommunity() {
            return pycharmCommunity;
        }

        public void setPycharmCommunity(String pycharmCommunity) {
            this.pycharmCommunity = pycharmCommunity;
        }

        public String getIdea() {
            return idea;
        }

        public void setIdea(String idea) {
            this.idea = idea;
        }

        public String getWebstorm() {
            return webstorm;
        }

        public void setWebstorm(String webstorm) {
            this.webstorm = webstorm;
        }

        public String getAppcode() {
            return appcode;
        }

        public void setAppcode(String appcode) {
            this.appcode = appcode;
        }

        public String getDataspell() {
            return dataspell;
        }

        public void setDataspell(String dataspell) {
            this.dataspell = dataspell;
        }

        public String getGoland() {
            return goland;
        }

        public void setGoland(String goland) {
            this.goland = goland;
        }

        public String getGateway() {
            return gateway;
        }

        public void setGateway(String gateway) {
            this.gateway = gateway;
        }

        public String getPhpstorm() {
            return phpstorm;
        }

        public void setPhpstorm(String phpstorm) {
            this.phpstorm = phpstorm;
        }

        public String getDbe() {
            return dbe;
        }

        public void setDbe(String dbe) {
            this.dbe = dbe;
        }

        public String getRubymine() {
            return rubymine;
        }

        public void setRubymine(String rubymine) {
            this.rubymine = rubymine;
        }

        public String getJbclient() {
            return jbclient;
        }

        public void setJbclient(String jbclient) {
            this.jbclient = jbclient;
        }

        public String getPycharm() {
            return pycharm;
        }

        public void setPycharm(String pycharm) {
            this.pycharm = pycharm;
        }

        public String getMps() {
            return mps;
        }

        public void setMps(String mps) {
            this.mps = mps;
        }

        public String getWriterside() {
            return writerside;
        }

        public void setWriterside(String writerside) {
            this.writerside = writerside;
        }
    }

    public static class Author {

        @JsonProperty(value = "id")
        private String id;

        @JsonProperty(value = "name")
        private String name;

        @JsonProperty(value = "link")
        private String link;

        @JsonProperty(value = "hubLogin")
        private String hubLogin;

        @JsonProperty(value = "icon")
        private String icon;

        @JsonProperty(value = "personalVendorId")
        private Integer personalVendorId;

        @JsonProperty(value = "showMarketoCheckbox")
        private Boolean showMarketoCheckbox;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getHubLogin() {
            return hubLogin;
        }

        public void setHubLogin(String hubLogin) {
            this.hubLogin = hubLogin;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public Integer getPersonalVendorId() {
            return personalVendorId;
        }

        public void setPersonalVendorId(Integer personalVendorId) {
            this.personalVendorId = personalVendorId;
        }

        public Boolean getShowMarketoCheckbox() {
            return showMarketoCheckbox;
        }

        public void setShowMarketoCheckbox(Boolean showMarketoCheckbox) {
            this.showMarketoCheckbox = showMarketoCheckbox;
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    public Boolean getListed() {
        return listed;
    }

    public void setListed(Boolean listed) {
        this.listed = listed;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getRecalculateCompatibilityAllowed() {
        return recalculateCompatibilityAllowed;
    }

    public void setRecalculateCompatibilityAllowed(Boolean recalculateCompatibilityAllowed) {
        this.recalculateCompatibilityAllowed = recalculateCompatibilityAllowed;
    }

    public Long getCdate() {
        return cdate;
    }

    public void setCdate(Long cdate) {
        this.cdate = cdate;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getZhNotes() {
        return zhNotes;
    }

    public void setZhNotes(String zhNotes) {
        this.zhNotes = zhNotes;
    }

    public String getEnNotes() {
        return enNotes;
    }

    public void setEnNotes(String enNotes) {
        this.enNotes = enNotes;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    public String getSinceUntil() {
        return sinceUntil;
    }

    public void setSinceUntil(String sinceUntil) {
        this.sinceUntil = sinceUntil;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getDownloads() {
        return downloads;
    }

    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    public Integer getPluginId() {
        return pluginId;
    }

    public void setPluginId(Integer pluginId) {
        this.pluginId = pluginId;
    }

    public CompatibleVersions getCompatibleVersions() {
        return compatibleVersions;
    }

    public void setCompatibleVersions(CompatibleVersions compatibleVersions) {
        this.compatibleVersions = compatibleVersions;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<?> getModules() {
        return modules;
    }

    public void setModules(List<?> modules) {
        this.modules = modules;
    }
}