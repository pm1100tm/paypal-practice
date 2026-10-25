import { createOrder, type CreateOrderRequest } from '@/entities/order'
import { createPayment } from '@/entities/payment'

const ORDER_ID_MISSING = 'Order ID was not received. Please try again.'
const PAYMENT_ID_MISSING = 'Payment ID was not received. Please try again.'

/**
 * 주문 생성(createOrder) → 결제 생성(createPayment) 오케스트레이션.
 *
 * ⚠️ 주의: 여기서의 "결제 생성"은 PayPal 에 주문(결제 의향)을 등록하는 단계이며,
 * 실제 결제 확정(capturePayment)은 구매자 승인(onApprove) 이후 서버에서 수행됩니다.
 *
 * @returns orderId(가맹점 주문 ID) + paymentId(PayPal Order ID)
 */
export async function createOrderAndPayment(params: {
  orderRequest: CreateOrderRequest
  /** createOrder 성공 직후 호출 — createPayment 실패 시에도 orderId 를 보존하기 위해 사용 */
  onOrderCreated?: (orderId: string) => void
}): Promise<{ orderId: string; paymentId: string }> {
  // Step 1: 백엔드에 주문 생성 (금액은 서버가 장바구니 기준으로 계산)
  const order = await createOrder(params.orderRequest)
  if (!order.orderId) throw new Error(ORDER_ID_MISSING)
  params.onOrderCreated?.(order.orderId)

  // Step 2: 생성된 주문 ID 로 PayPal 주문 생성
  const payment = await createPayment(order.orderId, 'PAYPAL')
  if (!payment.paymentId) throw new Error(PAYMENT_ID_MISSING)

  return { orderId: order.orderId, paymentId: payment.paymentId }
}
