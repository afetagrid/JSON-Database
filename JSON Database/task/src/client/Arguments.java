package client;

import com.beust.jcommander.Parameter;

public class Arguments {
    @Parameter(names = "-t", description = "Type of the request")
    private String type;
    @Parameter(names = "-k", description = "Key")
    private String key;
    @Parameter(names = "-v", description = "Value")
    private String value;
    @Parameter(names = "-in", description = "Path to file in JSON format")
    private String path;

    public String getType() { return type; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getPath() { return path; }
}
