const fs = require("fs");
const path = require("path");
const root = path.join("src", "test", "java");
const skip = new Set(["OrderApiSecurityTestConfig.java", "JavaMailSenderTestStub.java"]);
function stripRough(src) {
  let s = src.replace(/\/\*[\s\S]*?\*\//g, " ");
  s = s.replace(/\/\/[^\n]*/g, " ");
  s = s.replace(/"(?:\\.|[^"\\])*"/g, '""');
  s = s.replace(/'(?:\\.|[^'\\])*'/g, "''");
  s = s.replace(/"""[\s\S]*?"""/g, '""');
  return s;
}
function walk(d, acc) {
  for (const e of fs.readdirSync(d, { withFileTypes: true })) {
    const p = path.join(d, e.name);
    if (e.isDirectory()) walk(p, acc);
    else if (/Test(s)?\.java$/.test(e.name) && !skip.has(e.name)) acc.push(p);
  }
}
function parseFile(file) {
  const raw = fs.readFileSync(file, "utf8");
  const pkgM = raw.match(/^package\s+([\w.]+);/m);
  const pkg = pkgM ? pkgM[1] : "";
  const s = stripRough(raw);
  const lines = s.split(/\r?\n/);
  const classStack = [];
  let depth = 0;
  const tests = [];
  let pendingAt = null;
  for (let li = 0; li < lines.length; li++) {
    const line = lines[li];
    const open = (line.match(/\{/g) || []).length;
    const close = (line.match(/\}/g) || []).length;
    const classM = line.match(/^\s*(?:@\w+(?:\([^)]*\))?\s+)*(?:public\s+|private\s+|protected\s+)?(?:static\s+)?class\s+(\w+)\b/);
    if (classM && !line.includes("new ") && !line.includes("instanceof ")) {
      const simple = classM[1];
      const outer = classStack.length ? classStack[classStack.length - 1].fqn : null;
      const fqn = outer ? outer + "$" + simple : pkg + "." + simple;
      classStack.push({ fqn, openDepth: depth + open });
    }
    if (/@Test\b/.test(line)) pendingAt = li;
    if (pendingAt !== null) {
      const voidM = line.match(/\bvoid\s+(\w+)\s*\(/);
      if (voidM && li > pendingAt) {
        const fqn = classStack.length ? classStack[classStack.length - 1].fqn : null;
        if (fqn) tests.push(fqn + "#" + voidM[1]);
        pendingAt = null;
      }
    }
    depth += open - close;
    while (classStack.length && depth < classStack[classStack.length - 1].openDepth) classStack.pop();
  }
  return tests;
}
const prefixes = [["TC-AUTH-","auth"],["TC-REG-","reg"],["TC-CART-","cart"],["TC-ORD-","ord"],["TC-SEC-","sec"],["TC-PERF-","perf"],["TC-API-","api"],["TC-SVC-","svc"],["TC-DTO-","dto"],["TC-UTIL-","util"],["TC-INT-","int"],["TC-REPO-","repo"],["TC-CTRL-","ctrl"],["TC-APP-","app"]];
function suggestRef(key) {
  const k = key.toLowerCase();
  if (k.includes("integration") || k.includes("applicationtests")) return prefixes[10];
  if (k.includes("auth") || k.includes("login") || k.includes("customuserdetails")) return prefixes[0];
  if (k.includes("register")) return prefixes[1];
  if (k.includes("cart")) return prefixes[2];
  if (k.includes("order") || k.includes("pedido") || k.includes("checkout")) return prefixes[3];
  if (k.includes("secure") || k.includes("security") || k.includes("csrf")) return prefixes[4];
  if (k.includes("api.")) return prefixes[6];
  if (k.includes("service")) return prefixes[7];
  if (k.includes("dto.")) return prefixes[8];
  if (k.includes("util")) return prefixes[9];
  if (k.includes("repository")) return prefixes[11];
  if (k.includes("controller")) return prefixes[12];
  return prefixes[13];
}
const files = [];
walk(root, files);
const all = [];
for (const f of files.sort()) { try { all.push(...parseFile(f)); } catch (e) { console.error("skip", f, e.message); } }
const counters = {};
const linesOut = ["# Auto-generated", ""];
for (const key of all) {
  const [pfix, tag] = suggestRef(key);
  counters[tag] = (counters[tag] || 0) + 1;
  const n = String(counters[tag]).padStart(3, "0");
  linesOut.push(key + "=" + pfix + n);
}
console.log(linesOut.join("\n"));
