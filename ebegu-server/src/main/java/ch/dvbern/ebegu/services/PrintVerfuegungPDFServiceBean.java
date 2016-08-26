package ch.dvbern.ebegu.services;
/*
* Copyright (c) 2016 DV Bern AG, Switzerland
*
* Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
* geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
* insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
* elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
* Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
*
* Ersteller: zeab am: 09.08.2016
*/

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.vorlagen.GeneratePDFDocumentHelper;
import ch.dvbern.ebegu.vorlagen.VerfuegungPrintImpl;
import ch.dvbern.ebegu.vorlagen.VerfuegungPrintMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.docx.DOCXMergeEngine;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementiert VerfuegungsGenerierungPDFService
 */
@Stateless
@Local(PrintVerfuegungPDFService.class)
public class PrintVerfuegungPDFServiceBean extends AbstractBaseService implements PrintVerfuegungPDFService {

	@Nonnull
	@Override
	@SuppressFBWarnings(value = "UI_INHERITANCE_UNSAFE_GETRESOURCE")
	public List<byte[]> printVerfuegung(@Nonnull Gesuch gesuch) throws MergeDocException {

		Objects.requireNonNull(gesuch, "Das Argument 'gesuch' darf nicht leer sein");

		List<byte[]> result = new ArrayList<>();

		DOCXMergeEngine docxME = new DOCXMergeEngine("Verfuegungsmuster");

		try {

			for (KindContainer kindContainer : gesuch.getKindContainers()) {
				for (Betreuung betreuung : kindContainer.getBetreuungen()) {
					// Pro Betreuung ein Dokument
					InputStream is = this.getClass().getResourceAsStream("/vorlagen/Verfuegungsmuster.docx");
					Objects.requireNonNull(is, "Verfuegungsmuster.docx nicht gefunden");
					result.add(new GeneratePDFDocumentHelper().generatePDFDocument(docxME.getDocument(is, new VerfuegungPrintMergeSource(new VerfuegungPrintImpl(betreuung)))));
					is.close();
				}
			}
		} catch (IOException | DocTemplateException e) {
			throw new MergeDocException("generiereVerfuegung()", "Bei der Generierung der Verfuegungsmustervorlage ist einen Fehler aufgetreten", e, new Objects[] {});
		}
		return result;
	}
}
