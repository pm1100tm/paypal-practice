import Link from 'next/link'

export default function NotFound() {
  return (
    <div className="flex flex-col items-center gap-4 py-16 text-center text-sm text-neutral-600">
      <p>Page not found.</p>
      <Link href="/" className="font-semibold underline underline-offset-4">
        Go to shop
      </Link>
    </div>
  )
}
