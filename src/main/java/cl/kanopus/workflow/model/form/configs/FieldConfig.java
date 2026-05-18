package cl.kanopus.workflow.model.form.configs;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "TEXT"),
    @JsonSubTypes.Type(value = NumberFieldConfig.class, name = "NUMBER"),
    @JsonSubTypes.Type(value = FileFieldConfig.class, name = "FILE"),
    @JsonSubTypes.Type(value = TableFieldConfig.class, name = "TABLE"),
    @JsonSubTypes.Type(value = OptionsFieldConfig.class, name = "SELECT"),
    @JsonSubTypes.Type(value = OptionsFieldConfig.class, name = "RADIO"),
    @JsonSubTypes.Type(value = OptionsFieldConfig.class, name = "MULTISELECT"),
    @JsonSubTypes.Type(value = DateFieldConfig.class, name = "DATE"),
    @JsonSubTypes.Type(value = DateTimeFieldConfig.class, name = "DATETIME"),
    @JsonSubTypes.Type(value = BooleanFieldConfig.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = BooleanFieldConfig.class, name = "CHECKBOX"),
    @JsonSubTypes.Type(value = PickerFieldConfig.class, name = "USER_PICKER"),
    @JsonSubTypes.Type(value = PickerFieldConfig.class, name = "ORG_PICKER"),
    @JsonSubTypes.Type(value = RichTextFieldConfig.class, name = "RICH_TEXT"),
    @JsonSubTypes.Type(value = RepeaterFieldConfig.class, name = "REPEATER"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "EMAIL"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "PHONE"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "URL"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "SIGNATURE"),
    @JsonSubTypes.Type(value = FileFieldConfig.class, name = "IMAGE"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "PASSWORD"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "COLOR"),
    @JsonSubTypes.Type(value = PickerFieldConfig.class, name = "ROLE_PICKER"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "HIDDEN"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "CALCULATED"),
    @JsonSubTypes.Type(value = TextFieldConfig.class, name = "DISPLAY_TEXT")
    // Add more as needed
})
public interface FieldConfig {
}
