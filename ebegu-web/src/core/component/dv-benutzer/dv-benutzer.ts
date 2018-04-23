/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {IComponentOptions, IFilterService, IFormController, ILogService} from 'angular';
import {IStateService} from 'angular-ui-router';
import {Moment} from 'moment';
import * as moment from 'moment';
import {IBenutzerStateParams} from '../../../admin/admin.route';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import {getTSRoleValues, getTSRoleValuesWithoutSuperAdmin, rolePrefix, TSRole} from '../../../models/enums/TSRole';
import TSInstitution from '../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import TSUser from '../../../models/TSUser';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import GesuchsperiodeRS from '../../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../service/institutionRS.rest';
import {TraegerschaftRS} from '../../service/traegerschaftRS.rest';
import UserRS from '../../service/userRS.rest';
import ITranslateService = angular.translate.ITranslateService;

let removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');
let template = require('./dv-benutzer.html');
require('./dv-benutzer.less');

export class DVBenutzerConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = DVBenutzerController;
    controllerAs = 'vm';
}

export class DVBenutzerController {

    form: IFormController;
    TSRoleUtil: any;

    institutionenList: TSInstitution[];
    traegerschaftenList: TSTraegerschaft[];
    minDate: Moment = moment(moment.now());

    selectedUser: TSUser;
    roleBisher: TSRole;
    checkRolleBeenden: boolean;

    static $inject: any[] = ['EbeguUtil', '$filter', '$log', 'InstitutionRS', 'TraegerschaftRS', 'GesuchsperiodeRS', 'CONSTANTS', 'AuthServiceRS',
        '$window', '$translate', '$stateParams', 'UserRS', '$state', 'DvDialog'];
    /* @ngInject */
    constructor(private ebeguUtil: EbeguUtil, private $filter: IFilterService, private $log: ILogService,
                private institutionRS: InstitutionRS, private traegerschaftenRS: TraegerschaftRS, private gesuchsperiodeRS: GesuchsperiodeRS,
                private CONSTANTS: any, private authServiceRS: AuthServiceRS, private $window: ng.IWindowService, private $translate: ITranslateService,
                private $stateParams: IBenutzerStateParams, private userRS: UserRS, private $state: IStateService, private dvDialog: DvDialog) {

        this.TSRoleUtil = TSRoleUtil;
    }

    $onInit() {
        this.updateInstitutionenList();
        this.updateTraegerschaftenList();
        if (this.$stateParams.benutzerId) {
           this.userRS.findBenutzer(this.$stateParams.benutzerId).then((result) => {
               this.selectedUser = result;
               this.roleBisher = result.role;
               this.checkRolleBeenden = EbeguUtil.isNotNullOrUndefined(this.selectedUser.roleGueltigBis);
           });
        }
    }

    private updateInstitutionenList(): void {
        this.institutionRS.getAllInstitutionen().then((response: any) => {
            this.institutionenList = angular.copy(response);
        });
    }

    private updateTraegerschaftenList(): void {
        this.traegerschaftenRS.getAllTraegerschaften().then((response: any) => {
            this.traegerschaftenList = angular.copy(response);
        });
    }

    public getRollen(): Array<TSRole> {
        if (this.authServiceRS.isRole(TSRole.SUPER_ADMIN)) {
            return getTSRoleValues();
        }
        return getTSRoleValuesWithoutSuperAdmin();
    }

    public getTranslatedRole(role: TSRole): string {
        return this.$translate.instant(rolePrefix() + role);
    }

    public showInstitutionenList(): boolean {
        if (this.selectedUser) {
            return this.selectedUser.role === TSRole.SACHBEARBEITER_INSTITUTION;
        }
        return false;
    }

    public showTraegerschaftenList(): boolean {
        if (this.selectedUser) {
            return this.selectedUser.role === TSRole.SACHBEARBEITER_TRAEGERSCHAFT;
        }
        return false;
    }

    saveBenutzer(): void {
        if (this.form.$valid) {
            if (this.hasRoleChanged() && this.isMoreThanGesuchstellerRole()) {
                this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                    title: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_TITLE',
                    deleteText: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_TEXT',
                    parentController: undefined,
                    elementID: undefined
                }).then(() => {
                    if (this.isAdminRole()) {
                        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                            title: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_ADMIN_TITLE',
                            deleteText: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_ADMIN_TEXT',
                            parentController: undefined,
                            elementID: undefined
                        }).then(() => {
                            this.doSaveBenutzer();
                        });
                    } else {
                        this.doSaveBenutzer();
                    }
                });
            } else {
                this.doSaveBenutzer();
            }
        }
    }

    private hasRoleChanged() {
        return this.roleBisher !== this.selectedUser.role;
    }

    private isAdminRole() {
        return TSRoleUtil.getAdministratorRoles().indexOf(this.selectedUser.role) > -1;
    }

    private isMoreThanGesuchstellerRole() {
        return TSRoleUtil.getAllRolesButGesuchsteller().indexOf(this.selectedUser.role) > -1;
    }

    private doSaveBenutzer(): void {
        this.clearBenutzerObject(this.selectedUser);
        this.userRS.saveBenutzer(this.selectedUser).then((changedUser: TSUser) => {
            this.navigateBackToUsersList();
        });
    }

    inactivateBenutzer(): void {
        if (this.form.$valid) {
            this.userRS.inactivateBenutzer(this.selectedUser).then((changedUser: TSUser) => {
                this.selectedUser = changedUser;
            });
        }
    }

    reactivateBenutzer(): void {
        if (this.form.$valid) {
            this.userRS.reactivateBenutzer(this.selectedUser).then((changedUser: TSUser) => {
                this.selectedUser = changedUser;
            });
        }
    }

    cancel(): void {
        this.navigateBackToUsersList();
    }

    private navigateBackToUsersList() {
        this.$state.go('benutzerlist');
    }

    private clearBenutzerObject(benutzer: TSUser): void {
        // Wenn das Flag "Rolle beenden" nicht mehr gesetzt ist, muss das Datum gelöscht werden
        if (!this.checkRolleBeenden) {
            benutzer.roleGueltigBis = null;
        }
        // Es darf nur eine Institution gesetzt sein, wenn die Rolle INSTITUTION ist
        if (benutzer.role !== TSRole.SACHBEARBEITER_INSTITUTION) {
            benutzer.institution = null;
        }
        // Es darf nur eine Trägerschaft gesetzt sein, wenn die Rolle TRAEGERSCHAFT ist
        if (benutzer.role !== TSRole.SACHBEARBEITER_TRAEGERSCHAFT) {
            benutzer.traegerschaft = null;
        }
        // Das Datum gueltigBis sollte bei Rolle GESUCHSTELLER nicht gesetzt werden
        if (benutzer.role === TSRole.GESUCHSTELLER) {
            benutzer.roleGueltigBis = null;
        }
    }

    public showRolleBeenden(): boolean {
        return this.selectedUser.role !== TSRole.GESUCHSTELLER;
    }

    public showGueltigBis(): boolean {
        return this.showRolleBeenden() && this.checkRolleBeenden;
    }

}