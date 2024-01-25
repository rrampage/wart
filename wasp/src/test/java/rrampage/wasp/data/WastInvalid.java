package rrampage.wasp.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class WastInvalid extends WastCommand {
    @JsonProperty("module_type")
    private String moduleType;
    private String filename;
    private String text;
    public WastInvalid() {}
    public String getModuleType() {
        return moduleType;
    }
    public String getFilename() {
        return filename;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) { this.text = text;}
    public void setFilename(String filename) { this.filename = filename;}
    public void setModuleType(String moduleType) { this.moduleType = moduleType;}
}
