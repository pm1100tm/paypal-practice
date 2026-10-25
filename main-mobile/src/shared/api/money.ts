import { z } from 'zod'

/** 백엔드 금액 응답 `{ amount: "12.30", currency: "USD" }` */
export const MoneySchema = z.object({
  amount: z.string(),
  currency: z.string(),
})

export type Money = z.infer<typeof MoneySchema>
