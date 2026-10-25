'use client'

import { PayPalScriptProvider } from '@paypal/react-paypal-js'
import { useMemo, type ReactNode } from 'react'

interface PayPalProviderProps {
  /** PayPal REST App Client ID (서버 런타임 환경변수에서 전달) */
  clientId: string
  children: ReactNode
}

/**
 * PayPal SDK Provider — 상위 리렌더와 격리하기 위해 별도 컴포넌트로 분리.
 * options 를 memo 하므로 부모가 리렌더되어도 SDK 를 재로드하지 않습니다.
 */
export function PayPalProvider({ clientId, children }: PayPalProviderProps) {
  const options = useMemo(
    () => ({
      clientId,
      currency: 'USD',
      intent: 'capture',
      components: 'buttons',
    }),
    [clientId],
  )

  return <PayPalScriptProvider options={options}>{children}</PayPalScriptProvider>
}
