export class EntityRecord {
    private _meta: object;
    private _data: any;

    constructor() {

    }

    get meta(): object {
        return this._meta;
    }

    get data(): any{
        return this._data;
    }


}

export class EntityRecordList {
    meta: object;
    data: EntityRecord[];
}
