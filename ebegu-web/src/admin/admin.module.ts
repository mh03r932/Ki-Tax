import 'angular';
import 'angular-smart-table';
import {EbeguWebCore} from '../core/core.module';
import './admin.module.less';
import {AdminViewComponentConfig} from './component/adminView/adminView';
import {InstitutionViewComponentConfig} from './component/institutionView/institutionView';
import {ParameterViewComponentConfig} from './component/parameterView/parameterView';
import {ApplicationPropertyRS} from './service/applicationPropertyRS.rest';
import {ReindexRS} from './service/reindexRS.rest';
import {adminRun} from './admin.route';
import {InstitutionRS} from '../core/service/institutionRS.rest';
import {EbeguParameterRS} from './service/ebeguParameterRS.rest';
import {TraegerschaftViewComponentConfig} from './component/traegerschaftView/traegerschaftView';
import {EbeguVorlageRS} from './service/ebeguVorlageRS.rest';
import {TestFaelleRS} from './service/testFaelleRS.rest';
import {TestdatenViewComponentConfig} from './component/testdatenView/testdatenView';

export const EbeguWebAdmin = angular.module('ebeguWeb.admin', [EbeguWebCore.name, 'smart-table'])
    .service('ApplicationPropertyRS', ApplicationPropertyRS)
    .service('InstitutionRS', InstitutionRS)
    .service('EbeguParameterRS', EbeguParameterRS)
    .service('EbeguVorlageRS', EbeguVorlageRS)
    .service('ReindexRS', ReindexRS)
    .service('TestFaelleRS', TestFaelleRS)
    .component('dvAdminView', new AdminViewComponentConfig())
    .component('dvInstitutionView', new InstitutionViewComponentConfig())
    .component('dvParameterView', new ParameterViewComponentConfig())
    .component('dvTraegerschaftView', new TraegerschaftViewComponentConfig())
    .component('dvTestdatenView', new TestdatenViewComponentConfig())
    .run(adminRun);

export default EbeguWebAdmin;
