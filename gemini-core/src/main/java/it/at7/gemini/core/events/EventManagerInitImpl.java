package it.at7.gemini.core.events;

import it.at7.gemini.core.*;
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
    private final StateManager stateManager;


    /**
     * Maps contains method to invoke for each Entity -> Field accordingly to the event type
     */
    private final Map<String, Map<String, List<BeanWithMethod>>> beforeInsertField = new HashMap<>();
    private final Map<String, Map<String, List<BeanWithMethod>>> onUpdateField = new HashMap<>();

    /**
     * Maps contains method to invoke for each Entity accordingly to the event type
     */
    private final Map<String, List<BeanWithMethod>> onRecordInserted = new HashMap<>();
    private final Map<String, List<BeanWithMethod>> beforeCreateRecord = new HashMap<>();
    private final Map<String, List<BeanWithMethod>> beforeUpdateRecord = new HashMap<>();
    private final Map<String, List<BeanWithMethod>> beforeDeleteRecord = new HashMap<>();


    Map<String, List<Object>> entityEventsBeans;

    @Autowired
    public EventManagerInitImpl(ApplicationContext applicationContext, SchemaManager schemaManager, StateManager stateManager) {
        this.applicationContext = applicationContext;
        this.schemaManager = schemaManager;
        this.stateManager = stateManager;
    }

    @Override
    public void loadEvents() {
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
                for (Method method : eventBean.getClass().getDeclaredMethods()) {
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
        if (annotation instanceof OnRecordInserted) {
            resolveAnnotationEntity(entityName, onRecordInserted, bean, targetMethod);
        }
        if (annotation instanceof BeforeCreateRecord) {
            resolveAnnotationEntity(entityName, beforeCreateRecord, bean, targetMethod);
        }

        if (annotation instanceof BeforeUpdateRecord) {
            resolveAnnotationEntity(entityName, beforeUpdateRecord, bean, targetMethod);
        }

        if (annotation instanceof BeforeDeleteRecord) {
            resolveAnnotationEntity(entityName, beforeDeleteRecord, bean, targetMethod);
        }
    }

    private void resolveAnnotationField(String entityName, String fieldName, Map<String, Map<String, List<BeanWithMethod>>> mapByEntNameAndField, Object bean, Method targetMethod) {
        Map<String, List<BeanWithMethod>> beforeFieldByFieldName = mapByEntNameAndField.computeIfAbsent(entityName, e -> new HashMap<>());
        List<BeanWithMethod> methodList = beforeFieldByFieldName.computeIfAbsent(fieldName, f -> new ArrayList<>());
        methodList.add(BeanWithMethod.of(bean, targetMethod));
    }

    private void resolveAnnotationEntity(String entityName, Map<String, List<BeanWithMethod>> mapByEntityName, Object bean, Method targetMethod) {
        List<BeanWithMethod> methodList = mapByEntityName.computeIfAbsent(entityName, e -> new ArrayList<>());
        methodList.add(BeanWithMethod.of(bean, targetMethod));
    }

    @Override
    public void beforeInsertFields(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        handleEventForFields(record, null, transaction, entityOperationContext, this.beforeInsertField);
    }

    @Override
    public void onUpdateFields(EntityRecord record, EntityRecord persistedRecord, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        handleEventForFields(record, persistedRecord, transaction, entityOperationContext, this.onUpdateField);
    }

    private void handleEventForFields(EntityRecord record, EntityRecord persistedRecord, Transaction transaction, EntityOperationContext entityOperationContext, Map<String, Map<String, List<BeanWithMethod>>> methods) throws GeminiException {
        Entity entity = record.getEntity();
        String entityName = entity.getName();

        Set<EntityField> metaEntityFields = entity.getALLEntityFields();
        for (EntityField field : metaEntityFields) {

            // interface methods have low priority that entity events
            String interfaceName = field.getInterfaceName();
            if (interfaceName != null) {
                Map<String, List<BeanWithMethod>> interfaceMethods = methods.get(interfaceName.toUpperCase());
                if (interfaceMethods != null) {
                    invokeEventMethodForField(record, persistedRecord, entityOperationContext, interfaceMethods, field, transaction);
                }
            }

            Map<String, List<BeanWithMethod>> entityMethods = methods.get(entityName);
            if (entityMethods != null) {
                invokeEventMethodForField(record, persistedRecord, entityOperationContext, entityMethods, field, transaction);
            }
        }
    }

    private void invokeEventMethodForField(EntityRecord record, EntityRecord persistedRecord, EntityOperationContext entityOperationContext, Map<String, List<BeanWithMethod>> entityMethods, EntityField field, Transaction transaction) throws GeminiException {
        String fieldName = field.getName();
        List<BeanWithMethod> beanWithMethods = entityMethods.get(fieldName.toLowerCase());
        if (beanWithMethods != null && !beanWithMethods.isEmpty()) {
            // TODO events sorting and priority
            for (BeanWithMethod bm : beanWithMethods) {
                try {
                    EventContext eventContext = getEventContext(transaction, entityOperationContext, record, persistedRecord);
                    Object res = bm.method.invoke(bm.bean, eventContext);
                    if (res != null) {
                        record.put(field, res);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw GeminiGenericException.wrap(e.getCause());
                }
            }
        }
    }

    @Override
    public void beforeUpdateRecord(EntityRecord record, EntityRecord persistedRecord, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiGenericException {
        handleEventForEntity(record, persistedRecord, transaction, entityOperationContext, this.beforeUpdateRecord);
    }

    @Override
    public void beforeCreateRecord(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiGenericException {
        handleEventForEntity(record, null, transaction, entityOperationContext, this.beforeCreateRecord);
    }

    @Override
    public void beforeDeleteRecord(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiGenericException {
        handleEventForEntity(record, null, transaction, entityOperationContext, this.beforeDeleteRecord);
    }

    @Override
    public void onInsertedRecord(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiGenericException {
        handleEventForEntity(record, null, transaction, entityOperationContext, this.onRecordInserted);
    }

    private void handleEventForEntity(EntityRecord record, EntityRecord persistedRecord, Transaction transaction, EntityOperationContext entityOperationContext, Map<String, List<BeanWithMethod>> methods) throws GeminiGenericException {
        Entity entity = record.getEntity();
        String entityName = entity.getName();

        List<String> implementsIntefaces = entity.getImplementsIntefaces();
        for (String intf : implementsIntefaces) {
            List<BeanWithMethod> interfaceMethods = methods.get(intf);
            ivokeMethodForEntity(record, persistedRecord, entityOperationContext, interfaceMethods, transaction);
        }

        List<BeanWithMethod> entityMethods = methods.get(entityName);
        ivokeMethodForEntity(record, persistedRecord, entityOperationContext, entityMethods, transaction);
    }

    private void ivokeMethodForEntity(EntityRecord record, EntityRecord persistedRecord, EntityOperationContext entityOperationContext, List<BeanWithMethod> beanWithMethods, Transaction transaction) throws GeminiGenericException {
        if (beanWithMethods != null && !beanWithMethods.isEmpty()) {
            // TODO events sorting and priority
            for (BeanWithMethod bm : beanWithMethods) {
                try {
                    EventContext eventContext = getEventContext(transaction, entityOperationContext, record, persistedRecord);
                    bm.method.invoke(bm.bean, eventContext);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw GeminiGenericException.wrap(e);
                }
            }
        }
    }

    private EventContext getEventContext(Transaction transaction, EntityOperationContext entityOperationContext, EntityRecord record, EntityRecord persistedRecord) {
        return new EventContextBuilder()
                .with(transaction)
                .with(entityOperationContext)
                .withRecord(record)
                .withPersistedRecord(persistedRecord)
                .build();
    }

    /**
     * Utility class to bind a Method and its definition bean object
     */
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
