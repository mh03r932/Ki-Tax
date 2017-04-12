import {IComponentOptions} from 'angular';
import TSAntragDTO from '../../../models/TSAntragDTO';
import PendenzRS from '../../service/PendenzRS.rest';
import * as moment from 'moment';
import ITimeoutService = angular.ITimeoutService;
import Moment = moment.Moment;
import TSUser from '../../../models/TSUser';
let template = require('./pendenzenListView.html');
require('./pendenzenListView.less');

export class PendenzenListViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = PendenzenListViewController;
    controllerAs = 'vm';
}

export class PendenzenListViewController {

    private pendenzenList: Array<TSAntragDTO>;

    static $inject: string[] = ['PendenzRS', 'CONSTANTS'];

    constructor(public pendenzRS: PendenzRS, private CONSTANTS: any) {
        this.initViewModel();
    }

    private initViewModel() {
        this.updatePendenzenList();
    }

    private updatePendenzenList() {
        this.pendenzRS.getPendenzenList().then((response: any) => {
            this.pendenzenList = angular.copy(response);
        });
    }

    public getPendenzenList(): Array<TSAntragDTO> {
        return this.pendenzenList;
    }

    public userChanged(user: TSUser): void {
        console.log('HERE WE ARE');
        console.log('and the winner is: ' + user.getFullName());
    }
}
