export type AccessStatus = "ready" | "pending" | "disconnected";

export interface AccessMethod {
  id: "accessibility" | "shizuku";
  label: string;
  detail: string;
  status: AccessStatus;
}

export interface ReadinessCheck {
  id: string;
  label: string;
  detail: string;
  state: "complete" | "pending";
}

export interface ActivityEvent {
  id: string;
  time: string;
  event: string;
  source: string;
}

export const sampleDate = "Monday 16 November 2026";

export const accessMethods: readonly AccessMethod[] = [
  { id: "accessibility", label: "Accessibility Service", detail: "Service enabled in Android settings", status: "ready" },
  { id: "shizuku", label: "Shizuku", detail: "Authorization still needed", status: "pending" },
] as const;

export const readinessChecks: readonly ReadinessCheck[] = [
  { id: "service", label: "Accessibility Service", detail: "Enabled", state: "complete" },
  { id: "device", label: "Device compatibility", detail: "Checked", state: "complete" },
  { id: "storage", label: "Local event storage", detail: "Enabled", state: "complete" },
  { id: "shizuku", label: "Shizuku authorization", detail: "Pending", state: "pending" },
] as const;

export const activityEvents: readonly ActivityEvent[] = [
  { id: "service-enabled", time: "09:42", event: "Accessibility Service enabled", source: "Android settings" },
  { id: "checks-complete", time: "09:24", event: "Readiness check completed", source: "Relay" },
  { id: "shizuku-pending", time: "09:18", event: "Shizuku authorization pending", source: "Shizuku" },
  { id: "relay-opened", time: "09:14", event: "Relay opened", source: "This device" },
] as const;

export const completedChecks = readinessChecks.filter((check) => check.state === "complete").length;
export const readyPaths = accessMethods.filter((method) => method.status === "ready").length;

console.assert(accessMethods.length === readyPaths + accessMethods.filter((method) => method.status !== "ready").length, "Access-path total must reconcile.");
console.assert(readinessChecks.length === completedChecks + readinessChecks.filter((check) => check.state === "pending").length, "Readiness total must reconcile.");
console.assert(activityEvents.length === 4, "The sample activity log must contain four local events.");

