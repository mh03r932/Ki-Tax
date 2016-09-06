package ch.dvbern.ebegu.api.resource;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungInfo;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.services.EinkommensverschlechterungInfoService;
import ch.dvbern.ebegu.services.EinkommensverschlechterungService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.WizardStepService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

/**
 * REST Resource fuer Einkommensverschlechterung
 */
@Path("einkommensverschlechterungInfo")
@Stateless
@Api
public class EinkommensverschlechterungInfoResource {

	@Inject
	private EinkommensverschlechterungInfoService einkommensverschlechterungInfoService;

	@Inject
	private GesuchService gesuchService;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private EinkommensverschlechterungService  einkommensverschlechterungService;

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Create a new EinkommensverschlechterungInfo in the database.")
	@Nullable
	@PUT
	@Path("/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveEinkommensverschlechterungInfo(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchId,
		@Nonnull @NotNull @Valid JaxEinkommensverschlechterungInfo jaxEinkommensverschlechterungInfo,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) throws EbeguException {

		Optional<Gesuch> gesuch = gesuchService.findGesuch(gesuchId.getId());
		if (gesuch.isPresent()) {

			EinkommensverschlechterungInfo oldData = null;
			EinkommensverschlechterungInfo ekviToMerge = new EinkommensverschlechterungInfo();

			if (jaxEinkommensverschlechterungInfo.getId() != null) {
				Optional<EinkommensverschlechterungInfo> optional = einkommensverschlechterungInfoService.
					findEinkommensverschlechterungInfo(jaxEinkommensverschlechterungInfo.getId());
				ekviToMerge = optional.orElse(new EinkommensverschlechterungInfo());
				oldData = new EinkommensverschlechterungInfo(ekviToMerge); //wir muessen uns merken wie die Daten vorher waren damit wir nachher vergleichen koennen
			}
			EinkommensverschlechterungInfo convertedEkvi = converter.einkommensverschlechterungInfoToEntity(jaxEinkommensverschlechterungInfo, ekviToMerge);
			convertedEkvi.setGesuch(gesuch.get());
			gesuch.get().setEinkommensverschlechterungInfo(convertedEkvi);
			convertedEkvi.setGesuch(gesuchService.updateGesuch(gesuch.get())); // saving gesuch cascades and saves Ekvi too

			//Alle Daten des EV loeschen wenn man kein EV mehr eingeben will
			removeEinkommensverschlechterungFromGesuchsteller(gesuch.get().getGesuchsteller1(), oldData, convertedEkvi);
			removeEinkommensverschlechterungFromGesuchsteller(gesuch.get().getGesuchsteller2(), oldData, convertedEkvi);

			wizardStepService.updateSteps(gesuchId.getId(), oldData,
				convertedEkvi, WizardStepName.EINKOMMENSVERSCHLECHTERUNG);

			URI uri = uriInfo.getBaseUriBuilder()
				.path(EinkommensverschlechterungInfoResource.class)
				.path("/" + convertedEkvi.getId())
				.build();

			JaxEinkommensverschlechterungInfo jaxEinkommensverschlechterungInfoReturn = converter.einkommensverschlechterungInfoToJAX(convertedEkvi);
			return Response.created(uri).entity(jaxEinkommensverschlechterungInfoReturn).build();
		}
		throw new EbeguEntityNotFoundException("saveEinkommensverschlechterungInfo", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchId.getId());
	}

	private void removeEinkommensverschlechterungFromGesuchsteller(Gesuchsteller gesuchsteller, EinkommensverschlechterungInfo oldData, EinkommensverschlechterungInfo convertedEkvi) {
		if (isNeededToRemoveEinkommensverschlechterung(gesuchsteller, oldData, convertedEkvi)) {
			einkommensverschlechterungService.removeEinkommensverschlechterungContainer(gesuchsteller.getEinkommensverschlechterungContainer());
			gesuchsteller.setEinkommensverschlechterungContainer(null);
        }
	}

	/**
	 * Returns true when the given GS already has an einkommensverschlechtrung and the new EVInfo says that no EV should be present
	 * @param gesuchsteller
	 * @param oldData
	 * @param newData
	 * @return
	 */
	private boolean isNeededToRemoveEinkommensverschlechterung(Gesuchsteller gesuchsteller, EinkommensverschlechterungInfo oldData, EinkommensverschlechterungInfo newData) {
		return oldData != null && newData != null && gesuchsteller != null
			&& oldData.getEinkommensverschlechterung() && !newData.getEinkommensverschlechterung()
			&& gesuchsteller.getEinkommensverschlechterungContainer() != null;
	}
}
