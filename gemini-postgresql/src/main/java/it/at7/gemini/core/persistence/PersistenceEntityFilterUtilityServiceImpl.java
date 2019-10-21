package it.at7.gemini.core.persistence;

import org.springframework.stereotype.Service;

@Service
public class PersistenceEntityFilterUtilityServiceImpl implements PersistenceEntityFilterUtilityService {
    @Override
    public String notContainsCondition(String fieldName, String parameterName) {
        return String.format(" %s NOT IN (:%s)", fieldName, parameterName);
    }
}
