import Link from "next/link";

import { getOpenAPIOperations } from "@/lib/openapi-operations";

const methodColors: Record<string, string> = {
  GET: "bg-emerald-100 text-emerald-700",
  POST: "bg-blue-100 text-blue-700",
  PUT: "bg-amber-100 text-amber-700",
  PATCH: "bg-purple-100 text-purple-700",
  DELETE: "bg-rose-100 text-rose-700",
};

function MethodBadge({ method }: { method: string }) {
  const color = methodColors[method] ?? "bg-slate-100 text-slate-700";

  return (
    <span
      className={`font-semibold uppercase tracking-wide text-xs px-2 py-1 rounded ${color}`}
    >
      {method}
    </span>
  );
}

export function APIOverviewTable() {
  const operations = getOpenAPIOperations();

  if (operations.length === 0) {
    return null;
  }

  return (
    <div className="not-prose mt-6 overflow-x-auto">
      <table className="w-full border-separate border-spacing-y-2 text-sm">
        <thead className="text-left text-muted-foreground">
          <tr>
            <th className="px-3 py-2 font-medium">路径</th>
            <th className="px-3 py-2 font-medium">方法</th>
            <th className="px-3 py-2 font-medium">摘要</th>
          </tr>
        </thead>
        <tbody>
          {operations.map((operation) => (
            <tr
              key={`${operation.method}-${operation.route}`}
              className="bg-muted/30"
            >
              <td className="px-3 py-2 align-top font-mono">
                <Link className="hover:underline" href={operation.href}>
                  {operation.route}
                </Link>
              </td>
              <td className="px-3 py-2 align-top">
                <MethodBadge method={operation.method} />
              </td>
              <td className="px-3 py-2 align-top text-muted-foreground">
                {operation.summary || "—"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
