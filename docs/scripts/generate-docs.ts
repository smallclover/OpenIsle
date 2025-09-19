import { rmSync } from "node:fs";

import { generateFiles } from "fumadocs-openapi";
import { openapi } from "@/lib/openapi";

const outputDir = "./content/docs/openapi/(generated)";

rmSync(outputDir, { recursive: true, force: true });

void generateFiles({
  input: openapi,
  output: outputDir,
  // we recommend to enable it
  // make sure your endpoint description doesn't break MDX syntax.
  includeDescription: true,
  per: "operation",
  groupBy: "route",
});
