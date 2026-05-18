package cl.kanopus.workflow.model.form;

public enum ValidationType {
    REQUIRED,
    MIN,
    MAX,
    MIN_LENGTH,
    MAX_LENGTH,
    REGEX,
    EMAIL,
    URL,
    CUSTOM_EXPRESSION
}
