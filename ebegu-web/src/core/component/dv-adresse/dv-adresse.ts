/// <reference path="../../../../typings/browser.d.ts" />
module app.DvAdresse {
    'use strict';

    export class AdresseComponentConfig implements angular.IComponentOptions {
        transclude: boolean;
        bindings: any;
        templateUrl: string | Function;
        controller: any;
        controllerAs: string;
        require: any;

        constructor() {
            this.transclude = false;
            this.bindings = {adresse:'<'};
            this.require = {parentForm: '?^form'};
            this.templateUrl = 'src/core/component/dv-adresse/dv-adresse.html';
            this.controller = DvAdresseController;
            this.controllerAs = 'vm';
        }
    }


    export class DvAdresseController  {
        adresse:ebeguWeb.API.TSAdresse;
        adresseRS:ebeguWeb.services.IAdresseRS;
        showDate:boolean;
        parentForm: angular.IFormController;

        static $inject = ['adresseRS'];
        /* @ngInject */
        constructor(adresseRS:ebeguWeb.services.IAdresseRS) {
            this.adresseRS = adresseRS;
            this.showDate = false;
        }

        submit () {
            this.adresseRS.create(this.adresse)
                .then((response) => {
                    if (response.status === 201) {
                        this.resetForm();
                    }
                });
        }

        removeRow(row:any) {
            //this.adresseRS.remove(row.name).then((reponse:angular.IHttpPromiseCallbackArg<any>) => {
            //    var index = this.adressen.indexOf(row);
            //    if (index !== -1) {
            //        this.adressen.splice(index, 1);
            //        this.resetForm();
            //    }
            //});
        }

        createItem() {
            this.adresse = new ebeguWeb.API.TSAdresse('', '', '', '', '', '', '', undefined, undefined);
        }

        resetForm() {
            this.adresse = undefined;
        }

    }

    angular.module('ebeguWeb.core').component('dvAdresse', new AdresseComponentConfig());

}