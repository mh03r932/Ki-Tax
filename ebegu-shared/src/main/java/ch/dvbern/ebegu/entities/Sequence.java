/*
 * Copyright © 2016 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.SequenceType;
import ch.dvbern.ebegu.util.Constants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(
	uniqueConstraints = @UniqueConstraint(name = "UK_sequence", columnNames = { "sequenceType", "mandant_id" }),
	indexes = {
		@Index(name = "sequence_ix1", columnList = "mandant_id"),
	}
)
public class Sequence extends AbstractEntity implements HasMandant {

	private static final long serialVersionUID = -8310781486097591752L;

	@Nonnull
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = Constants.DB_DEFAULT_SHORT_LENGTH)
	private SequenceType sequenceType;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private Long currentValue;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_sequence_mandant_id"))
	private Mandant mandant;

	/**
	 * JPA only
	 */
	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "JPA instantiation only")
	protected Sequence() {
		this(SequenceType.FALL_NUMMER, -1L);
	}

	public Sequence(@Nonnull SequenceType sequenceType, @Nonnull Long currentValue) {
		this.sequenceType = sequenceType;
		this.currentValue = currentValue;
	}

	@Nonnull
	public Long incrementAndGet() {
		currentValue = currentValue + 1;
		return currentValue;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("sequenceType", sequenceType)
			.append("currentValue", currentValue)
			.toString();
	}

	@Nonnull
	public SequenceType getSequenceType() {
		return sequenceType;
	}

	public void setSequenceType(@Nonnull SequenceType sequenceType) {
		this.sequenceType = sequenceType;
	}

	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(Mandant mandant) {
		this.mandant = mandant;
	}

	@Nonnull
	public Long getCurrentValue() {
		return currentValue;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof Sequence)) {
			return false;
		}
		final Sequence otherSequence = (Sequence) other;
		return Objects.equals(getSequenceType(), otherSequence.getSequenceType()) &&
			Objects.equals(getCurrentValue(), otherSequence.getCurrentValue());
	}
}
