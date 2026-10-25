import { z } from 'zod'
import { MoneySchema } from '@/shared/api'

export const OrderLineSchema = z.object({
  productId: z.number(),
  name: z.string(),
  unitPrice: MoneySchema,
  quantity: z.number(),
  lineTotal: MoneySchema,
})

/** 체크아웃 금액 미리보기 (POST /api/v2/orders/checkout) */
export const CheckoutSummarySchema = z.object({
  cartId: z.string(),
  items: z.array(OrderLineSchema),
  itemTotal: MoneySchema,
  shippingCost: MoneySchema,
  totalAmount: MoneySchema,
  freeShippingThreshold: MoneySchema.nullable(),
})

/** PayPal 이 SET_PROVIDED_ADDRESS 로 사용하는 배송지 구조와 1:1 대응 */
export const ShippingAddressSchema = z.object({
  fullName: z.string(),
  addressLine1: z.string(),
  addressLine2: z.string().optional(),
  city: z.string(),
  state: z.string().optional(),
  postalCode: z.string().optional(),
  countryCode: z.string(),
})

export const CreateOrderRequestSchema = z.object({
  cartId: z.string(),
  email: z.string(),
  phone: z.string().optional(),
  shippingAddress: ShippingAddressSchema,
})

export const CreateOrderResponseSchema = z.object({
  orderId: z.string(),
  totalAmount: MoneySchema,
})

export const OrderStatusSchema = z.enum(['PENDING_PAYMENT', 'PAID', 'CANCELLED'])

export const OrderSchema = z.object({
  orderId: z.string(),
  status: OrderStatusSchema,
  items: z.array(OrderLineSchema),
  itemTotal: MoneySchema,
  shippingCost: MoneySchema,
  totalAmount: MoneySchema,
  recipientName: z.string(),
  countryCode: z.string(),
  createdAt: z.string(),
  paidAt: z.string().nullable(),
})

export type OrderLine = z.infer<typeof OrderLineSchema>
export type CheckoutSummary = z.infer<typeof CheckoutSummarySchema>
export type ShippingAddress = z.infer<typeof ShippingAddressSchema>
export type CreateOrderRequest = z.infer<typeof CreateOrderRequestSchema>
export type CreateOrderResponse = z.infer<typeof CreateOrderResponseSchema>
export type OrderStatus = z.infer<typeof OrderStatusSchema>
export type Order = z.infer<typeof OrderSchema>
