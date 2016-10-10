package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.*;

/**
 * Entitaet zum Speichern von Gesuch in der Datenbank.
 */
@Audited
@Entity
//todo team die FK kann irgendwie nicht ueberschrieben werden. Folgende 2 Moeglichkeiten sollten gehen aber es ueberschreibt den Namen nicht --> Problem mit hibernate-maven-plugin??
//@AssociationOverride(name = "gesuchsperiode", joinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_gesuchsperiode_id")))
//@AssociationOverride(name = "gesuchsperiode", foreignKey = @ForeignKey(name="FK_gesuch_gesuchsperiode_id"))
public class Gesuch extends AbstractAntragEntity {

	private static final long serialVersionUID = -8403487439884700618L;

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_gesuchsteller1_id"))
	private Gesuchsteller gesuchsteller1;

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_gesuchsteller2_id"))
	private Gesuchsteller gesuchsteller2;

	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "gesuch")
	@OrderBy("kindNummer")
	private Set<KindContainer> kindContainers = new LinkedHashSet<>();

	@OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "gesuch", fetch = FetchType.LAZY)
	@OrderBy("datum")
	private Set<AntragStatusHistory> antragStatusHistories = new LinkedHashSet<>();

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_familiensituation_id"))
	private Familiensituation familiensituation;

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_einkommensverschlechterungInfo_id"))
	private EinkommensverschlechterungInfo einkommensverschlechterungInfo;

	@Transient
	private FinanzDatenDTO finanzDatenDTO;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungen;


	public Gesuch() {
	}

	public Gesuch(@Nonnull Gesuch toCopy) {
		this.setFall(toCopy.getFall());
		this.setGesuchsperiode(toCopy.getGesuchsperiode());
		this.setEingangsdatum(LocalDate.now()); //TODO (team) dies sollte dann nicht mehr zwingend sein!
		this.setStatus(AntragStatus.IN_BEARBEITUNG_JA); //TODO (team) abhaengig vom eingeloggten Benutzer!
		this.setTyp(AntragTyp.MUTATION);

		if (toCopy.getGesuchsteller1() != null) {
			this.setGesuchsteller1(new Gesuchsteller(toCopy.getGesuchsteller1()));
		}
		if (toCopy.getGesuchsteller2() != null) {
			this.setGesuchsteller2(new Gesuchsteller(toCopy.getGesuchsteller2()));
		}
		for (KindContainer kindContainer : toCopy.getKindContainers()) {
			this.addKindContainer(new KindContainer(kindContainer, this));
		}
		this.setAntragStatusHistories(new LinkedHashSet<>());
		this.setFamiliensituation(new Familiensituation(toCopy.getFamiliensituation()));
		if (toCopy.getEinkommensverschlechterungInfo() != null) {
			this.setEinkommensverschlechterungInfo(new EinkommensverschlechterungInfo(toCopy.getEinkommensverschlechterungInfo()));
		}
		this.setBemerkungen("Mutation des Gesuchs vom " + toCopy.getEingangsdatum()); //TODO hefr test only!
	}

	@Nullable
	public Gesuchsteller getGesuchsteller1() {
		return gesuchsteller1;
	}

	public void setGesuchsteller1(@Nullable final Gesuchsteller gesuchsteller1) {
		this.gesuchsteller1 = gesuchsteller1;
	}

	@Nullable
	public Gesuchsteller getGesuchsteller2() {
		return gesuchsteller2;
	}

	public void setGesuchsteller2(@Nullable final Gesuchsteller gesuchsteller2) {
		this.gesuchsteller2 = gesuchsteller2;
	}

	public Set<KindContainer> getKindContainers() {
		return kindContainers;
	}

	public void setKindContainers(final Set<KindContainer> kindContainers) {
		this.kindContainers = kindContainers;
	}

	@Nullable
	public Familiensituation getFamiliensituation() {
		return familiensituation;
	}

	public void setFamiliensituation(@Nullable final Familiensituation familiensituation) {
		this.familiensituation = familiensituation;
	}

	public Set<AntragStatusHistory> getAntragStatusHistories() {
		return antragStatusHistories;
	}

	public void setAntragStatusHistories(Set<AntragStatusHistory> antragStatusHistories) {
		this.antragStatusHistories = antragStatusHistories;
	}

	@Nullable
	public EinkommensverschlechterungInfo getEinkommensverschlechterungInfo() {
		return einkommensverschlechterungInfo;
	}

	public void setEinkommensverschlechterungInfo(@Nullable final EinkommensverschlechterungInfo einkommensverschlechterungInfo) {
		this.einkommensverschlechterungInfo = einkommensverschlechterungInfo;
		if (this.einkommensverschlechterungInfo != null) {
			this.einkommensverschlechterungInfo.setGesuch(this);
		}
	}

	public boolean addKindContainer(@NotNull final KindContainer kindContainer) {
		kindContainer.setGesuch(this);
		return !this.kindContainers.contains(kindContainer) && this.kindContainers.add(kindContainer);
	}

	public FinanzDatenDTO getFinanzDatenDTO() {
		return finanzDatenDTO;
	}

	public void setFinanzDatenDTO(FinanzDatenDTO finanzDatenDTO) {
		this.finanzDatenDTO = finanzDatenDTO;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Transient
	public List<Betreuung> extractAllBetreuungen() {
		final List<Betreuung> list = new ArrayList<>();
		for (final KindContainer kind: getKindContainers()) {
			list.addAll(kind.getBetreuungen());
		}
		return list;
	}

	@Transient
	public Betreuung extractBetreuungById(String betreuungId) {
		for (KindContainer kind : getKindContainers()) {
			for (Betreuung betreuung : kind.getBetreuungen()) {
				if (betreuung.getId().equals(betreuungId)) {
					return betreuung;
				}
			}
		}
		return null;
	}

	/**
	 * @return Den Familiennamen beider Gesuchsteller falls es 2 gibt, sonst Familiennamen von GS1
	 */
	@Transient
	public String extractFamiliennamenString(){
		String bothFamiliennamen = (this.getGesuchsteller1() != null ? this.getGesuchsteller1().getNachname() : "");
		bothFamiliennamen += this.getGesuchsteller2() != null ? ", " + this.getGesuchsteller2().getNachname() : "";
		return bothFamiliennamen;

	}
}
