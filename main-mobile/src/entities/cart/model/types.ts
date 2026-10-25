import { z } from 'zod'
import { MoneySchema } from '@/shared/api'

export const CartItemSchema = z.object({
  productId: z.number(),
  name: z.string(),
  brand: z.string().nullable(),
  imageUrl: z.string().nullable(),
  unitPrice: MoneySchema,
  quantity: z.number(),
  lineTotal: MoneySchema,
  purchasable: z.boolean(),
})

export const CartSchema = z.object({
  cartId: z.string(),
  items: z.array(CartItemSchema),
  totalQuantity: z.number(),
  subtotal: MoneySchema,
  checkoutAvailable: z.boolean(),
})

export type CartItem = z.infer<typeof CartItemSchema>
export type Cart = z.infer<typeof CartSchema>

export const CART_ITEM_MIN_QUANTITY = 1
export const CART_ITEM_MAX_QUANTITY = 99
