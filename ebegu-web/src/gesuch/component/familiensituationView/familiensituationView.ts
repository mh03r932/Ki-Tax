/// <reference path="../../../../typings/browser.d.ts" />
/// <reference path="../../component/abstractGesuchView.ts" />
/// <reference path="../../../models/TSFamiliensituation.ts" />
/// <reference path="../../service/familiensituationRS.rest.ts" />
module ebeguWeb.FamiliensituationView {
    import EnumEx = ebeguWeb.utils.EnumEx;
    import AbstractGesuchViewController = ebeguWeb.GesuchView.AbstractGesuchViewController;

    import TSFall = ebeguWeb.API.TSFall;
    import TSGesuch = ebeguWeb.API.TSGesuch;
    import TSFamiliensituation = ebeguWeb.API.TSFamiliensituation;

    import IFallRS = ebeguWeb.services.IFallRS;
    import IGesuchRS = ebeguWeb.services.IGesuchRS;
    import IFamiliensituationRS = ebeguWeb.services.IFamiliensituationRS;

    import TSFamilienstatus = ebeguWeb.API.TSFamilienstatus;
    import TSGesuchKardinalitaet = ebeguWeb.API.TSGesuchKardinalitaet;
    'use strict';

    class FamiliensituationViewComponentConfig implements angular.IComponentOptions {
        transclude: boolean;
        bindings: any;
        templateUrl: string | Function;
        controller: any;
        controllerAs: string;

        constructor() {
            this.transclude = false;
            this.bindings = {};
            this.templateUrl = 'src/gesuch/component/familiensituationView/familiensituationView.html';
            this.controller = FamiliensituationViewController;
            this.controllerAs = 'vm';
        }
    }


    class FamiliensituationViewController extends AbstractGesuchViewController {
        fall: TSFall;
        gesuch: TSGesuch;
        familiensituation: TSFamiliensituation;
        fallRS: IFallRS;
        gesuchRS: IGesuchRS;
        familiensituationRS: IFamiliensituationRS;
        familienstatusValues: Array<TSFamilienstatus>;
        gesuchKardinalitaetValues: Array<TSGesuchKardinalitaet>;

        static $inject = ['$state', 'familiensituationRS', 'fallRS', 'gesuchRS'];
        /* @ngInject */
        constructor($state: angular.ui.IStateService, familiensituationRS: IFamiliensituationRS,
                    fallRS: IFallRS, gesuchRS: IGesuchRS) {
            super($state);
            this.fall = new TSFall();
            this.gesuch = new TSGesuch();
            this.familiensituation = new TSFamiliensituation();
            this.fallRS = fallRS;
            this.gesuchRS = gesuchRS;
            this.familiensituationRS = familiensituationRS;
            this.familienstatusValues = ebeguWeb.API.getTSFamilienstatusValues();
            this.gesuchKardinalitaetValues = ebeguWeb.API.getTSGesuchKardinalitaetValues();
        }

        submit ($form: angular.IFormController) {
            if ($form.$valid) {
                //testen ob aktuelles familiensituation schon gespeichert ist
                if (this.familiensituation.timestampErstellt) {
                    this.fallRS.update(this.fall); //todo imanol id holen und dem gesuch geben
                    this.gesuchRS.update(this.gesuch);//todo imanol id holen und der Familiensituation geben
                    this.familiensituationRS.update(this.familiensituation);
                } else {
                    //todo team. Fall und Gesuch sollten in ihren eigenen Services gespeichert werden
                    this.fallRS.create(this.fall).then((fallResponse: any) => {
                        if (!(fallResponse.data instanceof Array)) {
                            this.gesuch.fall = fallResponse.data;
                            this.gesuchRS.create(this.gesuch).then((gesuchResponse: any) => {
                                if (!(gesuchResponse.data instanceof Array)) {
                                    console.log('PREV:'+this.familiensituation.gesuch);
                                    this.familiensituation.gesuch = gesuchResponse.data;
                                    console.log('POST:'+this.familiensituation.gesuch);
                                    this.familiensituationRS.create(this.familiensituation);
                                }
                            });
                        }
                    });
                }
                this.state.go("gesuch.stammdaten");
            }
        }

        showGesuchKardinalitaet(): boolean {
            return this.familiensituation.familienstatus === TSFamilienstatus.ALLEINERZIEHEND
                || this.familiensituation.familienstatus === TSFamilienstatus.WENIGER_FUENF_JAHRE;
        }

    }

    angular.module('ebeguWeb.gesuch').component('familiensituationView', new FamiliensituationViewComponentConfig());

}
