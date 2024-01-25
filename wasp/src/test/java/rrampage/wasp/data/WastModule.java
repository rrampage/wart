package rrampage.wasp.data;

public final class WastModule extends WastCommand {
    private String module;
    private String filename;

    public WastModule() {}
    public String getModule() {
        return module;
    }
    public void setModule(String module) { this.module = module;}
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) { this.filename = filename;}
}
