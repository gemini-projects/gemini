package it.at7.gemini.api;

import it.at7.gemini.core.EntityOperationContext;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;

public interface RestAPIControllerListener {

    /**
     * For each Entity request an EntityOperation Context is created. You can use the following callback to add custom
     * logic accordingly to your module. You can extend the entity Operation Context withRecord your information and retrieve
     * them later during Entity Manager events
     *
     * @param entity                 target entity
     * @param body                   request body
     * @param request                full request
     * @param entityOperationContext the entity operation context you can extend
     */
    void onEntityOperationContextCreate(HttpServletRequest request, EntityOperationContext entityOperationContext, @Nullable String entity, @Nullable Object body);
}
