package org.example.commoncontracts.kafka;

public final class KafkaTopics {
    public static final String ORDER_SENT_FOR_APPROVAL = "order_sent_for_approval";
    public static final String ORDER_APPROVED = "order_approved";
    public static final String ORDER_REJECTED = "order_rejected";

    public static final String ORDER_AWAITING_PAYMENT_V1 = "order_awaiting_payment_v1";
    public static final String ORDER_CANCELLED_V1 = "order_cancelled_v1";
    public static final String RESERVATION_EXPIRED_V1 = "reservation_expired_v1";
    public static final String ASSEMBLY_COMPLETED_V1 = "assembly_completed_v1";
    public static final String ASSEMBLY_FAILED_V1 = "assembly_failed_v1";

    private KafkaTopics() {
    }
}
