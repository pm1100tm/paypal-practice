import { cn } from '@/shared/lib/cn'

interface ThumbnailProps {
  name: string
  imageUrl: string | null
  className?: string
}

/** 이미지가 없으면 상품명 이니셜 플레이스홀더를 보여준다 */
export function Thumbnail({ name, imageUrl, className }: ThumbnailProps) {
  if (imageUrl) {
    // eslint-disable-next-line @next/next/no-img-element
    return <img src={imageUrl} alt={name} className={cn('rounded-md object-cover', className)} />
  }
  const initials = name
    .split(/\s+/)
    .slice(0, 2)
    .map((word) => word[0]?.toUpperCase() ?? '')
    .join('')
  return (
    <div
      aria-hidden
      className={cn(
        'flex items-center justify-center rounded-md bg-gradient-to-br from-rose-100 to-amber-100 text-lg font-semibold text-neutral-600',
        className,
      )}
    >
      {initials}
    </div>
  )
}
