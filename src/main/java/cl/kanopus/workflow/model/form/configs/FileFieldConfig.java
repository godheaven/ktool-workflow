package cl.kanopus.workflow.model.form.configs;

import lombok.Data;
import java.util.List;

@Data
public class FileFieldConfig implements FieldConfig {
    private List<String> allowedExtensions;
    private List<String> allowedMimeTypes;
    private Long maxFileSizeMb;
    private Integer maxFiles;
    private Boolean virusScan;
    private Boolean previewEnabled;
    private Boolean imageOnly;
    private String storageProvider;
}
