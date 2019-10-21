package it.at7.gemini.core.persistence;

public interface PersistenceEntityFilterUtilityService {
    /**
     * Return a not contains condition on fieldName and using the paremter name
     */
    String notContainsCondition(String fieldName, String parameterName);
}
