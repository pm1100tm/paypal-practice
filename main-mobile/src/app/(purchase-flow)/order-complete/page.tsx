import { OrderCompleteView } from '@/widgets/order-complete'

interface OrderCompletePageProps {
  searchParams: Promise<{ id?: string | string[] }>
}

export default async function OrderCompletePage({ searchParams }: OrderCompletePageProps) {
  const { id } = await searchParams
  const orderId = Array.isArray(id) ? (id[0] ?? null) : (id ?? null)

  return <OrderCompleteView orderId={orderId} />
}
