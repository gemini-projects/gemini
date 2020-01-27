package it.at7.gemini.schema;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.ModuleBase;
import it.at7.gemini.core.RecordConverters;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.EntityFieldNotFoundException;
import it.at7.gemini.exceptions.EntityMetaFieldNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

public class Entity {
    public static final String ENTITY = "ENTITY";
    public static final String FIELD_RESOLUTION = "FIELDRESOLUTION";
    public static final String CORE_META_INTERFACE = "COREMETA";

    public static final String NAME = "name";

    private final ModuleBase module;
    private final String name;
    private final Map<String, Object> defaultRecord;
    private final Set<EntityField> dataFields;
    private final Map<String, EntityField> metaFieldsByName;
    private final Map<String, EntityField> dataFieldsByName;
    private final Set<EntityField> metaFields;
    private final LogicalKey logicalKey;
    private final EntityField idField;
    private final boolean embedable;
    private final boolean oneRecord;
    private final boolean tree;
    private final List<String> implementsIntefaces;
    private Object idValue;
    private EntityRecord actualEntityRecord;

    private boolean isClosedDomain = false;

    public Entity(ModuleBase module, String name, boolean embedable, boolean oneRecord, boolean tree, List<String> implementsIntefaces, List<EntityFieldBuilder> fieldsBuilders, @Nullable Object defaultRecord) {
        this.oneRecord = oneRecord;
        Assert.notNull(module, "Module must be not null");
        Assert.notNull(name, "Entity name must be not null");
        this.module = module;
        this.name = name;
        this.embedable = embedable;
        this.tree = tree;
        this.defaultRecord = defaultRecord == null ? new HashMap<>() : (Map<String, Object>) defaultRecord;
        fieldsBuilders.forEach(f -> f.setEntity(this));
        this.dataFields = fieldsBuilders.stream().filter(e -> e.getScope().equals(EntityField.Scope.DATA)).map(EntityFieldBuilder::build).collect(toSet());
        this.dataFieldsByName = dataFields.stream().collect(Collectors.toMap(e -> e.getName().toLowerCase(), e -> e));
        this.metaFields = embedable ? Set.of() : fieldsBuilders.stream().filter(e -> e.getScope().equals(EntityField.Scope.META)).map(EntityFieldBuilder::build).collect(toSet());
        this.metaFieldsByName = metaFields.stream().collect(Collectors.toMap(e -> e.getName().toLowerCase(), e -> e));
        Assert.isTrue(uniqueMetaAndDataField(dataFieldsByName.keySet(), metaFieldsByName.keySet()), "Data/Meta Fields names are not unique");
        this.logicalKey = extractLogicalKeyFrom(dataFields);
        this.idField = EntityFieldBuilder.ID(this);
        this.implementsIntefaces = new ArrayList<>(implementsIntefaces);
        idValue = null;
    }

    private boolean uniqueMetaAndDataField(Set<String> data, Set<String> meta) {
        Set<String> intersection = new HashSet<>(data);
        intersection.retainAll(meta);
        return intersection.isEmpty();
    }

    @NotNull
    public String getName() {
        return name;
    }

    public ModuleBase getModule() {
        return module;
    }

    public boolean isEmbedable() {
        return embedable;
    }

    /**
     * Entity must contain only a single record
     *
     * @return
     */
    public boolean isOneRecord() {
        return oneRecord;
    }

    public boolean isTree() {
        return tree;
    }

    public boolean isClosedDomain() {
        return isClosedDomain;
    }

    public EntityField getField(String fieldName) throws EntityFieldNotFoundException {
        fieldName = fieldName.toLowerCase();
        EntityField idField = getIdEntityField();
        if (idField.getName().toLowerCase().equals(fieldName)) {
            return idField; // id is a special field
        }
        EntityField entityField = dataFieldsByName.get(fieldName);
        if (entityField == null) {
            throw EntityFieldException.ENTITYFIELD_NOT_FOUND(this, fieldName);
        }
        return entityField;
    }

    public EntityField getMetaField(String fieldName) throws EntityMetaFieldNotFoundException {
        fieldName = fieldName.toLowerCase();
        EntityField entityField = metaFieldsByName.get(fieldName);
        if (entityField == null) {
            throw EntityFieldException.ENTITYMETAFIELD_NOT_FOUND(this, fieldName);
        }
        return entityField;
    }

    @Nullable // TODO this method probably may be removed
    public EntityRecord getDefaultEntityRecord() {
        return RecordConverters.entityRecordFromMap(this, copyDefaultRecord());
    }

    public Set<EntityField> getAllRootEntityFields() {
        return Stream.concat(dataFields.stream(), metaFields.stream()).collect(collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }


    public Set<EntityField> getDataEntityFields() {
        return Collections.unmodifiableSet(dataFields);
    }

    public Set<EntityField> getMetaEntityFields() {
        return Collections.unmodifiableSet(metaFields);
    }

    public LogicalKey getLogicalKey() {
        return logicalKey;
    }

    @NotNull
    public EntityField getIdEntityField() {
        return idField;
    }

    public List<String> getImplementsIntefaces() {
        return implementsIntefaces;
    }

    /* TODO runtime entities ?
    public void addField(EntityField entityField) throws EntityFieldException {
        if (this.dataFieldsByName.containsKey(entityField.getName().toLowerCase())) {
            throw EntityFieldException.ENTITYFIELD_ALREADY_FOUND(entityField);
        }
        this.dataFields.add(entityField);
        this.dataFieldsByName.put(entityField.getName().toLowerCase(), entityField);
    }

    public void removeField(EntityField entityField) throws EntityFieldException {
        String key = entityField.getName().toLowerCase();
        EntityField ef = dataFieldsByName.get(key);
        if (ef == null) {
            throw EntityFieldException.ENTITYFIELD_NOT_FOUND(entityField);
        }
        this.dataFields.remove(ef);
        this.dataFieldsByName.remove(key);
    }
    */

    public EntityRecord toInitializationEntityRecord() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        values.put("module", module.getName());
        values.put("embedable", embedable);
        values.put("onerecord", oneRecord);
        values.put("tree", tree);
        Entity entity = Services.getSchemaManager().getEntity(ENTITY);
        assert entity != null;
        return RecordConverters.entityRecordFromMap(entity, values);
    }

    public void setFieldIDValue(Object idValue) {
        this.idValue = idValue;
    }

    private LogicalKey extractLogicalKeyFrom(Set<EntityField> fields) {
        return new LogicalKey(fields.stream().filter(EntityField::isLogicalKey).sorted(comparing(EntityField::getLkOrder))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList)));
    }

    @Nullable
    public Object getIDValue() {
        return idValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(module, entity.module) &&
                Objects.equals(name, entity.name);
    }

    @Override
    public int hashCode() {
        // no ID Value / Fields in Equals
        return Objects.hash(module, name, embedable);
    }

    private Map<String, Object> copyDefaultRecord() {
        return copyRecordInner(defaultRecord);
    }

    private Map<String, Object> copyRecordInner(Map<String, Object> record) {
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> elem : record.entrySet()) {
            if (Map.class.isAssignableFrom(elem.getValue().getClass()))
                ret.put(elem.getKey(), copyRecordInner((Map<String, Object>) elem.getValue()));
            else ret.put(elem.getKey(), elem.getValue());
        }
        return ret;
    }

    public void actualEntityRecord(EntityRecord e) {
        this.actualEntityRecord = e;
        this.isClosedDomain = e.getFieldOrDefault(EntityRef.FIELDS.CLOSED_DOMAIN, false);
        if (idValue == null) {
            idValue = e.getID();
        }
    }

    /**
     * @return The entity record of this Entity.
     */
    @Nullable
    public EntityRecord getActualEntityRecord() {
        return actualEntityRecord;
    }


    public class LogicalKey {

        private final List<EntityField> logicalKeyList;

        LogicalKey(List<EntityField> logicalKeyList) {
            Assert.notNull(logicalKeyList, String.format("%s: logical Key Must be not NULL", name));
            this.logicalKeyList = logicalKeyList;
        }

        public Set<EntityField> getLogicalKeySet() {
            return Collections.unmodifiableSet(new HashSet<>(logicalKeyList));
        }

        public List<EntityField> getLogicalKeyList() {
            return logicalKeyList;
        }

        public boolean isEmpty() {
            return logicalKeyList.isEmpty();
        }
    }
}
