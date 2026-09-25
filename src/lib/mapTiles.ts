/**
 * Basemap tile URL + attribution.
 *
 * CARTO gates basemaps.cartocdn.com behind an API key and answers unkeyed
 * requests with HTTP 200 + a PNG that simply reads "API KEY REQUIRED". Leaflet
 * happily renders that on every tile, so a missing key must fall back to a
 * keyless provider instead of showing the watermark.
 *
 * Key source: https://carto.com/basemaps/apikey (free, emailed, no account).
 */

type EnvLike = { env?: Record<string, string | undefined> };

const CARTO_STYLE = "dark_all";
const CARTO_TEMPLATE = `https://{s}.basemaps.cartocdn.com/${CARTO_STYLE}/{z}/{x}/{y}{r}.png`;
const OSM_TEMPLATE = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";

export const CARTO_API_KEY: string = (
  (import.meta as unknown as EnvLike).env?.VITE_CARTO_API_KEY ?? ""
).trim();

export function buildTileUrl(key?: string): string {
  const k = (key ?? "").trim();
  if (!k) return OSM_TEMPLATE;
  return `${CARTO_TEMPLATE}?key=${encodeURIComponent(k)}`;
}

export function tileAttribution(key?: string): string {
  const k = (key ?? "").trim();
  if (!k) return '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
  return '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>';
}
