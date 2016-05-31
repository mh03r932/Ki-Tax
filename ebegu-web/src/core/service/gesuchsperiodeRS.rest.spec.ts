import GesuchsperiodeRS from './gesuchsperiodeRS.rest';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {EbeguWebCore} from '../core.module';
import {IHttpBackendService} from 'angular';
import TSGesuchsperiode from '../../models/TSGesuchsperiode';
import {TSDateRange} from '../../models/types/TSDateRange';
import DateUtil from '../../utils/DateUtil';
import TestDataUtil from '../../utils/TestDataUtil';

describe('gesuchsperiodeRS', function () {

    let gesuchsperiodeRS: GesuchsperiodeRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockGesuchsperiode: TSGesuchsperiode;
    let mockGesuchsperiodeRest: any;
    let date: moment.Moment;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.inject(function ($injector: any) {
        gesuchsperiodeRS = $injector.get('GesuchsperiodeRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        date = DateUtil.today();
        mockGesuchsperiode = new TSGesuchsperiode(true, new TSDateRange(date, date));
        TestDataUtil.setAbstractFieldsUndefined(mockGesuchsperiode);
        mockGesuchsperiodeRest = ebeguRestUtil.gesuchsperiodeToRestObject({}, mockGesuchsperiode);
    });

    describe('Public API', function () {
        it('check URI', function () {
            expect(gesuchsperiodeRS.serviceURL).toContain('gesuchsperioden');
        });
        it('check Service name', function () {
            expect(gesuchsperiodeRS.getServiceName()).toBe('GesuchsperiodeRS');
        });
        it('should include a findGesuchsperiode() function', function () {
            expect(gesuchsperiodeRS.findGesuchsperiode).toBeDefined();
        });
        it('should include a createGesuchsperiode() function', function () {
            expect(gesuchsperiodeRS.createGesuchsperiode).toBeDefined();
        });
        it('should include a updateGesuchsperiode() function', function () {
            expect(gesuchsperiodeRS.updateGesuchsperiode).toBeDefined();
        });
        it('should include a removeGesuchsperiode() function', function () {
            expect(gesuchsperiodeRS.removeGesuchsperiode).toBeDefined();
        });
    });

    describe('API Usage', function () {
        describe('findGesuchsperiode', () => {
            it('should return the Gesuchsperiode by id', () => {
                $httpBackend.expectGET(gesuchsperiodeRS.serviceURL + '/' + encodeURIComponent(mockGesuchsperiode.id)).respond(mockGesuchsperiodeRest);

                let foundGesuchsperiode: TSGesuchsperiode;
                gesuchsperiodeRS.findGesuchsperiode(mockGesuchsperiode.id).then((result) => {
                    foundGesuchsperiode = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundGesuchsperiode, mockGesuchsperiode, true);
            });

        });
        describe('createGesuchsperiode', () => {
            it('should create a gesuchsperiode', () => {
                let createdGesuchsperiode: TSGesuchsperiode;
                $httpBackend.expectPUT(gesuchsperiodeRS.serviceURL, mockGesuchsperiodeRest).respond(mockGesuchsperiodeRest);

                gesuchsperiodeRS.createGesuchsperiode(mockGesuchsperiode)
                    .then((result) => {
                        createdGesuchsperiode = result;
                    });
                $httpBackend.flush();
                checkFieldValues(createdGesuchsperiode, mockGesuchsperiode, true);
            });
        });
        describe('updateGesuchsperiode', () => {
            it('should update a gesuchsperiode', () => {
                mockGesuchsperiode.active = false;
                mockGesuchsperiodeRest = ebeguRestUtil.gesuchsperiodeToRestObject({}, mockGesuchsperiode);
                let updatedGesuchsperiode: TSGesuchsperiode;
                $httpBackend.expectPUT(gesuchsperiodeRS.serviceURL, mockGesuchsperiodeRest).respond(mockGesuchsperiodeRest);

                gesuchsperiodeRS.updateGesuchsperiode(mockGesuchsperiode)
                    .then((result) => {
                        updatedGesuchsperiode = result;
                    });
                $httpBackend.flush();
                checkFieldValues(updatedGesuchsperiode, mockGesuchsperiode, false);
            });
        });
        describe('removeGesuchsperiode', () => {
            it('should remove a gesuchsperiode', () => {
                $httpBackend.expectDELETE(gesuchsperiodeRS.serviceURL + '/' + mockGesuchsperiode.id)
                    .respond(200);

                let deleteResult: any;
                gesuchsperiodeRS.removeGesuchsperiode(mockGesuchsperiode.id)
                    .then((result) => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(200);
            });
        });
    });

    function checkFieldValues(createdGesuchsperiode: TSGesuchsperiode, mockGesuchsperiode: TSGesuchsperiode, active: boolean) {
        expect(createdGesuchsperiode).toBeDefined();
        expect(createdGesuchsperiode.active).toBe(active);
        TestDataUtil.checkGueltigkeitAndSetIfSame(createdGesuchsperiode, mockGesuchsperiode);
    }
});
