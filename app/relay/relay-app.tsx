"use client";

import { useEffect, useMemo, useRef, useState } from "react";

import { accessMethods, activityEvents, completedChecks, readinessChecks, readyPaths, sampleDate, type AccessStatus } from "./data";

type View = "/" | "/setup" | "/controls" | "/activity" | "/settings";

const navItems: readonly { label: string; view: View }[] = [
  { label: "Home", view: "/" },
  { label: "Access setup", view: "/setup" },
  { label: "Controls", view: "/controls" },
  { label: "Activity", view: "/activity" },
  { label: "Settings", view: "/settings" },
];

const viewTitles: Record<View, string> = {
  "/": "Home",
  "/setup": "Access setup",
  "/controls": "Controls",
  "/activity": "Activity",
  "/settings": "Settings",
};

const statusLabel = (status: AccessStatus) => (status === "ready" ? "Ready" : status === "pending" ? "Needs authorization" : "Disconnected");
const statusClass = (status: AccessStatus) => (status === "ready" ? "positive" : status === "pending" ? "warning" : "neutral");

const readHash = (): View => {
  if (typeof window === "undefined") return "/";
  const route = (window.location.hash.split("?")[0] ?? "").replace("#", "") || "/";
  return navItems.some((item) => item.view === route) ? (route as View) : "/";
};

interface PillProps {
  status: AccessStatus | "complete";
  children: string;
}

const Pill = ({ status, children }: PillProps) => <span className={`relay-pill ${status === "complete" ? "positive" : statusClass(status)}`}>{children}</span>;

interface PanelProps {
  title: string;
  children: React.ReactNode;
  className?: string;
  action?: React.ReactNode;
}

const Panel = ({ title, children, className = "", action }: PanelProps) => (
  <section className={`relay-panel ${className}`} aria-label={title}>
    <div className="relay-panel-head">
      <h2>{title}</h2>
      {action}
    </div>
    {children}
  </section>
);

export const RelayApp = () => {
  const [view, setView] = useState<View>("/");
  const [menuOpen, setMenuOpen] = useState(false);
  const [activeMethod, setActiveMethod] = useState<"accessibility" | "shizuku" | null>("accessibility");
  const [shizukuState, setShizukuState] = useState<AccessStatus>("pending");
  const [activityFilter, setActivityFilter] = useState<"today" | "all">("today");
  const [search, setSearch] = useState("");
  const [preferences, setPreferences] = useState({ storage: true, motion: true, labels: true });
  const [saved, setSaved] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [historyCleared, setHistoryCleared] = useState(false);
  const contentRef = useRef<HTMLElement>(null);
  const menuTriggerRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    const update = () => setView(readHash());
    update();
    window.addEventListener("hashchange", update);
    return () => window.removeEventListener("hashchange", update);
  }, []);

  useEffect(() => {
    if (!menuOpen) return;
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") setMenuOpen(false);
    };
    window.addEventListener("keydown", closeOnEscape);
    return () => window.removeEventListener("keydown", closeOnEscape);
  }, [menuOpen]);

  useEffect(() => {
    if (!menuOpen) menuTriggerRef.current?.focus();
  }, [menuOpen]);

  const navigate = (next: View) => {
    window.location.hash = next === "/" ? "/" : next;
    setView(next);
    setMenuOpen(false);
    window.setTimeout(() => contentRef.current?.focus(), 0);
  };

  const methods = useMemo(
    () => accessMethods.map((method) => (method.id === "shizuku" ? { ...method, status: shizukuState } : method)),
    [shizukuState],
  );
  const liveReadyPaths = methods.filter((method) => method.status === "ready").length;
  const filteredEvents = useMemo(() => {
    if (historyCleared) return [];
    const term = search.trim().toLowerCase();
    return activityEvents.filter((event) => !term || `${event.event} ${event.source}`.toLowerCase().includes(term));
  }, [historyCleared, search]);

  const requestShizuku = () => {
    setShizukuState((current) => (current === "ready" ? "pending" : "ready"));
    setActiveMethod("shizuku");
  };

  const renderNavigation = (mobile = false) => (
    <nav className={mobile ? "relay-mobile-nav" : "relay-nav"} aria-label="Relay views">
      {navItems.map((item) => (
        <button
          className={`relay-nav-item ${view === item.view ? "is-active" : ""}`}
          type="button"
          key={item.view}
          aria-current={view === item.view ? "page" : undefined}
          onClick={() => navigate(item.view)}
        >
          <span>{item.label}</span>
        </button>
      ))}
    </nav>
  );

  const accessPath = () => (
    <Panel title="Access path" className="relay-span-9" action={<button className="relay-text-button" type="button" onClick={() => navigate("/setup")}>Switch path</button>}>
      <div className="relay-paths">
        {methods.map((method) => {
          const selected = activeMethod === method.id;
          return (
            <button
              type="button"
              className={`relay-path ${selected ? "is-selected" : ""}`}
              key={method.id}
              aria-pressed={selected}
              onClick={() => setActiveMethod(method.id)}
            >
              <span className="relay-path-line" aria-hidden="true" />
              <span className="relay-path-copy"><strong>{method.label}</strong><small>{method.detail}</small></span>
              <Pill status={method.status}>{statusLabel(method.status)}</Pill>
            </button>
          );
        })}
      </div>
      <div className="relay-session-line"><span>Current session</span><strong>{activeMethod ? methods.find((method) => method.id === activeMethod)?.label : "No path selected"}</strong><button className="relay-text-button" type="button" onClick={() => setActiveMethod(null)} disabled={!activeMethod}>Disconnect</button></div>
    </Panel>
  );

  const home = () => (
    <div className="relay-home relay-grid">
      <div className="relay-greeting relay-span-12">
        <p className="relay-sample">Sample data for This device, {sampleDate}.</p>
        <div className="relay-greeting-row"><div><h1>Good morning. Accessibility is ready.</h1><p>Shizuku authorization is still pending.</p></div><button className="relay-primary" type="button" onClick={() => navigate("/setup")}>Authorize Shizuku</button></div>
      </div>
      <div className="relay-kpis relay-span-12">
        {[{ label: "Ready paths", value: `${liveReadyPaths} of 2`, caption: "One path can run" }, { label: "Checks complete", value: `${completedChecks} of 4`, caption: "One check needs attention" }, { label: "Local events", value: `${historyCleared ? 0 : activityEvents.length}`, caption: "Recorded today" }, { label: "Configured actions", value: "0", caption: "No verified commands" }].map((metric) => <div className="relay-kpi" key={metric.label}><span>{metric.label}</span><strong className="num">{metric.value}</strong><small>{metric.caption}</small></div>)}
      </div>
      {accessPath()}
      <Panel title="Checks" className="relay-span-3" action={<button className="relay-text-button" type="button" onClick={() => navigate("/settings")}>Safety</button>}>
        <div className="relay-check-list">{readinessChecks.map((check) => <div className="relay-check" key={check.id}><span><strong>{check.label}</strong><small>{check.detail}</small></span><Pill status={check.state}>{check.state === "complete" ? "Complete" : "Pending"}</Pill></div>)}</div>
      </Panel>
      <Panel title="Controls" className="relay-span-6" action={<button className="relay-text-button" type="button" onClick={() => navigate("/controls")}>Manage controls</button>}><div className="relay-empty">No verified commands are configured.</div></Panel>
      <Panel title="Activity" className="relay-span-6" action={<button className="relay-text-button" type="button" onClick={() => navigate("/activity")}>View activity</button>}><EventRows events={filteredEvents.slice(0, 2)} /></Panel>
    </div>
  );

  const setup = () => (
    <div className="relay-view relay-grid">
      <Title title="Access setup" />
      <Panel title="Access methods" className="relay-span-12"><div className="relay-method-grid">{methods.map((method) => <div className="relay-method" key={method.id}><div><strong>{method.label}</strong><p>{method.detail}</p></div><Pill status={method.status}>{statusLabel(method.status)}</Pill><button className={method.id === "shizuku" ? "relay-primary" : "relay-secondary"} type="button" onClick={() => method.id === "shizuku" ? requestShizuku() : setActiveMethod("accessibility")}>{method.id === "shizuku" ? "Authorize Shizuku" : "Open accessibility settings"}</button></div>)}</div></Panel>
      <Panel title="Accessibility Service" className="relay-span-6"><div className="relay-detail-row"><div><strong>Relay Accessibility Service</strong><small>Enabled on This device</small></div><Pill status="ready">Ready</Pill><button className="relay-secondary" type="button" onClick={() => setActiveMethod("accessibility")}>Open accessibility settings</button></div></Panel>
      <Panel title="Shizuku" className="relay-span-6"><div className="relay-detail-row"><div><strong>Authorization</strong><small>{shizukuState === "ready" ? "Authorization available in sample state" : "Authorization is pending"}</small></div><Pill status={shizukuState}>{statusLabel(shizukuState)}</Pill><button className="relay-secondary" type="button" onClick={requestShizuku}>Retry check</button></div></Panel>
      <Panel title="Readiness checks" className="relay-span-12"><div className="relay-check-list relay-check-list-wide">{readinessChecks.map((check) => <div className="relay-check" key={check.id}><span><strong>{check.label}</strong><small>{check.detail}</small></span><Pill status={check.state}>{check.state === "complete" ? "Complete" : shizukuState === "ready" ? "Complete" : "Pending"}</Pill></div>)}</div></Panel>
    </div>
  );

  const controls = () => (
    <div className="relay-view relay-grid"><Title title="Controls" /><Panel title="Configured actions" className="relay-span-9"><div className="relay-empty">No verified commands are configured for {activeMethod ? methods.find((method) => method.id === activeMethod)?.label : "this session"}.</div><button className="relay-secondary" type="button" onClick={() => navigate("/setup")}>Review access setup</button></Panel><Panel title="Session" className="relay-span-3"><div className="relay-stack"><Pill status={activeMethod ? "ready" : "disconnected"}>{activeMethod ? "Ready" : "Disconnected"}</Pill><strong>{activeMethod ? methods.find((method) => method.id === activeMethod)?.label : "No path selected"}</strong><small>No configured command has run.</small><button className="relay-secondary" type="button" disabled={!activeMethod} onClick={() => setActiveMethod(null)}>Disconnect</button></div></Panel></div>
  );

  const activity = () => (
    <div className="relay-view relay-grid"><Title title="Activity" action={<button className="relay-secondary" type="button" onClick={() => setSearch("")}>Refresh</button>} /><Panel title="Local events" className="relay-span-12" action={<span className="relay-count num">{filteredEvents.length} events</span>}><div className="relay-toolbar"><div className="relay-tabs" role="tablist" aria-label="Activity range">{(["today", "all"] as const).map((filter) => <button type="button" role="tab" aria-selected={activityFilter === filter} className={activityFilter === filter ? "is-active" : ""} key={filter} onClick={() => setActivityFilter(filter)}>{filter === "today" ? "Today" : "All local events"}</button>)}</div><label className="relay-search"><span className="sr-only">Search local events</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search local events" /></label></div>{filteredEvents.length ? <EventRows events={filteredEvents} /> : <div className="relay-empty">No local events match this view.</div>}</Panel></div>
  );

  const settings = () => (
    <div className="relay-settings"><Title title="Settings" />
      <SettingsRow title="Access methods" description="Accessibility Service is enabled. Shizuku requires authorization." panel={<><div className="relay-detail-row"><span>Accessibility Service</span><Pill status="ready">Enabled</Pill><button className="relay-secondary" type="button" onClick={() => setActiveMethod("accessibility")}>Review</button></div><div className="relay-detail-row"><span>Shizuku</span><Pill status={shizukuState}>{statusLabel(shizukuState)}</Pill><button className="relay-secondary" type="button" onClick={requestShizuku}>Authorize</button></div></>} />
      <SettingsRow title="Safety" description="Preferences apply to this device and keep local state readable." panel={<><Toggle label="Local event storage" checked={preferences.storage} onChange={() => setPreferences((state) => ({ ...state, storage: !state.storage }))} /><Toggle label="Follow reduced motion" checked={preferences.motion} onChange={() => setPreferences((state) => ({ ...state, motion: !state.motion }))} /><Toggle label="Screen-reader labels" checked={preferences.labels} onChange={() => setPreferences((state) => ({ ...state, labels: !state.labels }))} /><div className="relay-save"><button className="relay-primary" type="button" onClick={() => setSaved(true)}>Save preferences</button>{saved && <span>Saved</span>}</div></>} />
      <SettingsRow title="Reset access" description={`Clearing local history removes ${historyCleared ? 0 : activityEvents.length} local events from this device.`} panel={<div className="relay-stack"><button className="relay-secondary" type="button" disabled={!activeMethod} onClick={() => setActiveMethod(null)}>Disconnect current session</button><button className="relay-danger" type="button" onClick={() => setConfirming(true)}>Clear local history</button></div>} />
    </div>
  );

  const screen = view === "/" ? home() : view === "/setup" ? setup() : view === "/controls" ? controls() : view === "/activity" ? activity() : settings();

  return <div className="relay-app">
    <aside className="relay-sidebar"><div className="relay-brand"><span className="relay-mark" aria-hidden="true"><i /><i /></span><span><strong>Relay</strong><small>This device</small></span></div>{renderNavigation()}<div className="relay-sidebar-foot">Local workspace</div></aside>
    <header className="relay-topbar"><button className="relay-menu-button" type="button" ref={menuTriggerRef} aria-expanded={menuOpen} aria-controls="relay-mobile-menu" onClick={() => setMenuOpen(true)}>Menu</button><label className="relay-global-search"><span className="sr-only">Search Relay</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search local activity" /><kbd>⌘ K</kbd></label><button className="relay-account" type="button">Help</button></header>
    {menuOpen && <div id="relay-mobile-menu" className="relay-menu-sheet" role="dialog" aria-modal="true" aria-label="Relay navigation"><div><div className="relay-menu-head"><strong>Relay</strong><button className="relay-secondary" type="button" onClick={() => setMenuOpen(false)}>Close</button></div>{renderNavigation(true)}</div></div>}
    <main className="relay-sheet" ref={contentRef} tabIndex={-1}>{screen}</main>
    {confirming && <div className="relay-dialog-backdrop" role="presentation"><section className="relay-dialog" role="dialog" aria-modal="true" aria-labelledby="clear-history-title"><h2 id="clear-history-title">Clear local history?</h2><p>This removes {historyCleared ? 0 : activityEvents.length} local sample events from This device.</p><div><button className="relay-secondary" type="button" onClick={() => setConfirming(false)}>Cancel</button><button className="relay-danger" type="button" onClick={() => { setHistoryCleared(true); setConfirming(false); }}>Clear local history</button></div></section></div>}
  </div>;
};

const Title = ({ title, action }: { title: string; action?: React.ReactNode }) => <div className="relay-title relay-span-12"><div><h1>{title}</h1><p>Sample data for This device, {sampleDate}.</p></div>{action}</div>;

const EventRows = ({ events }: { events: readonly { id: string; time: string; event: string; source: string }[] }) => <div className="relay-event-list">{events.map((event) => <div className="relay-event" key={event.id}><span><strong>{event.event}</strong><small>{event.source}</small></span><time className="num">{event.time}</time></div>)}</div>;

const Toggle = ({ label, checked, onChange }: { label: string; checked: boolean; onChange: () => void }) => <label className="relay-toggle"><span><strong>{label}</strong><small>{checked ? "Enabled" : "Disabled"}</small></span><input type="checkbox" checked={checked} onChange={onChange} /><i aria-hidden="true" /></label>;

const SettingsRow = ({ title, description, panel }: { title: string; description: string; panel: React.ReactNode }) => <section className="relay-setting-row"><div><h2>{title}</h2><p>{description}</p></div><div className="relay-setting-panel">{panel}</div></section>;
