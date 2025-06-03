package cn.memoryzy.json.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.util.List;

/**
 * @author Memory
 * @since 2025/5/30
 */
@JsonRootName("plugin-repository")
public class PluginDetail {

    @JacksonXmlProperty(localName = "category")
    private Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public static class Category {

        @JacksonXmlProperty(isAttribute = true)
        private String name;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "idea-plugin")
        private List<IdeaPlugin> ideaPlugins;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<IdeaPlugin> getIdeaPlugins() {
            return ideaPlugins;
        }

        public void setIdeaPlugins(List<IdeaPlugin> ideaPlugins) {
            this.ideaPlugins = ideaPlugins;
        }
    }


    public static class IdeaPlugin {

        @JacksonXmlProperty(isAttribute = true)
        private int downloads;

        @JacksonXmlProperty(isAttribute = true)
        private long size;

        @JacksonXmlProperty(isAttribute = true)
        private long date;

        @JacksonXmlProperty(isAttribute = true)
        private long updatedDate;

        @JacksonXmlProperty(isAttribute = true)
        private String url;

        private String name;
        private String id;
        private String description;
        private String version;

        private Vendor vendor;

        private double rating;

        @JsonProperty("change-notes")
        private String changeNotes;

        private String chineseChangeNotes;

        private String englishChangeNotes;

        @JsonProperty("idea-version")
        private IdeaVersion ideaVersion;

        public int getDownloads() {
            return downloads;
        }

        public void setDownloads(int downloads) {
            this.downloads = downloads;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public long getUpdatedDate() {
            return updatedDate;
        }

        public void setUpdatedDate(long updatedDate) {
            this.updatedDate = updatedDate;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Vendor getVendor() {
            return vendor;
        }

        public void setVendor(Vendor vendor) {
            this.vendor = vendor;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public String getChangeNotes() {
            return changeNotes;
        }

        public void setChangeNotes(String changeNotes) {
            this.changeNotes = changeNotes;
        }

        public String getChineseChangeNotes() {
            return chineseChangeNotes;
        }

        public void setChineseChangeNotes(String chineseChangeNotes) {
            this.chineseChangeNotes = chineseChangeNotes;
        }

        public String getEnglishChangeNotes() {
            return englishChangeNotes;
        }

        public void setEnglishChangeNotes(String englishChangeNotes) {
            this.englishChangeNotes = englishChangeNotes;
        }

        public IdeaVersion getIdeaVersion() {
            return ideaVersion;
        }

        public void setIdeaVersion(IdeaVersion ideaVersion) {
            this.ideaVersion = ideaVersion;
        }
    }


    public static class Vendor {

        @JacksonXmlProperty(isAttribute = true)
        private String email;

        @JacksonXmlProperty(isAttribute = true)
        private String url;

        @JacksonXmlText
        private String name;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    public static class IdeaVersion {
        @JacksonXmlProperty(isAttribute = true, localName = "min")
        private String minVersion;

        @JacksonXmlProperty(isAttribute = true, localName = "max")
        private String maxVersion;

        @JacksonXmlProperty(isAttribute = true, localName = "since-build")
        private String sinceBuild;

        @JacksonXmlProperty(isAttribute = true, localName = "until-build")
        private String untilBuild;

        public String getMinVersion() {
            return minVersion;
        }

        public void setMinVersion(String minVersion) {
            this.minVersion = minVersion;
        }

        public String getMaxVersion() {
            return maxVersion;
        }

        public void setMaxVersion(String maxVersion) {
            this.maxVersion = maxVersion;
        }

        public String getSinceBuild() {
            return sinceBuild;
        }

        public void setSinceBuild(String sinceBuild) {
            this.sinceBuild = sinceBuild;
        }

        public String getUntilBuild() {
            return untilBuild;
        }

        public void setUntilBuild(String untilBuild) {
            this.untilBuild = untilBuild;
        }
    }

}

