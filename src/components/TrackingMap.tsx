/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 *
 * Tracking map for the Users tab — dark basemap + HUD reticles.
 * Live vs stale is derived from SecurityUser.lastActiveTs (15-minute window).
 */

import { useEffect, useRef, useState } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { SecurityUser } from "../types";

const LIVE_WINDOW_MS = 15 * 60 * 1000;
const TILE_URL = "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png";
const TILE_ATTR =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>';
const HOME: L.LatLngExpression = [23.8103, 90.4125];

export function isLive(u: SecurityUser, now: number = Date.now()): boolean {
  return typeof u.lastActiveTs === "number" && u.lastActiveTs > 0 && now - u.lastActiveTs < LIVE_WINDOW_MS;
}

export function hasFix(u: SecurityUser): boolean {
  const { lastLatitude: lat, lastLongitude: lng } = u;
  if (typeof lat !== "number" || typeof lng !== "number") return false;
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) return false;
  if (Math.abs(lat) < 1e-6 && Math.abs(lng) < 1e-6) return false; // device default 0.0
  return true;
}

function age(ms: number): string {
  const s = Math.max(0, Math.floor(ms / 1000));
  if (s < 60) return `${s}s`;
  if (s < 3600) return `${Math.floor(s / 60)}m`;
  return `${Math.floor(s / 3600)}h`;
}

function esc(s: string): string {
  return s.replace(/[&<>"']/g, (c) =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c] as string
  );
}

function reticle(u: SecurityUser, live: boolean, focused: boolean): L.DivIcon {
  const raw = (u.name || u.email || "UNKNOWN").trim();
  const label = esc(raw.split(/\s+/)[0].toUpperCase().slice(0, 12) || "UNIT");
  return L.divIcon({
    className: "",
    iconSize: [0, 0],
    iconAnchor: [0, 0],
    html: `<div class="gmap-pin ${live ? "is-live" : "is-stale"}${focused ? " is-focus" : ""}">
        <span class="gmap-ring"></span>
        <span class="gmap-core"></span>
        <span class="gmap-label">${label}</span>
      </div>`,
  });
}

interface TrackingMapProps {
  users: SecurityUser[];
  focusId: string | null;
  /** Incrementing token so re-selecting the same id still pans. */
  panReq?: { id: string; n: number } | null;
  onFocus: (id: string) => void;
}

export default function TrackingMap({ users, focusId, panReq, onFocus }: TrackingMapProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<L.Map | null>(null);
  const markersRef = useRef<Map<string, L.Marker>>(new Map());
  const fittedRef = useRef(false);
  const focusRef = useRef(onFocus);
  focusRef.current = onFocus;

  const [clock, setClock] = useState(() => Date.now());
  const [syncAt, setSyncAt] = useState(() => Date.now());
  const [refresh, setRefresh] = useState(0);
  const [cursor, setCursor] = useState<{ lat: number; lng: number; z: number } | null>(null);

  // --- map lifecycle -------------------------------------------------------
  useEffect(() => {
    const el = containerRef.current;
    if (!el || mapRef.current) return;

    const map = L.map(el, {
      zoomControl: false,
      attributionControl: false,
      minZoom: 2,
      maxZoom: 19,
      zoomSnap: 0.5,
      worldCopyJump: true,
    });

    L.tileLayer(TILE_URL, { subdomains: "abcd", maxZoom: 19, detectRetina: true }).addTo(map);
    map.attributionControl.setPrefix(false);
    map.attributionControl.setPosition("bottomleft");
    map.attributionControl.addAttribution(TILE_ATTR);
    L.control.zoom({ position: "bottomright", zoomInTitle: "Zoom in", zoomOutTitle: "Zoom out" }).addTo(map);

    map.setView(HOME, 6);
    map.on("mousemove", (e: L.LeafletMouseEvent) =>
      setCursor({ lat: e.latlng.lat, lng: e.latlng.lng, z: map.getZoom() })
    );
    map.on("mouseout", () => setCursor(null));

    const ro = new ResizeObserver(() => map.invalidateSize());
    ro.observe(el);

    mapRef.current = map;
    return () => {
      ro.disconnect();
      map.remove();
      mapRef.current = null;
      markersRef.current.clear();
      fittedRef.current = false;
    };
  }, []);

  // --- clock / staleness ticker -------------------------------------------
  useEffect(() => {
    const t = window.setInterval(() => setClock(Date.now()), 1000);
    const r = window.setInterval(() => setRefresh((n) => n + 1), 15000);
    return () => { window.clearInterval(t); window.clearInterval(r); };
  }, []);

  // --- sync markers to the user list --------------------------------------
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    const markers = markersRef.current;
    const keep = new Set<string>();

    for (const u of users) {
      if (!hasFix(u)) continue;
      keep.add(u.id);
      const pos = L.latLng(u.lastLatitude as number, u.lastLongitude as number);
      const live = isLive(u, clock);
      const focused = u.id === focusId;

      let m = markers.get(u.id);
      if (!m) {
        m = L.marker(pos, { icon: reticle(u, live, focused), keyboard: false, riseOnHover: true });
        m.bindTooltip("", { direction: "top", offset: [0, -16], className: "gmap-tooltip", opacity: 1 });
        m.on("click", () => focusRef.current(u.id));
        m.addTo(map);
        markers.set(u.id, m);
      } else {
        if (!m.getLatLng().equals(pos)) m.setLatLng(pos);
        m.setIcon(reticle(u, live, focused));
      }
      const seen = u.lastActiveTs ? age(clock - u.lastActiveTs) : "never";
      m.setTooltipContent(
        `<b>${esc(u.name || u.email || "UNKNOWN")}</b><br><span class="gmap-tip-sub">` +
        `${live ? "LIVE" : "STALE"} · heartbeat ${esc(seen)} ago</span>`
      );
    }

    markers.forEach((m, id) => {
      if (!keep.has(id)) { m.remove(); markers.delete(id); }
    });

    if (!fittedRef.current && markers.size > 0) {
      const pts = [...markers.values()].map((m) => m.getLatLng());
      map.fitBounds(L.latLngBounds(pts), { padding: [56, 56], maxZoom: 15 });
      fittedRef.current = true;
    }

    setSyncAt(Date.now());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [users, focusId, refresh]);

  // --- pan when a row or marker requests it -------------------------------
  useEffect(() => {
    if (!panReq) return;
    const map = mapRef.current;
    const m = markersRef.current.get(panReq.id);
    if (!map || !m) return;
    const reduce = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    map.panTo(m.getLatLng(), { animate: !reduce, duration: 0.55, easeLinearity: 0.25 });
  }, [panReq]);

  const withFix = users.filter(hasFix);
  const liveCount = withFix.filter((u) => isLive(u, clock)).length;
  const staleCount = withFix.length - liveCount;
  const noFixCount = users.length - withFix.length;
  const syncAge = Math.max(0, Math.floor((clock - syncAt) / 1000));
  const stamp = new Date(clock).toTimeString().slice(0, 8);

  return (
    <div className="gmap-frame relative w-full h-full overflow-hidden rounded-xl border border-[#2a3441] bg-[#050f1b] shadow-[0_0_0_1px_rgba(0,255,136,0.05),0_24px_60px_-30px_rgba(0,0,0,0.9)]">
      <div ref={containerRef} className="absolute inset-0" />

      <div className="gmap-hud absolute inset-0 pointer-events-none">
        <div className="gmap-grid absolute inset-0" />
        <div className="gmap-scan absolute inset-0" />
        <span className="gmap-bracket top-2 left-2 border-t border-l" />
        <span className="gmap-bracket top-2 right-2 border-t border-r" />
        <span className="gmap-bracket bottom-2 left-2 border-b border-l" />
        <span className="gmap-bracket bottom-2 right-2 border-b border-r" />

        {/* Command bar */}
        <div className="absolute top-0 inset-x-0 z-[900] flex items-center justify-between gap-3 px-3.5 py-2.5 border-b border-[#00ff88]/20 bg-gradient-to-b from-[#050f1b]/95 via-[#050f1b]/80 to-transparent">
          <div className="flex items-center gap-2 min-w-0">
            <span className="w-1.5 h-1.5 rounded-full bg-[#00ff88] animate-pulse shrink-0" />
            <span className="font-mono text-[10px] font-bold tracking-[0.18em] text-[#00ff88] uppercase truncate">
              GUARDIAN&nbsp;//&nbsp;TRACKING GRID
            </span>
          </div>
          <div className="flex items-center gap-2.5 font-mono text-[10px] tracking-widest uppercase shrink-0">
            <span className="text-[#00ff88] font-bold" title="Devices reporting within 15 minutes">
              LIVE {String(liveCount).padStart(2, "0")}
            </span>
            <span className="text-[#ffea2a]/80" title="Has a fix but no recent heartbeat">
              STALE {String(staleCount).padStart(2, "0")}
            </span>
            <span className="text-[#b9cbb9]/45" title="Never reported a location">
              NOFIX {String(noFixCount).padStart(2, "0")}
            </span>
          </div>
        </div>

        {/* Telemetry strip */}
        <div className="absolute left-3 right-3 bottom-9 z-[900] flex items-end justify-between gap-3 font-mono text-[9.5px] tracking-wider uppercase">
          <div className="flex flex-col gap-1.5">
            <div className="flex items-center gap-3 px-2 py-1.5 rounded border border-[#2a3441]/80 bg-[#050f1b]/85 backdrop-blur-sm">
              <span className="flex items-center gap-1.5 text-[#00ff88]">
                <span className="gmap-key gmap-key-live" /> LIVE
              </span>
              <span className="flex items-center gap-1.5 text-[#ffea2a]/85">
                <span className="gmap-key gmap-key-stale" /> STALE
              </span>
              <span className="flex items-center gap-1.5 text-[#b9cbb9]/55">
                <span className="gmap-key gmap-key-nofix" /> NO FIX
              </span>
            </div>
            <div className="px-2 py-1 rounded border border-[#2a3441]/80 bg-[#050f1b]/85 backdrop-blur-sm text-[#b9cbb9]/70">
              {cursor
                ? `LAT ${cursor.lat.toFixed(5)} · LNG ${cursor.lng.toFixed(5)} · Z${cursor.z}`
                : `SYNC ${String(syncAge).padStart(2, "0")}S · ${stamp} LOCAL`}
            </div>
          </div>
          <div className="hidden sm:block px-2 py-1 rounded border border-[#2a3441]/80 bg-[#050f1b]/85 backdrop-blur-sm text-[#b9cbb9]/45 text-right">
            FEED 07S · {users.length} UNIT{users.length === 1 ? "" : "S"}
          </div>
        </div>
      </div>
    </div>
  );
}
