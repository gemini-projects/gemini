package it.at7.gemini.dsl.entities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class RawSchema {
    private final Set<RawEntity> rawEntities;
    private final ReadWriteLock schemaLock;
    private final Set<RawEntity> rawEntityInterfaces;

    public RawSchema(Set<RawEntity> rawEntities, Set<RawEntity> rawInterfaces) {
        this.rawEntities = new HashSet<>(rawEntities);
        this.rawEntityInterfaces = rawInterfaces != null ? new HashSet<>(rawInterfaces) : Collections.EMPTY_SET;
        schemaLock = new ReentrantReadWriteLock();
    }

    public RawSchema(Set<RawEntity> rawEntities) {
        this(rawEntities, null);
    }

    public Set<RawEntity> getRawEntities() {
        try {
            schemaLock.readLock().lock();
            return rawEntities;
        } finally {
            schemaLock.readLock().unlock();
        }
    }

    public Set<RawEntity> getRawEntityInterfaces() {
        return rawEntityInterfaces;
    }

    public Map<String, RawEntity> getRawEntitisByName() {
        return getRawEntities().stream().collect(Collectors.toMap(RawEntity::getName, e -> e));
    }

    public void addOrUpdateRawEntity(RawEntity rawEntity) {
        assert rawEntity != null;
        try {
            schemaLock.writeLock().lock();
            String name = rawEntity.getName().toUpperCase();
            rawEntities.removeIf(e -> e.getName().equals(name));
            rawEntities.add(rawEntity);
        } finally {
            schemaLock.writeLock().unlock();
        }
    }

    public void persist(String location) throws IOException {
        StringBuilder stB = new StringBuilder();
        getRawEntities().forEach(e -> {
            stB.append(e.toString());
            stB.append("\n\n");
        });
        File file = new File(location);
        file.delete();
        file.getParentFile().mkdirs();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(stB.toString());
            fileWriter.flush();
        }
    }

}
