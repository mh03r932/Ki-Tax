import TSAbstractEntity from './TSAbstractEntity';
import TSAdresse from './TSAdresse';

export default class TSAdresseContainer extends TSAbstractEntity {

    private _adresseJA: TSAdresse;
    private _adresseGS: TSAdresse;

    constructor(adresseJA?: TSAdresse, adresseGS?: TSAdresse) {
        super();
        this._adresseGS = adresseGS;
        this._adresseJA = adresseJA;
    }

    get adresseJA(): TSAdresse {
        return this._adresseJA;
    }

    set adresseJA(value: TSAdresse) {
        this._adresseJA = value;
    }

    get adresseGS(): TSAdresse {
        return this._adresseGS;
    }

    set adresseGS(value: TSAdresse) {
        this._adresseGS = value;
    }
}
