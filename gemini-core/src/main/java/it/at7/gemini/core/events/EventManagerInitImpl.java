package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Module;
import it.at7.gemini.core.SchemaManager;
import it.at7.gemini.core.Transaction;
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

    private void resolveAnnotation(String entityName, Annotation annotation, Object bean, Method targetMethod) {
        if (annotation instanceof BeforeInsertField) {
            resolveBeforeInsertField(entityName, (BeforeInsertField) annotation, bean, targetMethod);
        }
    }

    private void resolveBeforeInsertField(String entityName, BeforeInsertField annotation, Object bean, Method targetMethod) {
        String fieldName = annotation.field().toLowerCase();
        Map<String, List<BeanWithMethod>> beforeFieldByFieldName = this.beforeInsertField.computeIfAbsent(entityName, e -> new HashMap<>());
        List<BeanWithMethod> methodList = beforeFieldByFieldName.computeIfAbsent(fieldName, f -> new ArrayList<>());
        methodList.add(BeanWithMethod.of(bean, targetMethod));
    }

    private void checkEntityEvents(Map<String, List<Object>> entityEvents) {
        Collection<Entity> allEntities = schemaManager.getAllEntities();
        entityEvents.keySet().forEach(e -> {
            // ?? TODO
        });
    }

    @Override
    public void beforeInsertFields(EntityRecord record, Transaction transaction) throws GeminiException {
        Entity entity = record.getEntity();
        String entityName = entity.getName();
        Map<String, List<BeanWithMethod>> entityMethods = this.beforeInsertField.get(entityName);

        Set<EntityField> metaEntityFields = entity.getMetaEntityFields();
        for (EntityField field : metaEntityFields) {
            if (entityMethods != null) {
                invokeBeforeInsertFieldMehod(record, entityMethods, field);
                continue;
            }

            // interface methods have low priority that entity events
            String interfaceName = field.getInterfaceName();
            if (interfaceName != null) {
                Map<String, List<BeanWithMethod>> interfaceMethods = this.beforeInsertField.get(interfaceName.toUpperCase());
                if (interfaceMethods != null) {
                    invokeBeforeInsertFieldMehod(record, interfaceMethods, field);
                }
            }
        }

    }

    private void invokeBeforeInsertFieldMehod(EntityRecord record, Map<String, List<BeanWithMethod>> entityMethods, EntityField field) throws GeminiException {
        String fieldName = field.getName();
        List<BeanWithMethod> beanWithMethods = entityMethods.get(fieldName.toLowerCase());
        if (beanWithMethods != null && !beanWithMethods.isEmpty()) {
            // events precedence
            for (BeanWithMethod bm : beanWithMethods) {
                try {
                    Object res = bm.method.invoke(bm.bean);
                    record.put(field, res);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw GeminiGenericException.wrap(e);
                }
            }
        }
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
