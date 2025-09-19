import matter from "gray-matter";

import { source } from "@/lib/source";

interface OperationFrontmatter {
  title?: string;
  description?: string;
  _openapi?: {
    method?: string;
    route?: string;
  };
}

export interface OpenAPIOperation {
  href: string;
  method: string;
  route: string;
  summary: string;
}

function parseFrontmatter(content: string): OperationFrontmatter {
  const result = matter(content);

  return result.data as OperationFrontmatter;
}

function normalizeSummary(frontmatter: OperationFrontmatter): string {
  return frontmatter.title ?? frontmatter.description ?? "";
}

export function getOpenAPIOperations(): OpenAPIOperation[] {
  return source
    .getPages()
    .filter((page) =>
      page.url.startsWith("/openapi/") && page.url !== "/openapi"
    )
    .map((page) => {
      if (typeof page.data.content !== "string") {
        return undefined;
      }

      const frontmatter = parseFrontmatter(page.data.content);

      const method = frontmatter._openapi?.method?.toUpperCase();
      const route = frontmatter._openapi?.route;
      const summary = normalizeSummary(frontmatter);

      if (!method || !route) {
        return undefined;
      }

      return {
        href: page.url,
        method,
        route,
        summary,
      } satisfies OpenAPIOperation;
    })
    .filter((operation): operation is OpenAPIOperation => Boolean(operation))
    .sort((a, b) => {
      const routeCompare = a.route.localeCompare(b.route);
      if (routeCompare !== 0) {
        return routeCompare;
      }

      return a.method.localeCompare(b.method);
    });
}
