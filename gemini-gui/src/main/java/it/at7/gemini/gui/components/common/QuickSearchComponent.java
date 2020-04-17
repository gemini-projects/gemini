package it.at7.gemini.gui.components.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.gui.annotation.GeminiGuiComponent;
import it.at7.gemini.gui.annotation.GeminiGuiComponentHook;
import it.at7.gemini.gui.schema.EntityGUIRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@GeminiGuiComponent("Common.QuickSearch")
public class QuickSearchComponent implements GeminiGuiComponentHook {

    private final EntityManager entityManager;

    @Autowired
    public QuickSearchComponent(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Object> onInit(@Nullable Map<String, Object> body) {

        // TODO filter could be specialized with parametric data
        Object filterObj = body == null ? null : body.get("filter");
        String filter = (!(filterObj instanceof String)) ? "" : (String) filterObj;

        List<SearchElement> elements = entityManager.getAllEntities().stream()
                .filter(entity -> {
                    switch (filter) {
                        case "ONLY_CUSTOM":
                            return !List.of("CORE", "AUTH", "GUI").contains(entity.getModule().getName().toUpperCase());
                        case "ALL":
                            return true;
                        default:
                            return false;
                    }
                })
                .filter(entity -> !entity.isEmbedable())
                .map(e -> {
                    try {
                        return SearchElement.of(SearchType.ENTITY, e.getName(),
                                this.entityManager.get(EntityGUIRef.NAME, e.getName()).get(EntityGUIRef.FIELDS.DISPLAY_NAME));
                    } catch (GeminiException ex) {
                        throw new GeminiRuntimeException(ex);
                    }
                })
                .collect(Collectors.toList());

        /* add other elements type here */

        return Optional.of(elements);
    }


    enum SearchType {
        ENTITY
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class SearchElement {
        public SearchType searchType;
        public String searchRoute;
        public String searchName;

        SearchElement(SearchType searchType, String searchRoute, String searchName) {
            this.searchType = searchType;
            this.searchRoute = searchRoute;
            this.searchName = searchName == null ? searchRoute : searchName;
        }

        static SearchElement of(SearchType elementType, String elementRoute, String displayName) {
            return new SearchElement(elementType, elementRoute, displayName);
        }
    }
}
