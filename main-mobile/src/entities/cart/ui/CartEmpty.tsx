import Link from 'next/link'
import { routes } from '@/shared/config'

export function CartEmpty() {
  return (
    <div className="flex flex-col items-center gap-4 py-16 text-center">
      <p className="text-sm text-neutral-600">Your shopping bag is empty.</p>
      <Link href={routes.home} className="text-sm font-semibold underline underline-offset-4">
        Continue shopping
      </Link>
    </div>
  )
}
