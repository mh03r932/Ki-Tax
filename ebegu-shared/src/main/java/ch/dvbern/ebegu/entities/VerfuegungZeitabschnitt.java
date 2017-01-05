package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;

/**
 * Dieses Objekt repraesentiert einen Zeitabschnitt wahrend eines Betreeungsgutscheinantrags waehrend dem die Faktoren
 * die fuer die Berechnung des Gutscheins der Betreuung relevant sind konstant geblieben sind.
 */
@Entity
@Audited
public class VerfuegungZeitabschnitt extends AbstractDateRangedEntity implements Comparable<VerfuegungZeitabschnitt> {

	private static final long serialVersionUID = 7250339356897563374L;

	// Zwischenresulate aus DATA-Rules ("Abschnitt")

	@Transient
	private Integer erwerbspensumGS1 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	private Integer erwerbspensumGS2 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	private int fachstellenpensum;

	@Transient
	private boolean zuSpaetEingereicht;

	@Transient
	private Boolean wohnsitzNichtInGemeindeGS1 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	private Boolean wohnsitzNichtInGemeindeGS2 = null; //es muss by default null sein um zu wissen, wann es nicht definiert wurde

	@Transient
	private boolean kindMinestalterUnterschritten;

	@Transient
	// Wenn Vollkosten bezahlt werden muessen, werden die Vollkosten berechnet und als Elternbeitrag gesetzt
	private boolean bezahltVollkosten;

	@Transient
	private boolean longAbwesenheit;

	@Transient
	private int anspruchspensumRest;

	@Transient
	private boolean hasSecondGesuchsteller;

	@Transient
	private boolean ekv1_alleine;

	@Transient
	private boolean ekv1_zuZweit;

	@Transient
	private boolean ekv2_alleine;

	@Transient
	private boolean ekv2_zuZweit;

	@Transient
	private boolean ekv1_alleine_notAccepted;

	@Transient
	private boolean ekv2_alleine_notAccepted;

	@Transient
	private boolean ekv1_zuZweit_notAccepted;

	@Transient
	private boolean ekv2_zuZweit_notAccepted;


	@Max(100)
	@Min(0)
	@NotNull
	@Column(nullable = false)
	private int betreuungspensum;

	@Max(100)
	@Min(0)
	@NotNull
	@Column(nullable = false)
	private int anspruchberechtigtesPensum; // = Anpsruch für diese Kita, bzw. Tageseltern Kleinkinder

	@Column(nullable = true)
	private BigDecimal betreuungsstunden;

	@Column(nullable = true)
	private BigDecimal vollkosten = ZERO;

	@Column(nullable = true)
	private BigDecimal elternbeitrag = ZERO;

	@Column(nullable = true)
	private BigDecimal abzugFamGroesse = null;

	@Column(nullable = true)
	private BigDecimal famGroesse = null;

	@Column(nullable = true)
	private BigDecimal massgebendesEinkommenVorAbzugFamgr = ZERO;

	@Column(nullable = true)
	private int einkommensjahr;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungen = "";

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegung_zeitabschnitt_verfuegung_id"), nullable = false)
	private Verfuegung verfuegung;

	public VerfuegungZeitabschnitt() {
	}

	/**
	 * copy Konstruktor
	 */
	public VerfuegungZeitabschnitt(VerfuegungZeitabschnitt toCopy) {
		this.setVorgaengerId(toCopy.getId());
		this.setGueltigkeit(new DateRange(toCopy.getGueltigkeit()));
		this.erwerbspensumGS1 = toCopy.erwerbspensumGS1;
		this.erwerbspensumGS2 = toCopy.erwerbspensumGS2;
		this.fachstellenpensum = toCopy.fachstellenpensum;
		this.zuSpaetEingereicht = toCopy.zuSpaetEingereicht;
		this.wohnsitzNichtInGemeindeGS1 = toCopy.wohnsitzNichtInGemeindeGS1;
		this.wohnsitzNichtInGemeindeGS2 = toCopy.wohnsitzNichtInGemeindeGS2;
		this.kindMinestalterUnterschritten = toCopy.kindMinestalterUnterschritten;
		this.bezahltVollkosten = toCopy.bezahltVollkosten;
		this.longAbwesenheit = toCopy.isLongAbwesenheit();
		this.anspruchspensumRest = toCopy.anspruchspensumRest;
		this.betreuungspensum = toCopy.betreuungspensum;
		this.anspruchberechtigtesPensum = toCopy.anspruchberechtigtesPensum;
		this.betreuungsstunden = toCopy.betreuungsstunden;
		this.vollkosten = toCopy.vollkosten;
		this.elternbeitrag = toCopy.elternbeitrag;
		this.abzugFamGroesse = toCopy.abzugFamGroesse;
		this.famGroesse = toCopy.famGroesse;
		this.massgebendesEinkommenVorAbzugFamgr = toCopy.massgebendesEinkommenVorAbzugFamgr;
		this.hasSecondGesuchsteller = toCopy.hasSecondGesuchsteller;
		this.einkommensjahr = toCopy.einkommensjahr;
		this.ekv1_alleine = toCopy.ekv1_alleine;
		this.ekv1_zuZweit = toCopy.ekv1_zuZweit;
		this.ekv2_alleine = toCopy.ekv2_alleine;
		this.ekv2_zuZweit = toCopy.ekv2_zuZweit;
		this.ekv1_alleine_notAccepted = toCopy.ekv1_alleine_notAccepted;
		this.ekv1_zuZweit_notAccepted = toCopy.ekv1_zuZweit_notAccepted;
		this.ekv2_alleine_notAccepted = toCopy.ekv2_alleine_notAccepted;
		this.ekv2_zuZweit_notAccepted = toCopy.ekv2_zuZweit_notAccepted;
		this.bemerkungen = toCopy.bemerkungen;
		this.verfuegung = null;
	}

	/**
	 * Erstellt einen Zeitabschnitt mit der gegebenen gueltigkeitsdauer
	 */
	public VerfuegungZeitabschnitt(DateRange gueltigkeit) {
		this.setGueltigkeit(new DateRange(gueltigkeit));
	}

	public Integer getErwerbspensumGS1() {
		return erwerbspensumGS1;
	}

	public void setErwerbspensumGS1(Integer erwerbspensumGS1) {
		this.erwerbspensumGS1 = erwerbspensumGS1;
	}

	public Integer getErwerbspensumGS2() {
		return erwerbspensumGS2;
	}

	public void setErwerbspensumGS2(Integer erwerbspensumGS2) {
		this.erwerbspensumGS2 = erwerbspensumGS2;
	}

	public int getBetreuungspensum() {
		return betreuungspensum;
	}

	public void setBetreuungspensum(int betreuungspensum) {
		this.betreuungspensum = betreuungspensum;
	}

	public int getFachstellenpensum() {
		return fachstellenpensum;
	}

	public void setFachstellenpensum(int fachstellenpensum) {
		this.fachstellenpensum = fachstellenpensum;
	}

	public int getAnspruchspensumRest() {
		return anspruchspensumRest;
	}

	public void setAnspruchspensumRest(int anspruchspensumRest) {
		this.anspruchspensumRest = anspruchspensumRest;
	}

	public void setAnspruchberechtigtesPensum(int anspruchberechtigtesPensum) {
		this.anspruchberechtigtesPensum = anspruchberechtigtesPensum;
	}

	public BigDecimal getBetreuungsstunden() {
		return betreuungsstunden;
	}

	public void setBetreuungsstunden(BigDecimal betreuungsstunden) {
		this.betreuungsstunden = betreuungsstunden;
	}

	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
	}

	public BigDecimal getElternbeitrag() {
		return elternbeitrag;
	}

	public void setElternbeitrag(BigDecimal elternbeitrag) {
		this.elternbeitrag = elternbeitrag;
	}

	public BigDecimal getAbzugFamGroesse() {
		return abzugFamGroesse;
	}

	public void setAbzugFamGroesse(BigDecimal abzugFamGroesse) {
		this.abzugFamGroesse = abzugFamGroesse;
	}

	/**
	 * @return berechneter Wert. Zieht vom massgebenenEinkommenVorAbzug den Familiengroessen Abzug ab
	 */
	public BigDecimal getMassgebendesEinkommen() {
		return MathUtil.GANZZAHL.subtract(massgebendesEinkommenVorAbzugFamgr,
			this.abzugFamGroesse == null ? BigDecimal.ZERO : this.abzugFamGroesse);
	}


	public BigDecimal getMassgebendesEinkommenVorAbzFamgr() {
		return massgebendesEinkommenVorAbzugFamgr;
	}

	public void setMassgebendesEinkommenVorAbzugFamgr(BigDecimal massgebendesEinkommenVorAbzugFamgr) {
		this.massgebendesEinkommenVorAbzugFamgr = massgebendesEinkommenVorAbzugFamgr;
	}

	public boolean isHasSecondGesuchsteller() {
		return hasSecondGesuchsteller;
	}

	public void setHasSecondGesuchsteller(boolean hasSecondGesuchsteller) {
		this.hasSecondGesuchsteller = hasSecondGesuchsteller;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	public Verfuegung getVerfuegung() {
		return verfuegung;
	}

	public void setVerfuegung(Verfuegung verfuegung) {
		this.verfuegung = verfuegung;
	}

	public boolean isZuSpaetEingereicht() {
		return zuSpaetEingereicht;
	}

	public void setZuSpaetEingereicht(boolean zuSpaetEingereicht) {
		this.zuSpaetEingereicht = zuSpaetEingereicht;
	}

	public boolean isBezahltVollkosten() {
		return bezahltVollkosten;
	}

	public void setBezahltVollkosten(boolean bezahltVollkosten) {
		this.bezahltVollkosten = bezahltVollkosten;
	}

	public boolean isLongAbwesenheit() {
		return longAbwesenheit;
	}

	public void setLongAbwesenheit(boolean longAbwesenheit) {
		this.longAbwesenheit = longAbwesenheit;
	}

	public boolean isWohnsitzNichtInGemeindeGS1() {
		return wohnsitzNichtInGemeindeGS1 != null ? wohnsitzNichtInGemeindeGS1 : true;
	}

	public void setWohnsitzNichtInGemeindeGS1(Boolean wohnsitzNichtInGemeindeGS1) {
		this.wohnsitzNichtInGemeindeGS1 = wohnsitzNichtInGemeindeGS1;
	}

	public boolean isWohnsitzNichtInGemeindeGS2() {
		return wohnsitzNichtInGemeindeGS2 != null ? wohnsitzNichtInGemeindeGS2 : true;
	}

	public void setWohnsitzNichtInGemeindeGS2(Boolean wohnsitzNichtInGemeindeGS2) {
		this.wohnsitzNichtInGemeindeGS2 = wohnsitzNichtInGemeindeGS2;
	}

	public boolean isKindMinestalterUnterschritten() {
		return kindMinestalterUnterschritten;
	}

	public void setKindMinestalterUnterschritten(boolean kindMinestalterUnterschritten) {
		this.kindMinestalterUnterschritten = kindMinestalterUnterschritten;
	}

	public BigDecimal getFamGroesse() {
		return famGroesse;
	}

	public void setFamGroesse(BigDecimal famGroesse) {
		this.famGroesse = famGroesse;
	}

	public int getEinkommensjahr() {
		return einkommensjahr;
	}

	public void setEinkommensjahr(int einkommensjahr) {
		this.einkommensjahr = einkommensjahr;
	}

	public boolean isEkv1_alleine() {
		return ekv1_alleine;
	}

	public void setEkv1_alleine(boolean ekv1_alleine) {
		this.ekv1_alleine = ekv1_alleine;
	}

	public boolean isEkv1_zuZweit() {
		return ekv1_zuZweit;
	}

	public void setEkv1_zuZweit(boolean ekv1_zuZweit) {
		this.ekv1_zuZweit = ekv1_zuZweit;
	}

	public boolean isEkv2_alleine() {
		return ekv2_alleine;
	}

	public void setEkv2_alleine(boolean ekv2_alleine) {
		this.ekv2_alleine = ekv2_alleine;
	}

	public boolean isEkv2_zuZweit() {
		return ekv2_zuZweit;
	}

	public void setEkv2_zuZweit(boolean ekv2_zuZweit) {
		this.ekv2_zuZweit = ekv2_zuZweit;
	}

	public boolean isEkv1_alleine_notAccepted() {
		return ekv1_alleine_notAccepted;
	}

	public void setEkv1_alleine_notAccepted(boolean ekv1_alleine_notAccepted) {
		this.ekv1_alleine_notAccepted = ekv1_alleine_notAccepted;
	}

	public boolean isEkv2_alleine_notAccepted() {
		return ekv2_alleine_notAccepted;
	}

	public void setEkv2_alleine_notAccepted(boolean ekv2_alleine_notAccepted) {
		this.ekv2_alleine_notAccepted = ekv2_alleine_notAccepted;
	}

	public boolean isEkv1_zuZweit_notAccepted() {
		return ekv1_zuZweit_notAccepted;
	}

	public void setEkv1_zuZweit_notAccepted(boolean ekv1_zuZweit_notAccepted) {
		this.ekv1_zuZweit_notAccepted = ekv1_zuZweit_notAccepted;
	}

	public boolean isEkv2_zuZweit_notAccepted() {
		return ekv2_zuZweit_notAccepted;
	}

	public void setEkv2_zuZweit_notAccepted(boolean ekv2_zuZweit_notAccepted) {
		this.ekv2_zuZweit_notAccepted = ekv2_zuZweit_notAccepted;
	}

	/**
	 * Addiert die Daten von "other" zu diesem VerfuegungsZeitabschnitt
	 */
	public void add(VerfuegungZeitabschnitt other) {
		this.setBetreuungspensum(this.getBetreuungspensum() + other.getBetreuungspensum());
		this.setFachstellenpensum(this.getFachstellenpensum() + other.getFachstellenpensum());
		this.setAnspruchspensumRest(this.getAnspruchspensumRest() + other.getAnspruchspensumRest());
		this.setAnspruchberechtigtesPensum(this.getAnspruchberechtigtesPensum() + other.getAnspruchberechtigtesPensum());
		BigDecimal newBetreuungsstunden = ZERO;
		if (this.getBetreuungsstunden() != null) {
			newBetreuungsstunden = newBetreuungsstunden.add(this.getBetreuungsstunden());
		}
		if (other.getBetreuungsstunden() != null) {
			newBetreuungsstunden = newBetreuungsstunden.add(other.getBetreuungsstunden());
		}
		this.setBetreuungsstunden(newBetreuungsstunden);

		if (this.getErwerbspensumGS1() == null && other.getErwerbspensumGS1() == null) {
			this.setErwerbspensumGS1(null);
		}
		else {
			this.setErwerbspensumGS1((this.getErwerbspensumGS1() != null ? this.getErwerbspensumGS1() : 0)
				+ (other.getErwerbspensumGS1() != null ? other.getErwerbspensumGS1() : 0));
		}

		if (this.getErwerbspensumGS2() == null && other.getErwerbspensumGS2() == null) {
			this.setErwerbspensumGS2(null);
		}
		else {
			this.setErwerbspensumGS2((this.getErwerbspensumGS2() != null ? this.getErwerbspensumGS2() : 0) +
				(other.getErwerbspensumGS2() != null ? other.getErwerbspensumGS2() : 0));
		}

		this.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.add(this.getMassgebendesEinkommenVorAbzFamgr(), other.getMassgebendesEinkommenVorAbzFamgr()));

		this.addBemerkung(other.getBemerkungen());
		this.setZuSpaetEingereicht(this.isZuSpaetEingereicht() || other.isZuSpaetEingereicht());

		this.setWohnsitzNichtInGemeindeGS1(this.isWohnsitzNichtInGemeindeGS1() && other.isWohnsitzNichtInGemeindeGS1());
		this.setWohnsitzNichtInGemeindeGS2(this.isWohnsitzNichtInGemeindeGS2() && other.isWohnsitzNichtInGemeindeGS2());

		this.setBezahltVollkosten(this.isBezahltVollkosten() || other.isBezahltVollkosten());

		this.setLongAbwesenheit(this.isLongAbwesenheit() || other.isLongAbwesenheit());

		this.setKindMinestalterUnterschritten(this.isKindMinestalterUnterschritten() || other.isKindMinestalterUnterschritten());
		// Der Familiengroessen Abzug kann nicht linear addiert werden, daher darf es hier nie uebschneidungen geben
		if (other.getAbzugFamGroesse() != null) {
			Validate.isTrue(this.getAbzugFamGroesse() == null, "Familiengoressenabzug kann nicht gemerged werden");
			this.setAbzugFamGroesse(other.getAbzugFamGroesse());
		}
		// Die Familiengroesse kann nicht linear addiert werden, daher darf es hier nie uebschneidungen geben
		if (other.getFamGroesse() != null) {
			Validate.isTrue(this.getFamGroesse() == null, "Familiengoressen kann nicht gemerged werden");
			this.setFamGroesse(other.getFamGroesse());
		}
		if (other.getEinkommensjahr() != 0) {
			this.setEinkommensjahr(other.getEinkommensjahr());
		}
		this.setHasSecondGesuchsteller(this.isHasSecondGesuchsteller() || other.isHasSecondGesuchsteller());

		this.ekv1_alleine = (this.ekv1_alleine || other.ekv1_alleine);
		this.ekv1_zuZweit = (this.ekv1_zuZweit || other.ekv1_zuZweit);
		this.ekv2_alleine = (this.ekv2_alleine || other.ekv2_alleine);
		this.ekv2_zuZweit = (this.ekv2_zuZweit || other.ekv2_zuZweit);
		this.ekv1_alleine_notAccepted = (this.ekv1_alleine_notAccepted || other.ekv1_alleine_notAccepted);
		this.ekv1_zuZweit_notAccepted = (this.ekv1_zuZweit_notAccepted || other.ekv1_zuZweit_notAccepted);
		this.ekv2_alleine_notAccepted = (this.ekv2_alleine_notAccepted || other.ekv2_alleine_notAccepted);
		this.ekv2_zuZweit_notAccepted = (this.ekv2_zuZweit_notAccepted || other.ekv2_zuZweit_notAccepted);
	}

	public void addBemerkung(RuleKey ruleKey, MsgKey msgKey) {
		String bemerkungsText = ServerMessageUtil.translateEnumValue(msgKey);
		this.addBemerkung(ruleKey.name() + ": " + bemerkungsText);

	}

	public void addBemerkung(RuleKey ruleKey, MsgKey msgKey, Object... args) {
		String bemerkungsText = ServerMessageUtil.translateEnumValue(msgKey, args);
		this.addBemerkung(ruleKey.name() + ": " + bemerkungsText);
	}

	/**
	 * Fügt eine Bemerkung zur Liste hinzu
	 */
	public void addBemerkung(String bem) {
		this.bemerkungen = Joiner.on("\n").skipNulls().join(
			StringUtils.defaultIfBlank(this.bemerkungen, null),
			StringUtils.defaultIfBlank(bem, null)
		);
	}

	/**
	 * Fügt mehrere Bemerkungen zur Liste hinzu
	 */
	public void addAllBemerkungen(@Nonnull List<String> bemerkungenList) {
		List<String> listOfStrings = new ArrayList<>();
		listOfStrings.add(this.bemerkungen);
		listOfStrings.addAll(bemerkungenList);
		this.bemerkungen = String.join(";", listOfStrings);
	}

	/**
	 * Dieses Pensum ist abhängig vom Erwerbspensum der Eltern respektive von dem durch die Fachstelle definierten
	 * Pensum.
	 * <p>
	 * Dieses Pensum kann grösser oder kleiner als das Betreuungspensum sein.
	 * <p>
	 * Beispiel: Zwei Eltern arbeiten zusammen 140%. In diesem Fall ist das anspruchsberechtigte Pensum 40%.
	 */

	public int getAnspruchberechtigtesPensum() {
		return anspruchberechtigtesPensum;
	}

	/**
	 * Das BG-Pensum (Pensum des Gutscheins) wird zum BG-Tarif berechnet und kann höchstens so gross sein, wie das Betreuungspensum.
	 * Falls das anspruchsberechtigte Pensum unter dem Betreuungspensum liegt, entspricht das BG-Pensum dem
	 * anspruchsberechtigten Pensum.
	 * <p>
	 * Ein Kind mit einem Betreuungspensum von 60% und einem anspruchsberechtigten Pensum von 40% hat ein BG-Pensum von 40%.
	 * Ein Kind mit einem Betreuungspensum von 40% und einem anspruchsberechtigten Pensum von 60% hat ein BG-Pensum von 40%.
	 */
	@Transient
	public int getBgPensum() {
		return Math.min(getBetreuungspensum(), getAnspruchberechtigtesPensum());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb())).append(" - ").append(Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigBis())).append("] ")
			.append(" EP GS1: ").append(erwerbspensumGS1).append("\t")
			.append(" EP GS2: ").append(erwerbspensumGS2).append("\t")
			.append(" BetrPensum: ").append(betreuungspensum).append("\t")
			.append(" Anspruch: ").append(anspruchberechtigtesPensum).append("\t")
			.append(" BG-Pensum: ").append(getBgPensum()).append("\t")
			.append(" Vollkosten: ").append(vollkosten).append("\t")
			.append(" Elternbeitrag: ").append(elternbeitrag).append("\t")
			.append(" Bemerkungen: ").append(bemerkungen).append("\t")
			.append(" Einkommen: ").append(massgebendesEinkommenVorAbzugFamgr).append("\t")
			.append(" Abzug Fam: ").append(abzugFamGroesse);
		return sb.toString();
	}

	public String toStringFinanzielleSituation() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigAb())).append(" - ").append(Constants.DATE_FORMATTER.format(getGueltigkeit().getGueltigBis())).append("] ")
			.append(" MassgebendesEinkommenVorAbzugFamiliengroesse: ").append(massgebendesEinkommenVorAbzugFamgr).append("\t")
			.append(" AbzugFamiliengroesse: ").append(abzugFamGroesse).append("\t")
			.append(" MassgebendesEinkommen: ").append(getMassgebendesEinkommen()).append("\t")
			.append(" Einkommensjahr: ").append(einkommensjahr).append("\t")
			.append(" Familiengroesse: ").append(famGroesse).append("\t")
			.append(" Bemerkungen: ").append(bemerkungen);
		return sb.toString();
	}

	//TODO: Ist hier Objects.equals() richtig??
	public boolean isSame(VerfuegungZeitabschnitt that) {
		if (this == that) {
			return true;
		}
		return isSameErwerbspensum(this.erwerbspensumGS1, that.erwerbspensumGS1) &&
			isSameErwerbspensum(this.erwerbspensumGS2, that.erwerbspensumGS2) &&
			betreuungspensum == that.betreuungspensum &&
			fachstellenpensum == that.fachstellenpensum &&
			anspruchspensumRest == that.anspruchspensumRest &&
			anspruchberechtigtesPensum == that.anspruchberechtigtesPensum &&
			hasSecondGesuchsteller == that.hasSecondGesuchsteller &&
			Objects.equals(abzugFamGroesse, that.abzugFamGroesse) &&
			Objects.equals(famGroesse, that.famGroesse) &&
			Objects.equals(massgebendesEinkommenVorAbzugFamgr, that.massgebendesEinkommenVorAbzugFamgr) &&
			(isWohnsitzNichtInGemeindeGS1() && isWohnsitzNichtInGemeindeGS2()) == (that.isWohnsitzNichtInGemeindeGS1() && that.isWohnsitzNichtInGemeindeGS2()) &&
			zuSpaetEingereicht == that.zuSpaetEingereicht &&
			bezahltVollkosten == that.bezahltVollkosten &&
			longAbwesenheit == that.longAbwesenheit &&
			kindMinestalterUnterschritten == that.kindMinestalterUnterschritten &&
			this.einkommensjahr == that.einkommensjahr &&
			this.ekv1_alleine == that.ekv1_alleine &&
			this.ekv1_zuZweit == that.ekv1_zuZweit &&
			this.ekv2_alleine == that.ekv2_alleine &&
			this.ekv2_zuZweit == that.ekv2_zuZweit &&
			this.ekv1_alleine_notAccepted == that.ekv1_alleine_notAccepted &&
			this.ekv1_zuZweit_notAccepted == that.ekv1_zuZweit_notAccepted &&
			this.ekv2_alleine_notAccepted == that.ekv2_alleine_notAccepted &&
			this.ekv2_zuZweit_notAccepted == that.ekv2_zuZweit_notAccepted;
	}

	private boolean isSameErwerbspensum(Integer thisErwerbspensumGS, Integer thatErwerbspensumGS) {
		return thisErwerbspensumGS == null && thatErwerbspensumGS == null
			|| !(thisErwerbspensumGS == null || thatErwerbspensumGS == null)
			&& thisErwerbspensumGS.equals(thatErwerbspensumGS);
	}

	/**
	 * Aller persistierten Daten ohne Kommentar
	 */
	public boolean isSamePersistedValues(VerfuegungZeitabschnitt that) {
		return betreuungspensum == that.betreuungspensum &&
			anspruchberechtigtesPensum == that.anspruchberechtigtesPensum &&
			(betreuungsstunden.compareTo(that.betreuungsstunden) == 0) &&
			(vollkosten.compareTo(that.vollkosten) == 0) &&
			(elternbeitrag.compareTo(that.elternbeitrag) == 0) &&
			(abzugFamGroesse.compareTo(that.abzugFamGroesse) == 0) &&
			(famGroesse.compareTo(that.famGroesse) == 0) &&
			(massgebendesEinkommenVorAbzugFamgr.compareTo(that.massgebendesEinkommenVorAbzugFamgr) == 0) &&
			getGueltigkeit().compareTo(that.getGueltigkeit()) == 0 &&
			this.einkommensjahr == that.einkommensjahr;
	}

	/**
	 * Gibt den Betrag des Gutscheins zurück.
	 */
	public BigDecimal getVerguenstigung() {
		if (vollkosten != null && elternbeitrag != null) {
			return vollkosten.subtract(elternbeitrag);
		}
		return ZERO;
	}

	@Override
	public int compareTo(VerfuegungZeitabschnitt other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getGueltigkeit(), other.getGueltigkeit());
		compareToBuilder.append(this.getId(), other.getId());  // wenn ids nicht gleich sind wollen wir auch compare to nicht gleich
		return compareToBuilder.toComparison();
	}
}
