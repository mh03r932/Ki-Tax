package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.AntragStatus;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service zum Verwalten von Gesuche
 */
public interface GesuchService {

	/**
	 * Erstellt ein neues Gesuch in der DB, falls der key noch nicht existiert
	 *
	 * @param gesuch der Gesuch als DTO
	 * @return das gespeicherte Gesuch
	 */
	@Nonnull
	Gesuch createGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Aktualisiert das Gesuch in der DB
	 *
	 * @param gesuch              das Gesuch als DTO
	 * @param saveInStatusHistory true wenn gewollt, dass die Aenderung in der Status gespeichert wird
	 * @return Das aktualisierte Gesuch
	 */
	@Nonnull
	Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory);

	/**
	 * Laedt das Gesuch mit der id aus der DB. ACHTUNG zudem wird hier der Status auf IN_BEARBEITUNG_JA gesetzt
	 * wenn der Benutzer ein JA Mitarbeiter ist und das Gesuch in FREIGEGEBEN ist
	 * @param key PK (id) des Gesuches
	 * @return Gesuch mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Gesuch> findGesuch(@Nonnull String key);

	/**
	 * Spezialmethode fuer die Freigabe. Kann Gesuche lesen die im Status Freigabequittung oder hoeher sind
	 */
	@Nonnull
	Optional<Gesuch> findGesuchForFreigabe(@Nonnull String gesuchId);

	/**
	 * Gibt alle Gesuche zurueck die in der Liste der gesuchIds auftauchen und fuer die der Benutzer berechtigt ist.
	 * Gesuche fuer die der Benutzer nicht berechtigt ist werden uebersprungen
	 * @param gesuchIds
	 *
	 */
	List<Gesuch> findReadableGesuche(@Nullable Collection<String> gesuchIds);

	/**
	 * Gibt alle existierenden Gesuche zurueck.
	 *
	 * @return Liste aller Gesuche aus der DB
	 */
	@Nonnull
	Collection<Gesuch> getAllGesuche();

	/**
	 * Gibt alle existierenden Gesuche zurueck, deren Status nicht VERFUEGT ist
	 *
	 * @return Liste aller Gesuche aus der DB
	 */
	@Nonnull
	Collection<Gesuch> getAllActiveGesuche();

	/**
	 * entfernt ein Gesuch aus der Database
	 *
	 * @param gesuch der Gesuch zu entfernen
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeGesuch(@Nonnull String gesuchId);

	/**
	 * Gibt eine Liste von Gesuchen zureck, deren Gesuchsteller 1 den angegebenen Namen und Vornamen hat.
	 * Achtung, damit ist ein Gesuchsteller nicht eindeutig identifiziert!
	 */
	@Nonnull
	List<Gesuch> findGesuchByGSName(String nachname, String vorname);

	/**
	 * Gibt alle Antraege des aktuell eingeloggten Benutzers
     */
	@Nonnull
	List<Gesuch> getAntraegeByCurrentBenutzer();

	/**
	 * Gibt alle Antraege zurueck, die eine Pendenz fuer das Steueramt sind
	 */
	@Nonnull
	List<Gesuch> getPendenzenForSteueramtUser();

	/**
	 * Methode welche jeweils eine bestimmte Menge an Suchresultate fuer die Paginatete Suchtabelle zuruckgibt,
	 *
	 * @param antragSearch
	 * @return Resultatpaar, der erste Wert im Paar ist die Anzahl Resultate, der zweite Wert ist die Resultatliste
	 */
	Pair<Long, List<Gesuch>> searchAntraege(AntragTableFilterDTO antragTableFilterDto);

	/**
	 * Gibt ein DTO mit saemtlichen Antragen eins bestimmten Falls zurueck
	 */
	@Nonnull
	List<JaxAntragDTO> getAllAntragDTOForFall(String fallId);

	/**
	 * Erstellt eine neue Mutation fuer die Gesuchsperiode und Fall des uebergebenen Antrags. Es wird immer der letzt
	 * verfuegte Antrag kopiert fuer die Mutation.
	 */
	@Nonnull
	Optional<Gesuch> antragMutieren(@Nonnull String antragId, @Nullable LocalDate eingangsdatum);

	/**
	 * hilfsmethode zur mutation von faellen ueber das gui. Wird fuer testzwecke benoetigt
	 */
	@Nonnull
	Optional<Gesuch> antragMutieren(@Nonnull Long fallNummer, @Nonnull String gesuchsperiodeId,
									@Nonnull LocalDate eingangsdatum);

	/**
	 * Gibt das neuste verfügte Gesuch (mit dem neuesten Verfuegungsdatum) in der gleichen Gesuchsperiode zurück,
	 * ACHTUNG: Dies kann ein neueres oder aelteres als das uebergebene Gesuch sein, oder sogar das uebergebene
	 * Gesuch selber!
	 */
	@Nonnull
	Optional<Gesuch> getNeustesVerfuegtesGesuchFuerGesuch(Gesuch gesuch);

	/**
	 * Gibt das neueste Gesuch der im selben Fall und Periode wie das gegebene Gesuch ist.
	 * Es wird nach Erstellungsdatum geschaut
	 * @param gesuch
	 * @return
	 */
	@Nonnull
	Optional<Gesuch> getNeustesGesuchFuerGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Gibt das letzte verfuegte Gesuch zurueck, also rekursiv ueber die Vorgaenger, nie das uebergebene Gesuch.
	 * @deprecated Diese Methode gibt das letzte verfuegte Gesuch zurueck. Dieses Gesuch muss nicht unbedignt
	 * die richtigen Daten enthalten. Diese Methode sollte deshalb nur vorsichtig benutzt werden.
	 * Z.B. wenn eine Betreuung im Status GESCHLOSSEN_OHNE_VERFUEGUNG ist, und wir diese Methode benutzen, wird dann die falsche
	 * Verfuegung geholt
	 * @return
	 */
	@Nonnull
	@Deprecated
	Optional<Gesuch> getNeuestesVerfuegtesVorgaengerGesuchFuerGesuch(Gesuch gesuch);

	/**
	 * fuellt die laufnummern der Gesuche/Mutationen eines Falls auf (nach timestamperstellt)
	 *
	 * @param fallId
	 */
	void updateLaufnummerOfAllGesucheOfFall(String fallId);

	/**
	 * Alle GesucheIDs des Gesuchstellers zurueckgeben fuer admin
	 */
	@Nonnull
	List<String> getAllGesuchIDsForFall(String fallId);

	/**
	 * Alle Gesuche fuer den gegebenen Fall in der gegebenen Periode
	 * @param fall
	 * @param gesuchsperiode
	 */
	@Nonnull
	List<Gesuch> getAllGesucheForFallAndPeriod(@Nonnull Fall fall, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Das gegebene Gesuch wird mit heutigem Datum freigegeben und den Step FREIGABE auf OK gesetzt
	 * @param gesuch
	 * @param statusToChangeTo
	 */
	Gesuch antragFreigabequittungErstellen(@Nonnull Gesuch gesuch, AntragStatus statusToChangeTo);

	/**
	 * Gibt das Gesuch frei für das Jugendamt: Anpassung des Status inkl Kopieren der Daten des GS aus den
	 * JA-Containern in die GS-Containern. Wird u.a. beim einlesen per Scanner aufgerufen
	 */
	@Nonnull
	Gesuch antragFreigeben(@Nonnull String gesuchId, @Nullable String username);

	/**
	 * Setzt das gegebene Gesuch als Beschwerde hängig und bei allen Gescuhen der Periode den Flag
	 * gesperrtWegenBeschwerde auf true.
	 * @return Gibt das aktualisierte gegebene Gesuch zurueck
	 */
	@Nonnull
	Gesuch setBeschwerdeHaengigForPeriode(@Nonnull Gesuch gesuch);

	/**
	 * Setzt das gegebene Gesuch als VERFUEGT und bei allen Gescuhen der Periode den Flag
	 * gesperrtWegenBeschwerde auf false
	 * @return Gibt das aktualisierte gegebene Gesuch zurueck
	 */
	@Nonnull
	Gesuch removeBeschwerdeHaengigForPeriode(@Nonnull Gesuch gesuch);

	/**
	 * Gibt alle aktuellen Antrags-Ids zurueck, d.h. den letzten Antrag jedes Falles, fuer eine Gesuchsperiode
	 */
	@Nonnull
	List<String> getNeuesteVerfuegteAntraege(@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Gibt pro Fall den neuesten freigegebenen Antrag für eine Gesuchsperiode zurück.
	 * Es wird *keine* Leseberechtigung geprüft, d.h. es werden sowohl JA-Angebote wie auch Nur-Schulamt
	 * zurückgegeben!
	 */
	@Nonnull
	List<String> getNeuesteFreigegebeneAntraege(@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Gibt die Antrags-Ids aller Antraege zurueck, welche im uebergebenen Zeitraum verfuegt wurden.
	 * Falls es mehrere fuer denselben Fall hat, wird nur der letzte (hoechste Laufnummer) zurueckgegeben
	 */
	@Nonnull
	List<String> getNeuesteVerfuegteAntraege(@Nonnull LocalDateTime verfuegtVon, @Nonnull LocalDateTime verfuegtBis);

	boolean isNeustesGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Schickt eine E-Mail an alle Gesuchsteller, die ihr Gesuch innerhalb einer konfigurierbaren Frist nach
	 * Erstellung nicht freigegeben haben.
	 * Gibt die Anzahl Warnungen zurueck.
	 */
	int warnGesuchNichtFreigegeben();

	/**
	 * Schickt eine E-Mail an alle Gesuchsteller, die die Freigabequittung innerhalb einer konfigurierbaren Frist nach
	 * Freigabe des Gesuchs nicht geschickt haben.
	 * Gibt die Anzahl Warnungen zurueck.
	 */
	int warnFreigabequittungFehlt();

	/**
	 * Löscht alle Gesuche, die nach einer konfigurierbaren Frist nach Erstellung nicht freigegeben bzw. nach Freigabe
	 * die Quittung nicht geschickt haben. Schickt dem Gesuchsteller eine E-Mail.
	 * Gibt die Anzahl Warnungen zurueck.
	 */
	int deleteGesucheOhneFreigabeOderQuittung();
}
