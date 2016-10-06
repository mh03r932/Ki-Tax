import {IComponentOptions} from 'angular';
import UserRS from '../../../core/service/userRS.rest';
import TSUser from '../../../models/TSUser';
import EbeguUtil from '../../../utils/EbeguUtil';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSGesuch from '../../../models/TSGesuch';
import GesuchRS from '../../service/gesuchRS.rest';
import {IStateService} from 'angular-ui-router';
import TSAntragDTO from '../../../models/TSAntragDTO';
import {IGesuchStateParams} from '../../gesuch.route';
import Moment = moment.Moment;
import ITranslateService = angular.translate.ITranslateService;
import IScope = angular.IScope;
let template = require('./gesuchToolbar.html');
require('./gesuchToolbar.less');

export class GesuchToolbarComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {
        gesuchid: '@'
    };

    template = template;
    controller = GesuchToolbarController;
    controllerAs = 'vm';
}

export class GesuchToolbarController {

    userList: Array<TSUser>;
    antragList: Array<TSAntragDTO>;
    gesuchid: string;
    gesuch: TSGesuch;


    gesuchsperiodeList: { [key: string]: Array<TSAntragDTO> } = {};
    antragTypList: { [key: string]: TSAntragDTO } = {};

    static $inject = ['UserRS', 'EbeguUtil', 'CONSTANTS', 'GesuchRS',
        '$state', '$stateParams', '$scope'];

    constructor(private userRS: UserRS, private ebeguUtil: EbeguUtil,
                private CONSTANTS: any, private gesuchRS: GesuchRS,
                private $state: IStateService, private $stateParams: IGesuchStateParams, private $scope: IScope) {
        $scope.$watch(() => {
            return this.gesuchid;
        }, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                if (this.gesuchid) {
                    gesuchRS.findGesuch(this.gesuchid).then((gesuchResponse: any) => {
                        this.gesuch = gesuchResponse;
                        this.updateAntragDTOList();
                        console.log('watch on gesuchId');
                    });
                } else {
                    this.gesuch = null;
                }
            }
        });
    }


    public getVerantwortlicherFullName(): string {
        if (this.gesuch && this.gesuch.fall && this.gesuch.fall.verantwortlicher) {
            return this.gesuch.fall.verantwortlicher.getFullName();
        }
        return '';
    }

    public updateUserList(): void {
        this.userRS.getAllUsers().then((response) => {
            this.userList = angular.copy(response);
        });
    }

    public updateAntragDTOList(): void {
        if (this.gesuch && this.gesuch.id) {
            this.gesuchRS.getAllAntragDTOForFall(this.gesuch.fall.id).then((response) => {
                this.antragList = angular.copy(response);
                this.updateGesuchperiodeList();
                this.updateAntragTypList();
            });
        }
    }

    private updateGesuchperiodeList() {

        for (var i = 0; i < this.antragList.length; i++) {
            let gs = this.antragList[i].gesuchsperiodeString;

            if (!this.gesuchsperiodeList[gs]) {
                this.gesuchsperiodeList[gs] = [];
            }
            this.gesuchsperiodeList[gs].push(this.antragList[i]);
        }
    }

    private updateAntragTypList() {
        for (var i = 0; i < this.antragList.length; i++) {
            let antrag = this.antragList[i];
            if (this.gesuch.gesuchsperiode.gueltigkeit.gueltigAb.isSame(antrag.gesuchsperiodeGueltigAb)) {
                let txt = this.ebeguUtil.getAntragTextDateAsString(antrag.antragTyp, antrag.eingangsdatum);

                this.antragTypList[txt] = antrag;
            }
        }
    }

    getKeys(map: { [key: string]: Array<TSAntragDTO> }): Array<String> {
        var keys: Array<String> = [];
        for (var key in map) {
            if (map.hasOwnProperty(key)) {
                keys.push(key);
            }
        }
        return keys;
    }

    /**
     * Sets the given user as the verantworlicher fuer den aktuellen Fall
     * @param verantwortlicher
     */
    public setVerantwortlicher(verantwortlicher: TSUser): void {
        // if (verantwortlicher) {
        //     this.gesuchModelManager.setUserAsFallVerantwortlicher(verantwortlicher);
        //     this.gesuchModelManager.updateFall();
        // }
        //TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    /**
     *
     * @param user
     * @returns {boolean} true if the given user is already the verantwortlicher of the current fall
     */
    public isCurrentVerantwortlicher(user: TSUser): boolean {
        return (user && this.getFallVerantwortlicher() && this.getFallVerantwortlicher().username === user.username);
    }

    public getFallVerantwortlicher(): TSUser {
        if (this.gesuch && this.gesuch.fall) {
            return this.gesuch.fall.verantwortlicher;
        }
        return undefined;
    }

    public getGesuchName(): string {
        if (this.gesuch) {
            var text = this.ebeguUtil.addZerosToNumber(this.gesuch.fall.fallNummer, this.CONSTANTS.FALLNUMMER_LENGTH);
            if (this.gesuch.gesuchsteller1 && this.gesuch.gesuchsteller1.nachname) {
                text = text + ' ' + this.gesuch.gesuchsteller1.nachname;
            }
            return text;
        } else {
            return '--';
        }
    }

    public getGesuch(): TSGesuch {
        return this.gesuch;
    }

    public getCurrentGesuchsperiode(): string {

        if (this.gesuch && this.gesuch.gesuchsperiode) {
            return this.getGesuchsperiodeAsString(this.gesuch.gesuchsperiode);
        } else {
            return '--';
        }
    }

    public getAntragTyp(): string {
        if (this.gesuch) {
            return this.ebeguUtil.getAntragTextDateAsString(this.gesuch.typ, this.gesuch.eingangsdatum);
        } else {
            return '';
        }
    }

    public getAntragDatum(): Moment {
        if (this.gesuch && this.gesuch.eingangsdatum) {
            return this.gesuch.eingangsdatum;
        } else {
            return moment();
        }
    }

    public getGesuchsperiodeAsString(tsGesuchsperiode: TSGesuchsperiode) {
        return tsGesuchsperiode.gesuchsperiodeString;
    }

    public setGesuchsperiode(gesuchsperiodeKey: string) {
        let selectedGesuche = this.gesuchsperiodeList[gesuchsperiodeKey];
        let selectedGesuch: TSAntragDTO = this.getNewest(selectedGesuche);

        this.goToOpenGesuch(selectedGesuch.antragId);
    }

    private getNewest(arrayTSAntragDTO: Array<TSAntragDTO>): TSAntragDTO {
        let newest: TSAntragDTO = arrayTSAntragDTO[0];
        for (var i = 0; i < arrayTSAntragDTO.length; i++) {
            if (arrayTSAntragDTO[i].eingangsdatum.isAfter(newest.eingangsdatum)) {
                newest = arrayTSAntragDTO[i];
            }
        }
        return newest;
    }

    private goToOpenGesuch(gesuchId: string): void {
        if (gesuchId) {
            this.$state.go('gesuch.fallcreation', {createNew: false, gesuchId: gesuchId});
        }
    }


    public setAntragTypDatum(antragTypDatumKey: string) {
        let selectedAntragTypGesuch = this.antragTypList[antragTypDatumKey];
        this.goToOpenGesuch(selectedAntragTypGesuch.antragId);
    }

}
