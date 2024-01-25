package rrampage.wasp.data;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WastJson {
    private String sourceFilename;
    private WastCommand[] commands;
    public WastJson() {}
    public String getSourceFilename() {return sourceFilename;}
    public void setSourceFilename(String sourceFilename) { this.sourceFilename = sourceFilename;}
    public WastCommand[] getCommands() { return commands;}
    public void setCommands(WastCommand[] commands) {this.commands = commands;}
}
