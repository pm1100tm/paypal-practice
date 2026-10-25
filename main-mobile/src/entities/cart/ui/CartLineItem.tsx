import type { ReactNode } from 'react'
import { Thumbnail } from '@/shared/ui'
import { formatMoney } from '@/shared/lib/money'
import type { CartItem } from '../model/types'

interface CartLineItemProps {
  item: CartItem
  /** 수량 변경 컨트롤 슬롯 */
  quantityControl?: ReactNode
  /** 삭제 버튼 슬롯 */
  removeAction?: ReactNode
}

export function CartLineItem({ item, quantityControl, removeAction }: CartLineItemProps) {
  return (
    <li className="flex gap-3 py-4">
      <Thumbnail name={item.name} imageUrl={item.imageUrl} className="h-20 w-20 shrink-0" />
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            {item.brand && <p className="text-xs text-neutral-500">{item.brand}</p>}
            <p className="truncate text-sm font-medium">{item.name}</p>
          </div>
          {removeAction}
        </div>
        <p className="text-xs text-neutral-500">{formatMoney(item.unitPrice)} each</p>
        {!item.purchasable && <p className="text-xs font-medium text-red-600">Currently unavailable</p>}
        <div className="mt-auto flex items-center justify-between">
          {quantityControl ?? <span className="text-sm">Qty {item.quantity}</span>}
          <span className="text-sm font-semibold">{formatMoney(item.lineTotal)}</span>
        </div>
      </div>
    </li>
  )
}
