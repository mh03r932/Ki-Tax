import {IComponentOptions} from 'angular';
import TSZahlungsauftrag from '../../models/TSZahlungsauftrag';
import EbeguUtil from '../../utils/EbeguUtil';
import ZahlungRS from '../../core/service/zahlungRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import TSDownloadFile from '../../models/TSDownloadFile';
import ITimeoutService = angular.ITimeoutService;
import IPromise = angular.IPromise;
import ILogService = angular.ILogService;
import IQService = angular.IQService;
import IStateService = angular.ui.IStateService;
import IFormController = angular.IFormController;
let template = require('./zahlungsauftragView.html');
require('./zahlungsauftragView.less');

export class ZahlungsauftragViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = ZahlungsauftragViewController;
    controllerAs = 'vm';
}

export class ZahlungsauftragViewController {

    form: IFormController;
    private zahlungsauftragen: Array<TSZahlungsauftrag>;
    private zahlungsauftragToEdit: TSZahlungsauftrag;

    beschrieb: string;
    faelligkeitsdatum: moment.Moment;
    datumGeneriert: moment.Moment;

    itemsByPage: number = 20;
    numberOfPages: number = 1;

    static $inject: string[] = ['ZahlungRS', 'EbeguUtil', 'CONSTANTS', '$state', 'DownloadRS'];

    constructor(private zahlungRS: ZahlungRS, private ebeguUtil: EbeguUtil, private CONSTANTS: any,
                private $state: IStateService, private downloadRS: DownloadRS) {
        this.initViewModel();
    }

    public getZahlungsauftragen() {
        return this.zahlungsauftragen;
    }

    public addZerosToFallNummer(fallnummer: number): string {
        return this.ebeguUtil.addZerosToNumber(fallnummer, this.CONSTANTS.FALLNUMMER_LENGTH);
    }

    private initViewModel() {
        this.updateZahlungsauftrag();
    }

    private updateZahlungsauftrag() {
        this.zahlungRS.getAllZahlungsauftraege().then((response: any) => {
            this.zahlungsauftragen = angular.copy(response);
            this.numberOfPages = this.zahlungsauftragen.length / this.itemsByPage;
        });
    }

    public gotoZahlung(zahlungsauftrag: TSZahlungsauftrag) {
        //stateparams übergeben
        this.$state.go('zahlung', {
            zahlungsauftrag: zahlungsauftrag,
            zahlungsauftragId: zahlungsauftrag.id
        });
    }

    public createZahlungsauftrag() {
        if (this.form.$valid) {
            this.zahlungRS.createZahlungsauftrag(this.beschrieb, this.faelligkeitsdatum, this.datumGeneriert).then((response: TSZahlungsauftrag) => {
                this.zahlungsauftragen.push(response);
                this.resetEditZahlungsauftrag();
            });
        }
    }

    public downloadPain(zahlungsauftrag: TSZahlungsauftrag) {
        console.log('downloadPain' + zahlungsauftrag.id);
        let win: Window = this.downloadRS.prepareDownloadWindow();
        return this.downloadRS.getPain001AccessTokenGeneratedDokument(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            });
    }

    public downloadAllDetails(zahlungsauftrag: TSZahlungsauftrag) {
        console.log('downloadAllDetails' + zahlungsauftrag.id);
    }

    public ausloesen(zahlungsauftrag: TSZahlungsauftrag) {
        console.log('ausloesen' + zahlungsauftrag.id);
    }

    public edit(zahlungsauftrag: TSZahlungsauftrag) {
        this.zahlungsauftragToEdit = zahlungsauftrag;
    }

    public save(zahlungsauftrag: TSZahlungsauftrag) {
        if (this.isEditValid()) {
            this.zahlungRS.updateZahlungsauftrag(
                this.zahlungsauftragToEdit.beschrieb, this.zahlungsauftragToEdit.datumFaellig, this.zahlungsauftragToEdit.id).then((response: TSZahlungsauftrag) => {
                let index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsauftragen);
                if (index > -1) {
                    this.zahlungsauftragen[index] = response;
                }
                this.form.$setPristine(); // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                this.resetEditZahlungsauftrag();
            });

        }
    }

    public isEditMode(zahlungsauftragId: string): boolean {
        if (this.zahlungsauftragToEdit && this.zahlungsauftragToEdit.id == zahlungsauftragId) {
            return true;
        }
        return false;
    }

    public isEditValid(): boolean {
        if (this.zahlungsauftragToEdit) {
            return this.zahlungsauftragToEdit.beschrieb && this.zahlungsauftragToEdit.beschrieb.length > 0 &&
                this.zahlungsauftragToEdit.datumFaellig != null && this.zahlungsauftragToEdit.datumFaellig != undefined;
        }
        return false;
    }


    private resetEditZahlungsauftrag() {
        this.zahlungsauftragToEdit = null;
    }

    public rowClass(zahlungsauftragId: string) {
        if (this.isEditMode(zahlungsauftragId) && !this.isEditValid()) {
            return "errorrow";
        }
        return "";
    }
}
