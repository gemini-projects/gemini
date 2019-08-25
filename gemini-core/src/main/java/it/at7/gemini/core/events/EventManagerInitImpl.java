package it.at7.gemini.core.events;

import it.at7.gemini.core.*;
import it.at7.gemini.core.Module;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiGenericException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class EventManagerInitImpl implements EventManagerInit, EventManager {
    private final ApplicationContext applicationContext;
    private final SchemaManager schemaManager;


    private final Map<String, Map<String, List<BeanWithMethod>>> beforeInsertField = new HashMap<>();
    private final Map<String, Map<String, List<BeanWithMethod>>> onUpdateField = new HashMap<>();


    Map<String, List<Object>> entityEventsBeans;

    @Autowired
    public EventManagerInitImpl(ApplicationContext applicationContext, SchemaManager schemaManager) {
        this.applicationContext = applicationContext;
        this.schemaManager = schemaManager;
    }

    @Override
    public void loadEvents(List<Module> modules) {
        loadEntityEventsByAnnotation();
        // checkEntityEvents(entityEvents);

    }

    private void loadEntityEventsByAnnotation() {
        Map<String, List<Object>> eventsByEntityName = new HashMap<>();
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Events.class);
        for (Object bean : beans.values()) {
            Events event = bean.getClass().getAnnotation(Events.class);
            String entityName = event.entityName().toUpperCase();
            List<Object> entityEvents = eventsByEntityName.computeIfAbsent(entityName, k -> new ArrayList<>());
            entityEvents.add(bean);
        }
        eventsByEntityName.values().forEach(e -> e.sort((b1, b2) -> b1.getClass().getAnnotation(Events.class).order() - b2.getClass().getAnnotation(Events.class).order()));

        eventsByEntityName.entrySet().forEach(ev -> {
            String entityName = ev.getKey();
            List<Object> orderedEventBeansForEntity = ev.getValue();
            for (Object eventBean : orderedEventBeansForEntity) {
                for (Method method : eventBean.getClass().getMethods()) {
                    Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
                    for (Annotation annotation : declaredAnnotations) {
                        resolveAnnotation(entityName, annotation, eventBean, method);
                    }
                }
            }
        });
    }

    private void checkEntityEvents(Map<String, List<Object>> entityEvents) {
        Collection<Entity> allEntities = schemaManager.getAllEntities();
        entityEvents.keySet().forEach(e -> {
            // ?? TODO
        });
    }

    private void resolveAnnotation(String entityName, Annotation annotation, Object bean, Method targetMethod) {
        if (annotation instanceof BeforeInsertField) {
            String fieldName = ((BeforeInsertField) annotation).field().toLowerCase();
            resolveAnnotationField(entityName, fieldName, beforeInsertField, bean, targetMethod);
        }
        if (annotation instanceof BeforeInsertField.List) {
            BeforeInsertField[] bif = ((BeforeInsertField.List) annotation).value();
            for (BeforeInsertField insertField : bif) {
                resolveAnnotationField(entityName, insertField.field(), beforeInsertField, bean, targetMethod);
            }
        }
        if (annotation instanceof OnUpdateField) {
            String fieldName = ((OnUpdateField) annotation).field().toLowerCase();
            resolveAnnotationField(entityName, fieldName, onUpdateField, bean, targetMethod);
        }
        if (annotation instanceof OnUpdateField.List) {
            OnUpdateField[] onF = ((OnUpdateField.List) annotation).value();
            for (OnUpdateField insertField : onF) {
                resolveAnnotationField(entityName, insertField.field(), onUpdateField, bean, targetMethod);
            }
        }
    }

    private void resolveAnnotationField(String entityName, String fieldName, Map<String, Map<String, List<BeanWithMethod>>> mapByEntNameAndField, Object bean, Method targetMethod) {
        Map<String, List<BeanWithMethod>> beforeFieldByFieldName = mapByEntNameAndField.computeIfAbsent(entityName, e -> new HashMap<>());
        List<BeanWithMethod> methodList = beforeFieldByFieldName.computeIfAbsent(fieldName, f -> new ArrayList<>());
        methodList.add(BeanWithMethod.of(bean, targetMethod));
    }

    @Override
    public void beforeInsertFields(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        handleEventForFields(record, transaction, entityOperationContext, this.beforeInsertField);
    }

    @Override
    public void onUpdateFields(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        handleEventForFields(record, transaction, entityOperationContext, this.onUpdateField);
    }

    private void handleEventForFields(EntityRecord record, Transaction transaction, EntityOperationContext entityOperationContext, Map<String, Map<String, List<BeanWithMethod>>> methods) throws GeminiException {
        Entity entity = record.getEntity();
        String entityName = entity.getName();

        Set<EntityField> metaEntityFields = entity.getALLEntityFields();
        for (EntityField field : metaEntityFields) {

            // interface methods have low priority that entity events
            String interfaceName = field.getInterfaceName();
            if (interfaceName != null) {
                Map<String, List<BeanWithMethod>> interfaceMethods = methods.get(interfaceName.toUpperCase());
                if (interfaceMethods != null) {
                    invokeEventMethodForField(record, entityOperationContext, interfaceMethods, field, transaction);
                }
            }

            Map<String, List<BeanWithMethod>> entityMethods = methods.get(entityName);
            if (entityMethods != null) {
                invokeEventMethodForField(record, entityOperationContext, entityMethods, field, transaction);
            }
        }
    }

    private void invokeEventMethodForField(EntityRecord record, EntityOperationContext entityOperationContext, Map<String, List<BeanWithMethod>> entityMethods, EntityField field, Transaction transaction) throws GeminiException {
        String fieldName = field.getName();
        List<BeanWithMethod> beanWithMethods = entityMethods.get(fieldName.toLowerCase());
        if (beanWithMethods != null && !beanWithMethods.isEmpty()) {
            // TODO events precedence
            for (BeanWithMethod bm : beanWithMethods) {
                try {
                    EventContext eventContext = getEventContext(transaction, entityOperationContext, record);
                    Object res = bm.method.invoke(bm.bean, eventContext);
                    if (res != null) {
                        record.put(field, res);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw GeminiGenericException.wrap(e);
                }
            }
        }
    }

    private EventContext getEventContext(Transaction transaction, EntityOperationContext entityOperationContext, EntityRecord record) {
        return new EventContextBuilder()
                .with(transaction)
                .with(entityOperationContext)
                .with(record)
                .build();
    }

    static class BeanWithMethod {
        Object bean;
        Method method;

        public BeanWithMethod(Object bean, Method targetMethod) {
            this.bean = bean;
            this.method = targetMethod;
        }

        public static BeanWithMethod of(Object bean, Method targetMethod) {
            return new BeanWithMethod(bean, targetMethod);
        }
    }
}
