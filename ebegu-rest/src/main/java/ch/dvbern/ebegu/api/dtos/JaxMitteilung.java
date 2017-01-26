package ch.dvbern.ebegu.api.dtos;

import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * DTO fuer Stammdaten der Mitteilungen
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxMitteilung extends JaxAbstractDTO {

	private static final long serialVersionUID = -1297021341674137397L;

	@Nullable
	private JaxFall fall;

	@NotNull
	private MitteilungTeilnehmerTyp senderTyp;

	@NotNull
	private MitteilungTeilnehmerTyp empfaengerTyp;

	@NotNull
	private JaxAuthLoginElement sender;

	@Nullable
	private JaxAuthLoginElement empfaenger;

	@Size(min = 1, max = Constants.DB_DEFAULT_MAX_LENGTH)
	@NotNull
	private String subject;

	@Size(min = 1, max = Constants.DB_TEXTAREA_LENGTH)
	@NotNull
	private String message;

	@NotNull
	private MitteilungStatus mitteilungStatus;


	@Nullable
	public JaxFall getFall() {
		return fall;
	}

	public void setFall(@Nullable JaxFall fall) {
		this.fall = fall;
	}

	public MitteilungTeilnehmerTyp getSenderTyp() {
		return senderTyp;
	}

	public void setSenderTyp(MitteilungTeilnehmerTyp senderTyp) {
		this.senderTyp = senderTyp;
	}

	public MitteilungTeilnehmerTyp getEmpfaengerTyp() {
		return empfaengerTyp;
	}

	public void setEmpfaengerTyp(MitteilungTeilnehmerTyp empfaengerTyp) {
		this.empfaengerTyp = empfaengerTyp;
	}

	public JaxAuthLoginElement getSender() {
		return sender;
	}

	public void setSender(JaxAuthLoginElement sender) {
		this.sender = sender;
	}

	@Nullable
	public JaxAuthLoginElement getEmpfaenger() {
		return empfaenger;
	}

	public void setEmpfaenger(@Nullable JaxAuthLoginElement empfaenger) {
		this.empfaenger = empfaenger;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public MitteilungStatus getMitteilungStatus() {
		return mitteilungStatus;
	}

	public void setMitteilungStatus(MitteilungStatus mitteilungStatus) {
		this.mitteilungStatus = mitteilungStatus;
	}
}