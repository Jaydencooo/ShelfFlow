import type { HTMLAttributes, ReactNode } from "react"
import clsx from "clsx"

export function Panel({ children, className, ...props }: { children: ReactNode; className?: string } & HTMLAttributes<HTMLElement>) {
  return <section className={clsx("rounded-[28px] border border-slate-900/[0.08] bg-white/[0.82] shadow-[0_24px_70px_rgba(15,23,42,0.07)] backdrop-blur-xl", className)} {...props}>{children}</section>
}

export function SectionTitle({ title, description, action }: { title: string; description?: string; action?: ReactNode }) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-4">
      <div className="space-y-1">
        <h2 className="text-xl font-semibold text-slate-950">{title}</h2>
        {description ? <p className="text-sm text-slate-500">{description}</p> : null}
      </div>
      {action}
    </div>
  )
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <Panel className="p-10 text-center">
      <h3 className="text-base font-semibold text-slate-900">{title}</h3>
      <p className="mt-2 text-sm text-slate-500">{description}</p>
    </Panel>
  )
}

export function InlineError({ message }: { message: string }) {
  return (
    <div className="rounded-2xl border border-rose-200 bg-rose-50/90 px-4 py-3 text-sm text-rose-700">
      {message}
    </div>
  )
}

export function InlineSuccess({ message }: { message: string }) {
  return (
    <div className="rounded-2xl border border-emerald-200 bg-emerald-50/90 px-4 py-3 text-sm text-emerald-700">
      {message}
    </div>
  )
}

export function PaginationControls({
  page,
  pageSize,
  total,
  onPageChange
}: {
  page: number
  pageSize: number
  total: number
  onPageChange: (page: number) => void
}) {
  const totalPages = Math.max(1, Math.ceil(total / pageSize))

  if (totalPages <= 1) {
    return null
  }

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-[24px] border border-slate-900/[0.08] bg-white/80 px-5 py-4 shadow-sm">
      <div className="text-sm text-slate-500">
        第 {page} / {totalPages} 页，共 {total} 条
      </div>
      <div className="flex items-center gap-2">
        <button
          className="rounded-full border border-slate-900/[0.08] bg-white px-4 py-2 text-sm font-medium text-slate-600 transition hover:border-emerald-200 hover:text-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
          disabled={page <= 1}
          onClick={() => onPageChange(page - 1)}
          type="button"
        >
          上一页
        </button>
        <button
          className="rounded-full border border-slate-900/[0.08] bg-white px-4 py-2 text-sm font-medium text-slate-600 transition hover:border-emerald-200 hover:text-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
          disabled={page >= totalPages}
          onClick={() => onPageChange(page + 1)}
          type="button"
        >
          下一页
        </button>
      </div>
    </div>
  )
}

export function StatusBadge({ children, tone = "neutral" }: { children: ReactNode; tone?: "neutral" | "success" | "warning" | "danger" | "info" }) {
  const className = {
    neutral: "bg-slate-100 text-slate-700",
    success: "bg-emerald-100 text-emerald-700",
    warning: "bg-amber-100 text-amber-700",
    danger: "bg-rose-100 text-rose-700",
    info: "bg-sky-100 text-sky-700"
  }[tone]

  return <span className={clsx("inline-flex rounded-full px-3 py-1 text-xs font-semibold", className)}>{children}</span>
}
