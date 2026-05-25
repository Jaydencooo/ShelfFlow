import type { CSSProperties } from "react"

export const glassCard: CSSProperties = {
  background: "rgba(255, 255, 255, 0.35)",
  backdropFilter: "blur(40px) saturate(250%)",
  WebkitBackdropFilter: "blur(40px) saturate(250%)",
  border: "1px solid rgba(255, 255, 255, 0.5)",
  boxShadow: "0 8px 32px rgba(15, 23, 42, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.6)",
}

export const glassPanel: CSSProperties = {
  background: "rgba(255, 255, 255, 0.25)",
  backdropFilter: "blur(40px) saturate(250%)",
  WebkitBackdropFilter: "blur(40px) saturate(250%)",
  border: "1px solid rgba(255, 255, 255, 0.4)",
  boxShadow: "0 4px 24px rgba(15, 23, 42, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.35)",
}

export const glassDialog: CSSProperties = {
  background: "rgba(255, 255, 255, 0.86)",
  backdropFilter: "blur(40px) saturate(250%)",
  WebkitBackdropFilter: "blur(40px) saturate(250%)",
  border: "1px solid rgba(255, 255, 255, 0.6)",
  boxShadow: "0 24px 64px rgba(15, 23, 42, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.8)",
}

export const glassInputClassName =
  "border-white/50 bg-white/40 text-slate-800 placeholder:text-slate-500 transition-all focus:bg-white/60"

export const primaryGradient = "linear-gradient(135deg, #0C115B 0%, #1e3a8a 100%)"
export const primaryShadow = "0 4px 14px rgba(12, 17, 91, 0.4)"

export const statusGradients = {
  success: "linear-gradient(135deg, #059669 0%, #10b981 100%)",
  warning: "linear-gradient(135deg, #d97706 0%, #f59e0b 100%)",
  error: "linear-gradient(135deg, #dc2626 0%, #ef4444 100%)",
  info: "linear-gradient(135deg, #0C115B 0%, #1e3a8a 100%)",
  purple: "linear-gradient(135deg, #7c3aed 0%, #8b5cf6 100%)",
  cyan: "linear-gradient(135deg, #0891b2 0%, #06b6d4 100%)",
} as const
