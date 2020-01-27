package it.at7.gemini.api;

import it.at7.gemini.core.EntityOperationContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface to use some common REST API Controller features
 */
public interface RestAPIControllerInterface {


    /**
     * Create an API compliant Entity Operation Context. This method can be used by third party modules/libraries that
     * implements custom APIs but want to use the Gemini API context (for example to add users information, specific
     * behaviour and so on.
     *
     * @param request {@link HttpServletRequest} request
     * @return Entity Operation Context to use
     */
    default EntityOperationContext createEntityOperationContext(HttpServletRequest request) {
        return createEntityOperationContext(request, null, null);
    }


    /**
     * Create an API compliant Entity Operation Context. This method can be used by third party modules/libraries that
     * implements custom APIs but want to use the Gemini API context (for example to add users information, specific
     * behaviour and so on.
     *
     * @param request {@link HttpServletRequest} request
     * @param entity  optional entity name
     * @param body    optional body object
     * @return Entity Operation Context to use
     */
    EntityOperationContext createEntityOperationContext(HttpServletRequest request, String entity, Object body);

}
