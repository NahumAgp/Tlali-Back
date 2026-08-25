package com.tlali.api.firebasehistory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
		name = "firebase_node_history",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_firebase_node_history_node_seq", columnNames = {"node", "sequence_number"})
		},
		indexes = {
				@Index(name = "idx_firebase_node_history_node", columnList = "node"),
				@Index(name = "idx_firebase_node_history_type", columnList = "node_type"),
				@Index(name = "idx_firebase_node_history_received", columnList = "gateway_received_at")
		}
)
public class FirebaseNodeHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String node;

	@Column(name = "node_type", length = 40)
	private String type;

	@Column(name = "sequence_number")
	private Long sequenceNumber;

	@Lob
	@Column(name = "data_json")
	private String dataJson;

	@Lob
	@Column(name = "gateway_json")
	private String gatewayJson;

	@Lob
	@Column(name = "radio_json")
	private String radioJson;

	@Lob
	@Column(name = "valid_json")
	private String validJson;

	@Column(name = "gateway_received_at")
	private Instant gatewayReceivedAt;

	@Column(name = "synced_at", nullable = false)
	private Instant syncedAt;

	protected FirebaseNodeHistory() {
	}

	public FirebaseNodeHistory(
			String node,
			String type,
			Long sequenceNumber,
			String dataJson,
			String gatewayJson,
			String radioJson,
			String validJson,
			Instant gatewayReceivedAt,
			Instant syncedAt
	) {
		this.node = node;
		this.type = type;
		this.sequenceNumber = sequenceNumber;
		this.dataJson = dataJson;
		this.gatewayJson = gatewayJson;
		this.radioJson = radioJson;
		this.validJson = validJson;
		this.gatewayReceivedAt = gatewayReceivedAt;
		this.syncedAt = syncedAt;
	}

	public Long getId() {
		return id;
	}

	public String getNode() {
		return node;
	}

	public String getType() {
		return type;
	}

	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	public String getDataJson() {
		return dataJson;
	}

	public String getGatewayJson() {
		return gatewayJson;
	}

	public String getRadioJson() {
		return radioJson;
	}

	public String getValidJson() {
		return validJson;
	}

	public Instant getGatewayReceivedAt() {
		return gatewayReceivedAt;
	}

	public Instant getSyncedAt() {
		return syncedAt;
	}
}
