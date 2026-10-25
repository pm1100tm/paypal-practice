import type { Money } from '@/shared/api'
import { formatMoney } from '@/shared/lib/money'
import type { OrderLine } from '../model/types'

interface OrderSummaryProps {
  items: OrderLine[]
  itemTotal: Money
  shippingCost: Money
  totalAmount: Money
}

export function OrderSummary({ items, itemTotal, shippingCost, totalAmount }: OrderSummaryProps) {
  return (
    <section aria-label="Order summary" className="flex flex-col gap-3">
      <ul className="flex flex-col gap-2">
        {items.map((item) => (
          <li key={item.productId} className="flex justify-between gap-3 text-sm">
            <span className="min-w-0 truncate text-neutral-700">
              {item.name} <span className="text-neutral-400">× {item.quantity}</span>
            </span>
            <span className="shrink-0">{formatMoney(item.lineTotal)}</span>
          </li>
        ))}
      </ul>
      <dl className="flex flex-col gap-1 border-t border-neutral-200 pt-3 text-sm">
        <div className="flex justify-between">
          <dt className="text-neutral-600">Subtotal</dt>
          <dd>{formatMoney(itemTotal)}</dd>
        </div>
        <div className="flex justify-between">
          <dt className="text-neutral-600">Shipping</dt>
          <dd>{Number(shippingCost.amount) === 0 ? 'Free' : formatMoney(shippingCost)}</dd>
        </div>
        <div className="mt-1 flex justify-between text-base font-semibold">
          <dt>Total</dt>
          <dd>{formatMoney(totalAmount)}</dd>
        </div>
      </dl>
    </section>
  )
}
