import { z } from 'zod'

/** v1 은 PayPal 만 지원 */
export const PaymentMethodEnum = z.enum(['PAYPAL'])
export type PaymentMethodType = z.infer<typeof PaymentMethodEnum>

export const CreatePaymentResponseSchema = z.object({
  orderId: z.string(),
  /** PayPal Order ID — PayPal JS SDK createOrder 콜백에서 그대로 반환 */
  paymentId: z.string(),
  paymentMethod: z.string(),
  initiationType: z.enum(['CLIENT_SDK']),
  redirectUrl: z.string().nullable().optional(),
})
export type CreatePaymentResponse = z.infer<typeof CreatePaymentResponseSchema>

export const CapturePaymentResponseSchema = z.object({
  orderId: z.string(),
  status: z.string(),
})
export type CapturePaymentResponse = z.infer<typeof CapturePaymentResponseSchema>

export const CancelPaymentResponseSchema = z.object({
  success: z.boolean(),
  msg: z.string().nullable(),
})
export type CancelPaymentResponse = z.infer<typeof CancelPaymentResponseSchema>
