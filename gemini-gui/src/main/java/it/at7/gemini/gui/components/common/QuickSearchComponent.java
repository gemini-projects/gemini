package it.at7.gemini.gui.components.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.gui.annotation.GeminiGuiComponent;
import it.at7.gemini.gui.annotation.GeminiGuiComponentHook;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
    public Optional<Object> onInit() {

        List<SearchElement> elements = entityManager.getAllEntities().stream()
                // todo add a filter for the entity based on some flag ??s
                .filter(entity -> !entity.isEmbedable())
                .map(e -> SearchElement.of(SearchType.ENTITY, e.getName()))
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

        static SearchElement of(SearchType elementType, String elementRoute) {
            // TODO ADD entity displayName
            return new SearchElement(elementType, elementRoute, null);
        }
    }
}
