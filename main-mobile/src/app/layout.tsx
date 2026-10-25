import './globals.css'

import type { Metadata, Viewport } from 'next'
import type { ReactNode } from 'react'
import { Header } from '@/widgets/header'
import { Toaster } from '@/shared/ui'
import { QueryProvider } from './(providers)/QueryProvider'

export const metadata: Metadata = {
  title: 'ShopSWD',
  description: 'Guest checkout with PayPal',
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
}

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-dvh antialiased">
        <QueryProvider>
          <Header />
          <main className="mx-auto w-full max-w-md px-4 py-4">{children}</main>
          <Toaster />
        </QueryProvider>
      </body>
    </html>
  )
}
