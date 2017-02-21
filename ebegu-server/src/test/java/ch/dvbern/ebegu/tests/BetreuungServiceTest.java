package ch.dvbern.ebegu.tests;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Tests fuer die Klasse betreuungService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class BetreuungServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private Persistence<Gesuch> persistence;
	@Inject
	private KindService kindService;



	@Test
	public void createAndUpdateBetreuungTest() {
		Assert.assertNotNull(betreuungService);
		Betreuung persitedBetreuung = TestDataUtil.persistBetreuung(betreuungService, persistence);
		Optional<Betreuung> betreuungOpt = betreuungService.findBetreuungWithBetreuungsPensen(persitedBetreuung.getId());
		Assert.assertTrue(betreuungOpt.isPresent());
		Betreuung betreuung = betreuungOpt.get();
		Assert.assertEquals(persitedBetreuung.getBetreuungsstatus(), betreuung.getBetreuungsstatus());

		betreuungService.saveBetreuung(betreuung, false);
		Optional<Betreuung> updatedBetreuung = betreuungService.findBetreuung(persitedBetreuung.getId());
		Assert.assertTrue(updatedBetreuung.isPresent());

		Assert.assertEquals(new Integer(1), updatedBetreuung.get().getBetreuungNummer());
		Assert.assertEquals(new Integer(2), kindService.findKind(betreuung.getKind().getId()).get().getNextNumberBetreuung());
	}

	@Test
	public void removeBetreuungTest() {
		Assert.assertNotNull(betreuungService);
		Betreuung persitedBetreuung = TestDataUtil.persistBetreuung(betreuungService, persistence);
		Optional<Betreuung> betreuung = betreuungService.findBetreuung(persitedBetreuung.getId());
		Assert.assertTrue(betreuung.isPresent());
		betreuungService.removeBetreuung(betreuung.get().getId());
		Optional<Betreuung> betreuungAfterRemove = betreuungService.findBetreuung(persitedBetreuung.getId());
		Assert.assertFalse(betreuungAfterRemove.isPresent());
	}

}
