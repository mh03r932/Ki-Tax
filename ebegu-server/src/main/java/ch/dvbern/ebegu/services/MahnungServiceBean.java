package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mahnung_;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.poi.ss.formula.functions.T;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Service fuer Mahnungen
 */
@Stateless
@Local(MahnungService.class)
public class MahnungServiceBean extends AbstractBaseService implements MahnungService {

	@Inject
	private Persistence<Mahnung> persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;


	@Override
	@Nonnull
	public Mahnung createMahnung(@Nonnull Mahnung mahnung) {
		Objects.requireNonNull(mahnung);
		if (MahnungTyp.ZWEITE_MAHNUNG.equals(mahnung.getMahnungTyp())) {
			// Die Vorgaenger-Mahnung suchen und verknuepfen, wird im Dokument gebraucht
			Optional<Mahnung> erstMahnung = findAktiveErstMahnung();
			if (erstMahnung.isPresent()) {
				mahnung.setVorgaengerId(erstMahnung.get().getId());
			} else {
				throw new IllegalArgumentException("Zweitmahnung erstellt ohne aktive Erstmahnung!");
			}
		}
		return persistence.persist(mahnung);
	}

	@Override
	@Nonnull
	public Collection<Mahnung> findMahnungenForGesuch(@Nonnull Gesuch gesuch) {
		return criteriaQueryHelper.getEntitiesByAttribute(Mahnung.class, gesuch, Mahnung_.gesuch);
	}

	@Override
	public void dokumenteKomplettErhalten(@Nonnull Gesuch gesuch) {
		// Alle Mahnungen auf erledigt stellen
		Collection<Mahnung> mahnungenForGesuch = findMahnungenForGesuch(gesuch);
		for (Mahnung mahnung : mahnungenForGesuch) {
			mahnung.setActive(false);
			persistence.persist(mahnung);
		}
	}

	@Nonnull
	private Optional<Mahnung> findAktiveErstMahnung() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mahnung> query = cb.createQuery(Mahnung.class);
		Root<Mahnung> root = query.from(Mahnung.class);
		query.select(root);
		Predicate predicateTyp = cb.equal(root.get(Mahnung_.mahnungTyp), MahnungTyp.ERSTE_MAHNUNG);
		Predicate predicateAktiv = cb.equal(root.get(Mahnung_.active), Boolean.TRUE);
		query.where(predicateTyp, predicateAktiv);
		// Wirft eine NonUnique-Exception, falls mehrere aktive ErstMahnungen!
		Mahnung aktiveErstMahnung = persistence.getCriteriaSingleResult(query);
		return Optional.of(aktiveErstMahnung);
	}
}
