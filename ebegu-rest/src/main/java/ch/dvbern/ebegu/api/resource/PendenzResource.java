/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

package ch.dvbern.ebegu.api.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxPendenzInstitution;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.Validate;

/**
 * REST Resource fuer Pendenzen
 */
@Path("pendenzen")
@Stateless
@Api(description = "Resource für die Verwaltung der Pendenzlisten")
public class PendenzResource {

	@Inject
	private JaxBConverter converter;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private PrincipalBean principalBean;

	/**
	 * Gibt eine Liste mit allen Pendenzen des Jugendamtes zurueck.
	 * Sollte keine Pendenze gefunden werden oder ein Fehler passieren, wird eine leere Liste zurueckgegeben.
	 */
	@ApiOperation(value = "Gibt eine Liste mit allen Pendenzen des Jugendamtes zurueck",
		responseContainer = "List", response = JaxAntragDTO.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/jugendamt")
	public List<JaxAntragDTO> getAllPendenzenJA() {
		Collection<Gesuch> gesucheList = gesuchService.getAllActiveGesuche();

		List<JaxAntragDTO> pendenzenList = new ArrayList<>();
		gesucheList.stream().filter(gesuch -> gesuch.getFall() != null)
			.forEach(gesuch -> pendenzenList.add(converter.gesuchToAntragDTO(gesuch, principalBean.discoverMostPrivilegedRole())));
		return pendenzenList;
	}

	/**
	 * Gibt eine Liste mit allen Pendenzen des übergebenen Benutzers des JA zurueck.
	 * Sollte keine Pendenze gefunden werden oder ein Fehler passieren, wird eine leere Liste zurueckgegeben.
	 */
	@ApiOperation(value = "Gibt eine Liste mit allen Pendenzen des übergebenen Benutzers des JA zurueck.",
		responseContainer = "List", response = JaxAntragDTO.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/jugendamt/{benutzername}")
	public List<JaxAntragDTO> getAllPendenzenJA(@Nonnull @NotNull @PathParam("benutzername") String benutzername) {
		Validate.notNull(benutzername);
		Collection<Gesuch> gesucheList = gesuchService.getAllActiveGesucheOfVerantwortlichePerson(benutzername);

		List<JaxAntragDTO> pendenzenList = new ArrayList<>();
		gesucheList.stream().filter(gesuch -> gesuch.getFall() != null)
			.forEach(gesuch -> pendenzenList.add(converter.gesuchToAntragDTO(gesuch, principalBean.discoverMostPrivilegedRole())));
		return pendenzenList;
	}

	@ApiOperation(value = "Gibt eine Liste mit allen Pendenzen der Institution oder Traegerschaft des eingeloggten " +
		"Benutzers zurueck.", responseContainer = "List", response = JaxPendenzInstitution.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/institution")
	public List<JaxPendenzInstitution> getAllPendenzenInstitution() {
		Collection<Betreuung> betreuungenInStatus = betreuungService.getPendenzenForInstitutionsOrTraegerschaftUser();
		List<JaxPendenzInstitution> pendenzenList = new ArrayList<>();
		for (Betreuung betreuung : betreuungenInStatus) {
			JaxPendenzInstitution pendenz = new JaxPendenzInstitution();
			pendenz.setBetreuungsNummer(betreuung.getBGNummer());
			pendenz.setBetreuungsId(betreuung.getId());
			pendenz.setGesuchId(betreuung.extractGesuch().getId());
			pendenz.setKindId(betreuung.getKind().getId());
			pendenz.setName(betreuung.getKind().getKindJA().getNachname());
			pendenz.setVorname(betreuung.getKind().getKindJA().getVorname());
			pendenz.setGeburtsdatum(betreuung.getKind().getKindJA().getGeburtsdatum());
			pendenz.setEingangsdatum(betreuung.extractGesuch().getEingangsdatum());
			pendenz.setGesuchsperiode(converter.gesuchsperiodeToJAX(betreuung.extractGesuchsperiode()));
			pendenz.setBetreuungsangebotTyp(betreuung.getBetreuungsangebotTyp());
			pendenz.setInstitution(converter.institutionToJAX(betreuung.getInstitutionStammdaten().getInstitution()));

			if (betreuung.getVorgaengerId() == null) {
				pendenz.setTyp("PLATZBESTAETIGUNG");
			} else {
				//Wenn die Betreung eine VorgängerID hat ist sie mutiert
				pendenz.setTyp("PLATZBESTAETIGUNG_MUTATION");
			}

			pendenzenList.add(pendenz);
		}
		return pendenzenList;
	}

	/**
	 * Gibt eine Liste der Faelle des Gesuchstellers zurueck.
	 */
	@ApiOperation(value = "Gibt alle Antraege des eingeloggten Gesuchstellers zurueck.",
		responseContainer = "List", response = JaxAntragDTO.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/gesuchsteller")
	public List<JaxAntragDTO> getAllAntraegeGesuchsteller() {
		List<Gesuch> antraege = gesuchService.getAntraegeByCurrentBenutzer();
		return convertToAntragDTOList(antraege);
	}

	@Nonnull
	private List<JaxAntragDTO> convertToAntragDTOList(List<Gesuch> antraege) {
		List<JaxAntragDTO> pendenzenList = new ArrayList<>();
		antraege.forEach(gesuch -> pendenzenList.add(converter.gesuchToAntragDTO(gesuch, principalBean.discoverMostPrivilegedRole())));
		return pendenzenList;
	}
}
