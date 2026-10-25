import { z } from 'zod'
import { MoneySchema } from '@/shared/api'

export const ProductSchema = z.object({
  productId: z.number(),
  name: z.string(),
  brand: z.string().nullable(),
  price: MoneySchema,
  imageUrl: z.string().nullable(),
  purchasable: z.boolean(),
})

export type Product = z.infer<typeof ProductSchema>
