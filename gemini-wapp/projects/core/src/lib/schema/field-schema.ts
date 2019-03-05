export class FieldSchema {
    name: string;
    entity?: any;
    type: FieldType;
    events: FieldEvents;
}

export class FieldEvents {
    visible: FieldControlEvent;
    modifiable: FieldControlEvent;
    required: FieldControlEvent;
    valueEventType: EventType;
}

export class FieldControlEvent {
    eventType: EventType;
    value: boolean
}

export enum EventType {
    NO_EVENT = "NO_EVENT"
}

export enum FieldType {
    TEXT = "TEXT",
    NUMBER = "NUMBER",
    LONG = "LONG",
    DOUBLE = "DOUBLE",
    BOOL = "BOOL",
    TIME = "TIME",
    DATE ="DATE",
    DATETIME = "DATETIME",
    ENTITY_REF = "ENTITY_REF",
    RECORD = "RECORD"
}

